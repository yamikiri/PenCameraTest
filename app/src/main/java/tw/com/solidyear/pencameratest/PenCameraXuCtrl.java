package tw.com.solidyear.pencameratest;

import android.hardware.usb.*;
import android.util.Log;
/**
 * Created by tylor on 2017/2/18.
 */

public class PenCameraXuCtrl {

    private static final String myTAG = "PenCameraXuCtrl";

    private static final int REQ_TYPE_SET = 0x21;
    private static final int REQ_TYPE_GET = 0xa1;
    private static final int UVC_RC_UNDEFINED = 0x00;
    private static final int UVC_SET_CUR = 0x01;
    private static final int UVC_GET_CUR = 0x81;
    private static final int UVC_GET_MIN = 0x82;
    private static final int UVC_GET_MAX = 0x83;
    private static final int UVC_GET_RES = 0x84;
    private static final int UVC_GET_LEN = 0x85;
    private static final int UVC_GET_INFO = 0x86;
    private static final int UVC_GET_DEF = 0x87;

    private static final int UVC_XU_CTRL_ID = 0x06;
    private static final int AUV_XU_EXT_ROM = 0x03;
    private static final int AUV_XU_REG = 0x06;
    private static final int AUV_XU_REG_SIZE = 11;

    private byte bFlag;
    private byte bAddrHi;
    private byte bAddrLo;

    private UsbDeviceConnection mDc;
    private UsbInterface mUsbInterface;
    private boolean mInterfaceCalimed;

    private boolean claimInterface()
    {
        if (mDc == null)
            return false;

        if (!mInterfaceCalimed) {
            mInterfaceCalimed = mDc.claimInterface(mUsbInterface, true);
            Log.d(myTAG, "claim interface: " + mInterfaceCalimed);
        }

        return true;
    }

    public PenCameraXuCtrl(UsbDeviceConnection dc, UsbDevice device) {
        Log.d(myTAG, "PenCameraXuCtrl");
        if (dc != null) {
            mDc = dc;
            mUsbInterface = device.getInterface(0);
            claimInterface();
        } else {
            Log.e(myTAG, "UsbDeviceConnection is null");
        }
    }

    private int UvcXuCtrlQuery(int bUnit, int bSelector, int bSize, int bUvcReqCode, byte[] pData) {
        claimInterface();

        byte len[] = {0, 0};
        int reqType = REQ_TYPE_GET;

//        if (mDc.controlTransfer(reqType, UVC_GET_LEN, bUnit << 8, bSelector << 8, len, 2, 0) < 0) {
//            Log.e(myTAG, "GET_LEN: transfer error!");
//            return -1;
//        } else {
//            Log.d(myTAG, "package len: " + len[0]);
//        }

        if (bUvcReqCode == UVC_SET_CUR) {
            reqType = REQ_TYPE_SET;
        }

        int ret = mDc.controlTransfer(reqType, bUvcReqCode, bSelector << 8, bUnit << 8, pData, bSize, 0);
        if (ret < 0) {
            Log.e(myTAG, "Control: transfer error!");
            return -1;
        } else {
            if (bUvcReqCode == UVC_SET_CUR) {
                Log.d(myTAG, "Set data success(" + ret + " bytes)");
            } else {
                Log.d(myTAG, "Get data success(" + ret + " bytes)");
//                Log.d(myTAG, "Data: " + pData[0] + " " +
//                        pData[1] + " " +
//                        pData[2] + " " +
//                        pData[3] + " " +
//                        pData[4] + " " +
//                        pData[5] + " " +
//                        pData[6] + " " +
//                        pData[7] + " " +
//                        pData[8] + " " +
//                        pData[9] + " " +
//                        pData[10]);
            }
        }

        return ret;
    }

    public boolean getHotkeyStatus()
    {
        byte pData[] = { (byte)0x01, 0x0B, (byte)0x8A, 0, 0, 0, 0, 0, 0, 0, 0 };
        int ret = 0;

        ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_SET_CUR, pData);
        if(ret < 0)
            return false;

        UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_GET_CUR, pData);
        if(ret < 0)
            return false;

        Log.d(myTAG, "Button: " + pData[3]);
        if (pData[3] == (byte)1)
            return true;
        else
            return false;
    }

    private boolean setLedCode(byte code)
    {
        byte pData[] = { (byte)0x81, 0x0B, (byte)0x89, code, 0, 0, 0, 0, 0, 0, 0 };
        int ret = 0;

        ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_SET_CUR, pData);
        if(ret < 0)
            return false;
        else
            return true;
    }

    public boolean setLedOn()
    {
        return setLedCode((byte)0x00);
    }

    public boolean setLedLink()
    {
        return setLedCode((byte)0x01);
    }

    public boolean setLedScan()
    {
        return setLedCode((byte)0x02);
    }

    public boolean setLedFail()
    {
        return setLedCode((byte)0x03);
    }

    public boolean setLedOff()
    {
        return setLedCode((byte)0x04);
    }

    public boolean getSnapshot()
    {
        byte pData[] = { (byte)0x01, 0x0B, (byte)0x8B, 0, 0, 0, 0, 0, 0, 0, 0 };
        int ret = 0;

        ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_SET_CUR, pData);
        if(ret < 0)
            return false;

        UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_GET_CUR, pData);
        if(ret < 0)
            return false;

        Log.d(myTAG, "Button: " + pData[3]);
        if (pData[3] == (byte)1)
            return true;
        else
            return false;
    }
    //?
    public boolean setSnapshot(int iCode)
    {
        byte code = (byte)iCode;
        byte pData[] = { (byte)0x81, 0x0B, (byte)0x8B, code, 0, 0, 0, 0, 0, 0, 0 };
        int ret = 0;

        ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_REG, 11, UVC_SET_CUR, pData);
        if(ret < 0)
            return false;
        else
            return true;
    }

    public String getUIDS()
    {
        byte buf[] = new byte[128];
        int offset = 0;
        int size = buf.length - 1;
        int ret = 0;

        while(offset < size) {
            byte packetLen = ((size - offset) > 8) ? (byte)0x08 : (byte)(size - offset);
            byte HiAddr = (byte)(offset / 0x100);
            byte LoAddr = (byte)(offset % 0x100);

            byte pData[] = { packetLen, (byte)((byte)0x7F + HiAddr), (byte)((byte)0x80 + LoAddr), 0, 0, 0, 0, 0, 0, 0, 0 };

            ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_EXT_ROM, 11, UVC_SET_CUR, pData);

            if(ret < 0)
                return null;

            ret = UvcXuCtrlQuery(UVC_XU_CTRL_ID, AUV_XU_EXT_ROM, 11, UVC_GET_CUR, pData);

            if(ret < 0)
                return null;

            System.arraycopy(pData, 3, buf, 0 + offset, packetLen);

            offset = offset + packetLen;
        }
        String uid = new String(buf);
        uid = uid.replaceAll("[^A-Za-z0-9_*&%$#@!~()\\\\^\\\\[\\\\]]", "");

        return uid;
    }
}
