package com.thecalda.nordicidnurplugin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.util.Log
import com.nordicid.nuraccessory.*
import com.nordicid.nurapi.*
import com.nordicid.nurapi.NurApi.BANK_TID
import com.nordicid.tdt.*
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*


@SuppressLint("StaticFieldLeak")
object NurHelper {

    const val TAG: String = "NUR_HELPER" //Can be used for filtering Log's at Logcat

    const val hardcoded_integrated_device_address = "integrated_reader"

    var context: Activity? = null
    lateinit var methodChannel: MethodChannel;

    lateinit var mNurApi: NurApi
    private var mAccExt: AccessoryExtension? = null
    private var hAcTr: NurApiAutoConnectTransport? = null

    //When connected, this flag is set depending if Accessories like barcode scan, beep etc supported.
    private var mIsAccessorySupported: Boolean = false
    private var mIsConnected = false

    //Need to keep track when barcode mScanning or mAiming ongoing in case it cancelled by trigger press or program leave
    private var mScanning = false
    private var mAiming = false

    private var triggerDown = false

    private var barcodeScanMode = false
    private var singleRFIDScanMode = false
    private var inventoryStreamMode = false

    private var barcodeScanTimeout = 5000;

    private var singleRFIDScanTaskRunning = false

    private var singleRFIDScanTimeout = 5000;

    //Temporary storing current TX level because single tag will be search using low TX level
    private var singleRFIDScanTempTxLevel: Int = 0

    //This counter add by one when single tag found after inventory. Reset to zero if multiple tags found.
    private var singleRFIDScanTagFoundCount: Int = 0

    //This variable hold last tag epc for making sure same tag found 3 times in row.
    private var singleRFIDScanTagUnderReview: String = ""

    fun init(activity: Activity, channel: MethodChannel, autoConnect: Boolean) {
        Log.i(TAG, "init")
        this.context = activity
        this.methodChannel = channel;

        mIsConnected = false
        mIsAccessorySupported = false

        //Create NurApi handle.
        mNurApi = NurApi()

        mNurApi.setLogLevel(NurApi.LOG_VERBOSE);

        //Accessory extension contains device specific API like barcode read, beep etc..
        //This included in NurApi.jar
        mAccExt = AccessoryExtension(mNurApi)

        // In this activity, we use mNurApiListener for receiving events
        mNurApi.setListener(mNurApiListener)

        //Bluetooth LE scanner need to find EXA's near
        BleScanner.init(activity);

        methodChannel.invokeMethod("onInitialised", true)

        //TODO autoConnect
        // hardcoded_integrated_device_address

        if(autoConnect) {
            try {
                connectToDevice("type=INT;addr=integrated_reader;name=Integrated Reader")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun connectToDevice(deviceSpecString: String) {
        Log.i(TAG, "NurDeviceListActivity.SPECSTR = " + deviceSpecString)

        val spec: NurDeviceSpec = NurDeviceSpec(deviceSpecString)
        //type=INT;addr=integrated_reader;name=Integrated Reader

        if (hAcTr != null) {
            println("Dispose transport")
            hAcTr?.dispose()
        }

        val strAddress: String?
        hAcTr = NurDeviceSpec.createAutoConnectTransport(context, mNurApi, spec)
        strAddress = spec.getAddress()
        Log.i(TAG, "Dev selected: code = " + strAddress)
        Log.i(TAG, "NurDeviceSpec = " + spec.getSpec())
        hAcTr?.setAddress(strAddress)
    }

    fun isInitialised(): Boolean {
        if (::mNurApi.isInitialized && mAccExt != null && context != null) {
            return true
        }
        return false
    }

    fun startDeviceDiscovery(activity: Activity) {
        Log.i(TAG, "startDeviceDiscovery")
//        Toast.makeText(activity, "Start searching. Make sure device power ON!", Toast.LENGTH_LONG).show();
        if (::mNurApi.isInitialized ) {
            NurDeviceListActivity.startDeviceRequest(activity, mNurApi)
        }
    }

    fun disconnect() {
        if(::mNurApi.isInitialized && mNurApi.isConnected()) {
            hAcTr?.dispose();
            hAcTr = null;

            context?.runOnUiThread {
                methodChannel.invokeMethod("onDisconnected", null)
            }
        }
    }

    fun isConnected(): Boolean {
        return mIsConnected
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult; requestCode ${requestCode}, resultCode ${resultCode}")
        if (requestCode == NurDeviceListActivity.REQUEST_SELECT_DEVICE) {
            if (data == null || resultCode != NurDeviceListActivity.RESULT_OK) {
                return
            }

            try {
                val deviceSpecString = data.getStringExtra(NurDeviceListActivity.SPECSTR)

                if (deviceSpecString != null) {
                    connectToDevice(deviceSpecString)
                }

//                showConnecting()

                //If you want connect to same device automatically later on, you can save 'strAddress" and use that for connecting at app startup for example.
                //saveSettings(spec);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanBarcode(timeout: Int) {
        Log.i(TAG, "scanBarcode")

        barcodeScanTimeout = timeout

        barcodeScanMode = true
        singleRFIDScanMode = false
        inventoryStreamMode = false

        mAccExt?.registerBarcodeResultListener(mBarcodeResult)
    }

    fun scanSingleRFID(timeout: Int) {
        Log.i(TAG, "scanSingleRFID")

        singleRFIDScanTimeout = timeout

        barcodeScanMode = false
        singleRFIDScanMode = true
        inventoryStreamMode = false

        val thread = Thread {
            mAccExt?.cancelBarcodeAsync()
        }
        thread.start()
    }

    fun setInventoryStreamMode() {
        Log.i(TAG, "setInventoryStreamMode")

        barcodeScanMode = false
        singleRFIDScanMode = false
        inventoryStreamMode = true

        val thread = Thread {
            mAccExt?.cancelBarcodeAsync()
        }
        thread.start()
    }

    /**
     * NurApi event handlers.
     * NOTE: All NurApi events are called from NurApi thread, thus direct UI updates are not allowed.
     * If you need to access UI controls, you can use runOnUiThread(Runnable) or Handler.
     */
    private val mNurApiListener = object : NurApiListener {
        override fun triggeredReadEvent(event: NurEventTriggeredRead) {
            Log.i(TAG, "triggeredReadEvent")
        }

        override fun traceTagEvent(event: NurEventTraceTag) {
            Log.i(TAG, "traceTagEvent")
        }

        override fun programmingProgressEvent(event: NurEventProgrammingProgress) {
            Log.i(TAG, "programmingProgressEvent")
        }

        override fun nxpEasAlarmEvent(event: NurEventNxpAlarm) {
            Log.i(TAG, "nxpEasAlarmEvent")
        }

        override fun logEvent(level: Int, txt: String) {
//            Log.i(TAG, "logEvent; ${level}, ${txt}")
        }

        override fun inventoryStreamEvent(event: NurEventInventory) {
//            Log.i(TAG, "inventoryStreamEvent")
            try {
                if (event.stopped) {
                    //InventoryStreaming is not active for ever. It automatically stopped after ~20 sec but it can be started again immediately if needed.
                    //check if need to restart streaming
                    if (triggerDown) {
                        mNurApi.startInventoryStream(); //Trigger button still down so start it again.
                    }
                } else {
                    if (event.tagsAdded > 0) {

                        val tagStorage: NurTagStorage = mNurApi.getStorage() //Storage contains all tags found

//                        for (x in 0 until event.tagsAdded) {
//                            val tag: NurTag = tagStorage.get(x)
//                            Log.i(TAG, "tag: ${tag.getEpcString()}")
//                        }

                        val epcStrings = mutableListOf<String>()

                        for (x in 0 until event.tagsAdded) {
                            val tag: NurTag = tagStorage.get(x)
                            val epcString = tag.getEpcString()
                            epcStrings.add(epcString)
                            Log.i(TAG, "tag: $epcString")
                        }

                        val jsonArray = "[" + epcStrings.joinToString(separator = ",") { "\"$it\"" } + "]"

//                        val epcStrings: List<String> = (0 until event.tagsAdded).map {
//                            val tag: NurTag = tagStorage.get(it)
//                            tag.getEpcString()
//                        }
                        context?.runOnUiThread {
                            // hashMapOf("data" to accBarcodeResult.strBarcode)
                            methodChannel.invokeMethod("onInventoryStreamEvent", hashMapOf("data" to jsonArray))
                        }

                    }
                }
            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "${ex.message}")
            }
        }

        override fun inventoryExtendedStreamEvent(event: NurEventInventory) {
            Log.i(TAG, "inventoryExtendedStreamEvent")
        }

        override fun frequencyHopEvent(event: NurEventFrequencyHop) {
            Log.i(TAG, "frequencyHopEvent")
        }

        override fun epcEnumEvent(event: NurEventEpcEnum) {
            Log.i(TAG, "epcEnumEvent")
        }

        override fun deviceSearchEvent(event: NurEventDeviceInfo) {
            Log.i(TAG, "deviceSearchEvent")
        }

        override fun debugMessageEvent(event: String) {
            Log.i(TAG, "debugMessageEvent")
        }

        override fun connectedEvent() {
            Log.i(TAG, "connectedEvent")

            //Device is connected.
            // Let's find out is device provided with accessory support (Barcode reader, battery info...) like EXA
            try {
                if (mAccExt?.isSupported() == true) {
                    Log.i(TAG, "mAccExt isSupported")
                    //Yes. Accessories supported
                    mIsAccessorySupported = true
                    //Let's take name of device from Accessory api
                    Log.i(TAG, "Connected to (Accessory api): ${mAccExt?.getConfig()?.name}")
                } else {
                    Log.i(TAG, "mAccExt isNotSupported")
                    //Accessories not supported. Probably fixed reader.
                    mIsAccessorySupported = false

//                    mUiConnStatusText = "Connected to " + ri.name
                }

                val ri: NurRespReaderInfo = mNurApi.getReaderInfo()
                Log.i(TAG, "Connected to (NurRespReaderInfo): ${ri.name}, ${ri.altSerial}")

            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "${ex.message}")
            }

            Log.i(TAG, "Connected!")
            mIsConnected = true
            context?.runOnUiThread {
                methodChannel.invokeMethod("onConnected", null)
            }
//            Beeper.beep(Beeper.BEEP_100MS)
//
//            mUiConnStatusTextColor = Color.GREEN
//            mUiConnButtonText = "DISCONNECT"
//            showOnUI()
        }

        override fun disconnectedEvent() {
            Log.i(TAG, "disconnectedEvent")

            mIsConnected = false
            context?.runOnUiThread {
                methodChannel.invokeMethod("onDisconnected", null)
            }
        }

        override fun clientDisconnectedEvent(event: NurEventClientInfo) {
            Log.i(TAG, "clientDisconnectedEvent")
        }

        override fun clientConnectedEvent(event: NurEventClientInfo) {
            Log.i(TAG, "clientConnectedEvent")
        }

        override fun bootEvent(event: String) {
            Log.i(TAG, "bootEvent")
        }

        override fun IOChangeEvent(event: NurEventIOChange) {
            Log.i(TAG, "IOChangeEvent Key ${event.source}")

            handleIOEvent(event)
        }

        override fun autotuneEvent(event: NurEventAutotune) {
            Log.i(TAG, "autotuneEvent")
        }

        override fun tagTrackingScanEvent(event: NurEventTagTrackingData) {
            Log.i(TAG, "tagTrackingScanEvent")
        }

        // @Override
        override fun tagTrackingChangeEvent(event: NurEventTagTrackingChange) {
            Log.i(TAG, "tagTrackingChangeEvent")
        }
    }

    private val mBarcodeResult: AccBarcodeResultListener = object : AccBarcodeResultListener {
        public override fun onBarcodeResult(accBarcodeResult: AccBarcodeResult) {
            Log.i(TAG, "BarcodeResult " + accBarcodeResult.strBarcode + ", Status = " + accBarcodeResult.status)
            mScanning = false

            context?.runOnUiThread {
                methodChannel.invokeMethod("onBarcodeScanned", hashMapOf("data" to accBarcodeResult.strBarcode))
            }

//            if (accBarcodeResult.status === NurApiErrors.NO_TAG) {
//                mUiStatusText = "No barcode found"
//                Beeper.beep(Beeper.FAIL)
//                mScanning = false
//            } else if (accBarcodeResult.status === NurApiErrors.NOT_READY) {
//                mUiStatusText = "Cancelled"
//            } else if (accBarcodeResult.status === NurApiErrors.HW_MISMATCH) {
//                //This reader does'nt support imager.
//                mUiStatusText = "No hardware found"
//                Beeper.beep(Beeper.FAIL)
//                mScanning = false
//            } else if (accBarcodeResult.status !== NurApiErrors.NUR_SUCCESS) {
//                mUiStatusText = "Error: " + accBarcodeResult.status
//                Beeper.beep(Beeper.FAIL)
//                mScanning = false
//            } else {
//                //Barcode scan success. Show result on the screen.
//                mUiResultText = accBarcodeResult.strBarcode
//                mUiStatusText = "Waiting trigger..."
//                Beeper.beep(Beeper.BEEP_100MS) //Beep on phone
//
//                try {
//                    mAccExt.beepAsync(100) //Beep on device
//                    if (mAccExt.getConfig().hasVibrator()) {
//                        //Device has vibra so vibrate.
//                        mAccExt.vibrate(200)
//                    }
//                } catch (ex: java.lang.Exception) {
//                    mToastLong = "Error! " + ex.message
//                }
//
//                mScanning = false
//            }
//
//            showOnUI()
        }
    }

    /**
     * Handle I/O events from reader.
     * When user press button on reader, event fired and handled in this function.
     * NurEventIOChange offers 'source' and 'direction' to determine which button changes state.
     * source 100 = Trigger button
     * source 101 = Power button
     * source 102 = Unpair button
     * direction 0 = Button released
     * direction 1 = Button pressed down
     */
    private fun handleIOEvent(event: NurEventIOChange) {
        Log.i(TAG, "IO src=" + event.source + ", Dir=" + event.direction)

        if (barcodeScanMode) {
            try {
                if (event.source === 100 && event.direction === 1) {
                    if (mScanning) {
                        //There is mScanning ongoing so we need just abort it
                        mAccExt?.cancelBarcodeAsync()
                        Log.i(TAG, "Cancelling..")
                    } else {
                        mAiming = true
                        mAccExt?.imagerAIM(mAiming)
//                    mUiStatusText = "Aiming..."
                    }
                } else if (event.source === 100 && event.direction === 0) {
                    if (mScanning) {
                        mScanning = false
                        return
                    }
                    //Trigger released. Stop aiming and start mScanning
                    mAiming = false
                    mAccExt?.imagerAIM(mAiming)
                    mAccExt?.readBarcodeAsync(barcodeScanTimeout) //5 sec timeout
//                mUiStatusText = "Scanning barcode..."
                    mScanning = true
                } else if (event.source === 101) {
//                if (event.direction === 0) mToastShort = "Power button released"
//                else mToastShort = "Power button pressed"
                } else if (event.source === 102) {
//                if (event.direction === 0) mToastShort = "Unpair button released"
//                else mToastShort = "Unpair button pressed"
                }
            } catch (ex: java.lang.Exception) {
                //Show error on status field
//            mUiStatusText = ex.message
                Log.e(TAG, "${ex.message}")
            }
        } else if (singleRFIDScanMode) {
            //Single scan operation start when Unpair button push down but no need to keep down during operation.
            if (event.direction == 1) {
                //Unpair button pressed. if SingleScan already running so nothing to do.
                singleRFIDScan(); //Do the single scan. No need to keep unpair button down.
            }

        } else if (inventoryStreamMode) {
            if (event.direction == 1) {
//                StartInventoryStream(); //Start Inventory streaming.
                startInventoryStream();
            } else if (event.direction == 0) {
//                StopInventoryStream(); //Trigger released. Stop streaming if running.
                stopInventoryStream();
            }
        }


//        showOnUI()
    }

    // ScanSingleTagThread
    private fun singleRFIDScan() {
        Log.i(TAG, "singleRFIDScan");
        if (singleRFIDScanTaskRunning || triggerDown) {
            return
        }

        var foundATag = false;

        val singleRFIDScanThread = Thread {

            singleRFIDScanTaskRunning = true
            singleRFIDScanTagFoundCount = 0

            //Store current TX level of RFID reader
            try {
                singleRFIDScanTempTxLevel = mNurApi.getSetupTxLevel()
                Log.i(TAG, "Current TxLevel = " + singleRFIDScanTempTxLevel)
                //Set rather low TX power. You need to get close to tag for successful reading
                mNurApi.setSetupTxLevel(NurApi.TXLEVEL_8)
            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "${ex.message}")
            }

            val time_start = System.currentTimeMillis()

            while (singleRFIDScanTaskRunning) {
                try {
                    mNurApi.clearIdBuffer(); //Clear buffer from existing tags

                    //Do the inventory with small rounds and Q values. We looking for single tag..
                    //mNurApi.setIRConfig(NurApi.IRTYPE_EPCDATA, BANK_TID, 0, 6);
                    val resp: NurRespInventory = mNurApi.inventory(2, 4, 0) //Rounds=2, Q=4, Session=0

                    if (resp.numTagsFound > 1) {
                        Log.i(TAG, "Too many tags seen: ${resp.numTagsFound}")
                        singleRFIDScanTagFoundCount = 0

                        context?.runOnUiThread {
                            methodChannel.invokeMethod("onSingleRFIDScanned", hashMapOf(
                                "data" to "",
                                "errorMessage" to "Too many tags seen!",
                                "errorNumberOfTags" to resp.numTagsFound))
                        }

                    } else {
                        val tag: NurTag = mNurApi.fetchTagAt(true, 0) //Get tag information from pos 0

                        //We looking for same tag in antenna field seen 3 times in row. isSameTag function make sure it is.
                        if (isSameTag(tag.getEpcString())) {
                            singleRFIDScanTagFoundCount++
                        } else {
                            singleRFIDScanTagFoundCount = 1 //It was not. Start all over.
                        }

                        if (singleRFIDScanTagFoundCount >= 3) {
                            singleRFIDScanTaskRunning = false
                            Log.i(TAG, "Same tag found 3 times")

                            Log.d(TAG, "Tag getEpcString: ${tag.getEpcString()}")
                            foundATag = true;

                            context?.runOnUiThread {
                                methodChannel.invokeMethod("onSingleRFIDScanned", hashMapOf("data" to tag.getEpcString()))
                            }


//                            Log.d(TAG, "Tag getDataString: ${tag.getDataString()}")

                            //Single tag found multiple times (3) in row so let's accept.
//                            try {
//                                //Check if tag is GS1 coded. Exception fired if not and plain EPC shown.
//                                //This is TDT (TagDataTranslation) library feature.
//                                val engine: EPCTagEngine = EPCTagEngine(tag.getEpcString())
//                                //Looks like it is GS1 coded, show pure Identity URI
//                                val gs: String? = engine.buildPureIdentityURI()
//                                Log.i(TAG, "GS1 coded tag!")
//                                Log.i(TAG, "gs: $gs")
//                            } catch (ex: java.lang.Exception) {
//                                Log.i(TAG, "Single Tag found!")
//                                Log.i(
//                                    TAG,
//                                    "EPC= ${tag.getEpcString()}, TID= ${NurApi.byteArrayToHexString(tag.getIrData())}"
//                                )
//                            }

                            /*
                                Reading TID BANK
                                EPC is known at this point, let's use that for reading first 32-bit TID bank using readTagByEpc()
                                Change the 'rdAddress' and'readByteCount' params for your purposes. Make sure values are in word boundaries (2,4,6,8...)
                                */
//                            try {
//                                val tidBank1: ByteArray? =
//                                    mNurApi.readTagByEpc(tag.getEpc(), tag.getEpc().size, BANK_TID, 0, 4)
//                                Log.i(TAG, "\nTID: ${NurApi.byteArrayToHexString(tidBank1)}")
//                                //SAMPLE WRITING EPC SINGULATED BY TID
//                                //final byte newEpc[] = new byte[] { (byte)0xaa, (byte)0xbb,(byte)0xcc,(byte)0xdd };
//                                //writeEpcByTID(tidBank1,tidBank1.length, newEpc.length, newEpc);
//                            } catch (e: NurApiException) {
//                                Log.i(TAG, "\nTID: ${e.message}")
//                            }
                        }
                    }

                } catch (ex: java.lang.Exception) {
                    Log.e(TAG, "${ex.message}")
                }

                //We try scan max singleRFIDScanTimeout millisec
                if (System.currentTimeMillis() >= time_start + singleRFIDScanTimeout) {
//                    //Give up.
//                    mUiResultMsg = "No single tag found";
//                    mUiResultColor = Color.RED;
//                    mUiStatusMsg = "Waiting button press...";
//                    //give some kind of "timeout beeps" for user
//                    if(MainActivity.IsAccessorySupported())
//                        mAccExt.beepAsync(300);
//                    else
//                        Beeper.beep(Beeper.BEEP_300MS);

                    singleRFIDScanTaskRunning = false
                    Log.i(TAG, "singleRFIDScan Thread End")
                }

                Thread.sleep(25)
            }

            try {
                mNurApi.setSetupTxLevel(singleRFIDScanTempTxLevel)
            } catch (ex: java.lang.Exception) {
                Log.e(TAG, "${ex.message}")
            }

            Log.i(TAG, "singleRFIDScan scan end")
            if (!foundATag) {
                context?.runOnUiThread {
                    methodChannel.invokeMethod("onSingleRFIDScanned", hashMapOf(
                        "data" to "",
                        "errorMessage" to "No tags found!",
                        "errorNumberOfTags" to 0))
                }
            }


        }
        singleRFIDScanThread.start()

    }

    /**
     * Start inventory streaming. After this you can receive InventoryStream events.
     * Inventory stream is active around 20 sec then stopped automatically. Event received about the state of streaming so you can start it immediately again.
     */
    private fun startInventoryStream() {
        Log.i(TAG, "startInventoryStream");
        if (singleRFIDScanTaskRunning || triggerDown) {
            // mInvStreamButton.setChecked(false);
            return //Already running tasks so let's not disturb that operation.
        }

        mNurApi.setSetupTxLevel(NurApi.TXLEVEL_27)

        try {
            mNurApi.clearIdBuffer() //This command clears all tag data currently stored into the module’s memory as well as the API's internal storage.
            mNurApi.startInventoryStream() //Kick inventory stream on. Now inventoryStreamEvent handler offers inventory results.
            triggerDown = true //Flag to indicate inventory stream running
//            mTagsAddedCounter = 0
//            mUiResultMsg = "Tags:" + java.lang.String.valueOf(mTagsAddedCounter)
//            mUiStatusMsg = "Inventory streaming..."

            context?.runOnUiThread {
                methodChannel.invokeMethod("onStartInventoryStream", null)
            }
        } catch (ex: java.lang.Exception) {
            Log.e(TAG, "${ex.message}")
        }
    }

    /**
     * Stop streaming.
     */
    private fun stopInventoryStream() {
        Log.i(TAG, "stopInventoryStream");

        try {
            if (mNurApi.isInventoryStreamRunning()) {
                mNurApi.stopInventoryStream()
            }
            triggerDown = false
//            mUiStatusMsg = "Waiting button press..."
//            mUiStatusColor = Color.BLACK
            context?.runOnUiThread {
                methodChannel.invokeMethod("onStopInventoryStream", null)
            }
        } catch (ex: java.lang.Exception) {
            Log.e(TAG, "${ex.message}")
//            mUiResultMsg = ex.message
//            mUiResultColor = Color.RED
        }
    }

    /**
     * Check is tag same as previous one
     * @param epc
     * @return
     */
    fun isSameTag(epc: String): Boolean {
        if (epc.compareTo(singleRFIDScanTagUnderReview) == 0) {
            return true
        } else {
            singleRFIDScanTagUnderReview = epc
        }

        return false
    }


}
