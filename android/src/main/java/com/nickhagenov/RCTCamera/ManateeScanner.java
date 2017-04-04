package com.nickhagenov.RCTCamera;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.manateeworks.BarcodeScanner;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by kitty on 4/4/17.
 */

public class ManateeScanner {
    public static ManateeScanner instance = null;
    private static List<String> barCodeTypes = null;
    private static String ManateeKey = "";
    private static boolean initialized = false;
    // concurrency lock for barcode scanner to avoid flooding the runtime
    public static volatile boolean barcodeScannerTaskLock = false;

    public static final int ID_AUTO_FOCUS = 0x01;
    public static final int ID_DECODE = 0x02;
    public static final int ID_RESTART_PREVIEW = 0x04;
    public static final int ID_DECODE_SUCCEED = 0x08;
    public static final int ID_DECODE_FAILED = 0x10;
    // reader instance for the barcode scanner
    public static final boolean USE_MWANALYTICS = false;
    public static final boolean PDF_OPTIMIZED = false;
    public static final boolean USE_MWPARSER = false;

    public static final int USE_RESULT_TYPE = BarcodeScanner.MWB_RESULT_TYPE_MW;

    public static final OverlayMode OVERLAY_MODE = OverlayMode.OM_MWOVERLAY;

    // !!! Rects are in format: x, y, width, height !!!
    public static final Rect RECT_LANDSCAPE_1D = new Rect(3, 20, 94, 60);
    public static final Rect RECT_LANDSCAPE_2D = new Rect(20, 5, 60, 90);
    public static final Rect RECT_PORTRAIT_1D = new Rect(20, 3, 60, 94);
    public static final Rect RECT_PORTRAIT_2D = new Rect(20, 5, 60, 90);
    public static final Rect RECT_FULL_1D = new Rect(3, 3, 94, 94);
    public static final Rect RECT_FULL_2D = new Rect(20, 5, 60, 90);
    public static final Rect RECT_DOTCODE = new Rect(30, 20, 40, 60);

    private enum State {
        STOPPED, PREVIEW, DECODING
    }

    private enum OverlayMode {
        OM_IMAGE, OM_MWOVERLAY, OM_NONE
    }
    State state = State.STOPPED;
    public ManateeScanner() {
        instance = this;
        if (!initialized && ManateeKey.length() > 0) {
            initializeBarcodeTypes();
        }
    }
    public static ManateeScanner getInstance() {
        return instance;
    }
    public static void setKey(String key) {
        ManateeKey = key;
        if (instance != null) {
            instance.initBarcodeReader();
            initialized = true;
        }
    }
    private void initializeBarcodeTypes() {
        int code = 0;
        BarcodeScanner
                .MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL
                        | BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
        for (String codeType: barCodeTypes) {
            if (codeType.equals("QR")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_QR;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR, RECT_FULL_1D);
            } else if (codeType.equals("25")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_25;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25, RECT_FULL_1D);
            } else if (codeType.equals("39")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_39;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39, RECT_FULL_1D);
            } else if (codeType.equals("93")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_93;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93, RECT_FULL_1D);
            } else if (codeType.equals("128")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_128;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128, RECT_FULL_1D);
            } else if (codeType.equals("ACTEC")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_AZTEC;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC, RECT_FULL_1D);
            } else if (codeType.equals("DM")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_DM;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM, RECT_FULL_1D);
            } else if (codeType.equals("EANUPC")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_EANUPC;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_FULL_1D);
            } else if (codeType.equals("PDF")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_PDF;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, RECT_FULL_1D);
            } else if (codeType.equals("CODABAR")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_CODABAR;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR, RECT_FULL_1D);
            } else if (codeType.equals("11")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_11;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11, RECT_FULL_1D);
            } else if (codeType.equals("MSI")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_MSI;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI, RECT_FULL_1D);
            } else if (codeType.equals("RSS")) {
                code = code | BarcodeScanner.MWB_CODE_MASK_RSS;
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS, RECT_FULL_1D);
            }
        }
        BarcodeScanner.MWBsetActiveCodes(code);
    }
    public static void setBarcodeTypes(List<String> codeTypes) {
        barCodeTypes = codeTypes;
        if (instance != null && initialized) {
            instance.initializeBarcodeTypes();
        }
    }
    public void initBarcodeReader() {
        Activity mActivity = RCTCameraPackage.getModuleInstance().getActivity();
        BarcodeScanner.MWBsetFlags(0, BarcodeScanner.MWB_CFG_GLOBAL_CALCULATE_1D_LOCATION | BarcodeScanner.MWB_CFG_GLOBAL_VERIFY_1D_LOCATION);
        int registerResult = BarcodeScanner.MWBregisterSDK(ManateeKey, mActivity);
        switch (registerResult) {
            case BarcodeScanner.MWB_RTREG_OK:
                Log.i("MWBregisterSDK", "Registration OK");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_KEY:
                Log.e("MWBregisterSDK", "Registration Invalid Key");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_CHECKSUM:
                Log.e("MWBregisterSDK", "Registration Invalid Checksum");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_APPLICATION:
                Log.e("MWBregisterSDK", "Registration Invalid Application");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_SDK_VERSION:
                Log.e("MWBregisterSDK", "Registration Invalid SDK Version");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_KEY_VERSION:
                Log.e("MWBregisterSDK", "Registration Invalid Key Version");
                break;
            case BarcodeScanner.MWB_RTREG_INVALID_PLATFORM:
                Log.e("MWBregisterSDK", "Registration Invalid Platform");
                break;
            case BarcodeScanner.MWB_RTREG_KEY_EXPIRED:
                Log.e("MWBregisterSDK", "Registration Key Expired");
                break;

            default:
                Log.e("MWBregisterSDK", "Registration Unknown Error");
                break;
        }

        // set decoder effort level (1 - 5)
        // for live scanning scenarios, a setting between 1 to 3 will suffice
        // levels 4 and 5 are typically reserved for batch scanning
        BarcodeScanner.MWBsetLevel(2);
        BarcodeScanner.MWBsetResultType(USE_RESULT_TYPE);

        // Set minimum result length for low-protected barcode types
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_25, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_MSI, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_39, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_CODABAR, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_11, 5);

        initializeBarcodeTypes();
    }

    public void scanImage(byte[] data, Camera camera) {
        if (!ManateeScanner.barcodeScannerTaskLock) {
            ManateeScanner.barcodeScannerTaskLock = true;
            new ReaderAsyncTask(camera, data).execute();
        }
    }
    private class ReaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private byte[] imageData;
        private final Camera camera;

        ReaderAsyncTask(Camera camera, byte[] imageData) {
            this.camera = camera;
            this.imageData = imageData;
        }

        @Override
        protected Void doInBackground(Void... ignored) {
            if (isCancelled()) {
                ManateeScanner.barcodeScannerTaskLock = false;
                return null;
            }

            Camera.Size size = camera.getParameters().getPreviewSize();

            int width = size.width;
            int height = size.height;
            BarcodeScanner.MWResult mwResult = null;
            byte[] rawResult = BarcodeScanner.MWBscanGrayscaleImage(imageData, width,
                    height);
            if (rawResult != null
                    && BarcodeScanner.MWBgetResultType() == BarcodeScanner.MWB_RESULT_TYPE_MW) {

                BarcodeScanner.MWResults results = new BarcodeScanner.MWResults(
                        rawResult);

                if (results.count > 0) {
                    mwResult = results.getResult(0);
                    rawResult = mwResult.bytes;
                }

            } else if (rawResult != null
                    && BarcodeScanner.MWBgetResultType() == BarcodeScanner.MWB_RESULT_TYPE_RAW) {
                mwResult = new BarcodeScanner.MWResult();
                mwResult.bytes = rawResult;
                mwResult.text = rawResult.toString();
                mwResult.type = BarcodeScanner.MWBgetLastType();
                mwResult.bytesLength = rawResult.length;
            }
            if (mwResult != null) {
                handleDecode(mwResult);
            }
            ManateeScanner.barcodeScannerTaskLock = false;
            return null;
        }


        public void handleDecode(BarcodeScanner.MWResult result) {

            byte[] rawResult = null;

            if (result != null && result.bytes != null) {
                rawResult = result.bytes;
            }

            String s = "";
		/* Parser */
		/*
		 * Parser result handler. Edit this code for custom handling of the
		 * parser result. Use MWParser.MWPgetJSON(MWPARSER_MASK,
		 * result.encryptedResult.getBytes()); to get JSON formatted result
		 */
		/*
		 * if (USE_MWPARSER && MWPARSER_MASK != MWParser.MWP_PARSER_MASK_NONE &&
		 * BarcodeScanner.MWBgetResultType() ==
		 * BarcodeScanner.MWB_RESULT_TYPE_MW) {
		 *
		 * s = MWParser.MWPgetFormattedText(MWPARSER_MASK,
		 * result.encryptedResult.getBytes()); if (s == null) { String
		 * parserMask = "parser"; switch (MWPARSER_MASK) { case
		 * MWParser.MWP_PARSER_MASK_AAMVA: parserMask = "AAMVA"; break; case
		 * MWParser.MWP_PARSER_MASK_ISBT: parserMask = "ISBT"; break; case
		 * MWParser.MWP_PARSER_MASK_IUID: parserMask = "IUID"; break; case
		 * MWParser.MWP_PARSER_MASK_HIBC: parserMask = "HIBC"; break;
		 *
		 * default: break; } s = result.text + "\n*Not a valid " + parserMask +
		 * " formatted barcode"; } } else {
		 */
            try {
                s = new String(rawResult, "UTF-8");
            } catch (UnsupportedEncodingException e) {

                s = "";
                for (int i = 0; i < rawResult.length; i++)
                    s = s + (char) rawResult[i];
                e.printStackTrace();
            }
		/* Parser */
		/*
		 * }
		 */

            int bcType = result.type;
            String typeName = "";
            switch (bcType) {
                case BarcodeScanner.FOUND_25_INTERLEAVED:
                    typeName = "Code 25 Interleaved";
                    break;
                case BarcodeScanner.FOUND_25_STANDARD:
                    typeName = "Code 25 Standard";
                    break;
                case BarcodeScanner.FOUND_128:
                    typeName = "Code 128";
                    break;
                case BarcodeScanner.FOUND_39:
                    typeName = "Code 39";
                    break;
                case BarcodeScanner.FOUND_93:
                    typeName = "Code 93";
                    break;
                case BarcodeScanner.FOUND_AZTEC:
                    typeName = "AZTEC";
                    break;
                case BarcodeScanner.FOUND_DM:
                    typeName = "Datamatrix";
                    break;
                case BarcodeScanner.FOUND_EAN_13:
                    typeName = "EAN 13";
                    break;
                case BarcodeScanner.FOUND_EAN_8:
                    typeName = "EAN 8";
                    break;
                case BarcodeScanner.FOUND_NONE:
                    typeName = "None";
                    break;
                case BarcodeScanner.FOUND_RSS_14:
                    typeName = "Databar 14";
                    break;
                case BarcodeScanner.FOUND_RSS_14_STACK:
                    typeName = "Databar 14 Stacked";
                    break;
                case BarcodeScanner.FOUND_RSS_EXP:
                    typeName = "Databar Expanded";
                    break;
                case BarcodeScanner.FOUND_RSS_LIM:
                    typeName = "Databar Limited";
                    break;
                case BarcodeScanner.FOUND_UPC_A:
                    typeName = "UPC A";
                    break;
                case BarcodeScanner.FOUND_UPC_E:
                    typeName = "UPC E";
                    break;
                case BarcodeScanner.FOUND_PDF:
                    typeName = "PDF417";
                    break;
                case BarcodeScanner.FOUND_QR:
                    typeName = "QR";
                    break;
                case BarcodeScanner.FOUND_CODABAR:
                    typeName = "Codabar";
                    break;
                case BarcodeScanner.FOUND_128_GS1:
                    typeName = "Code 128 GS1";
                    break;
                case BarcodeScanner.FOUND_ITF14:
                    typeName = "ITF 14";
                    break;
                case BarcodeScanner.FOUND_11:
                    typeName = "Code 11";
                    break;
                case BarcodeScanner.FOUND_MSI:
                    typeName = "MSI Plessey";
                    break;
                case BarcodeScanner.FOUND_25_IATA:
                    typeName = "IATA Code 25";
                    break;
            }

            if (result.isGS1) {
                typeName += " (GS1)";
            }


            if (bcType >= 0) {
                ReactContext reactContext = RCTCameraModule.getReactContextSingleton();
                WritableMap event = Arguments.createMap();
                event.putString("code", s);
                event.putString("type", typeName);
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("CameraBarCodeReadAndroid", event);
            }
        }
    }

}
