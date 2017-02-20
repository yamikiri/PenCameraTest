package tw.com.solidyear.pencameratest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String myTAG = "penCameraTestApp";
    private static final String ACTION_USB_PERMISSION =
            "tw.com.solidyear.pencameratest.USB_PERMISSION";
    private static final int SolidyearVID = 0x60B;
    private static final int SolidyearPID = 0x8074;
    IntentFilter filterAttached_and_Detached = null;
    UsbManager mUsbManager = null;
    UsbInterface mUsbInterface = null;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(myTAG, "onReceive: " + action);
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {
                        Log.d(myTAG, "DEATTCHED-" + device);
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(myTAG, "ATTACHED-" + device);
                        }
                    } else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    Log.i(myTAG, "called");
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    mUsbInterface = device.getInterface(0);
                    Log.d(myTAG, "bInterfaceNumber: " + mUsbInterface.getId() + "\n" +
                            "EndpointCount: " + mUsbInterface.getEndpointCount() + "\n" +
                            "bInterfaceClass: " + mUsbInterface.getInterfaceClass() + "\n" +
                            "bInterfaceSubClass: " + mUsbInterface.getInterfaceSubclass() + "\n" +
                            "bInterfaceProtocol: " + mUsbInterface.getInterfaceProtocol() + "\n" +
                            "%s" + mUsbInterface.toString());
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d(myTAG, "PERMISSION-" + device);

                        try {
                            UsbDeviceConnection dc;
                            dc = mUsbManager.openDevice(device);

                            if (dc != null) {
                                Log.d(myTAG, "Usb Device Connection acquired.");
                                PenCameraXuCtrl xu = new PenCameraXuCtrl(dc, device);

                                boolean hotkey = xu.getHotkeyStatus();

                                String uid = "UID: " + xu.getUIDS();
                                TextView tvUid = (TextView) findViewById(R.id.uid);
                                tvUid.setText(uid);
                                Log.d(myTAG, "uid: " + xu.getUIDS());

                                TextView tvHotkey = (TextView) findViewById(R.id.hotkeyStatus);
                                if (hotkey) {
                                    tvHotkey.setText("Hotkey: pressed");
                                } else {
                                    tvHotkey.setText("Hotkey: released");
                                }
                            }
                        } catch (Exception e) {
                            Log.d(myTAG, "exception: " + e);
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(myTAG, deviceList.size() + " USB device(s) found");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            if (device.getVendorId() == SolidyearVID && device.getProductId() == SolidyearPID) {
                Log.d(myTAG, "found PenCamera!");
                synchronized (this) {
                    PendingIntent mPermissionIntent;
                    mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            }
        }
    }
}
