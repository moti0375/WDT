package com.wdt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "WDT";
    private static final UUID uuid = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");

    private Context context;
    private Button upButton, downButton, plusButton,
            minusButton;

    private TextView output, output2;
    private ProgressDialog progressDialog;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    Toolbar mToolbar;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Starting BT discovery");
                progressDialog = ProgressDialog.show(context,
                        "Scanning for BT devices", "Please wait...", true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                Log.d(TAG, "BT discovery finished");
                progressDialog.dismiss();
                showScanResults(false);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found BT device: " + device.getName());
                devices.add(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        context = this;
        upButton = (Button) findViewById(R.id.bUp);
        upButton.setOnClickListener(this);
        downButton = (Button) findViewById(R.id.bDown);
        downButton.setOnClickListener(this);
        plusButton = (Button) findViewById(R.id.bPlus);
        plusButton.setOnClickListener(this);
        minusButton = (Button) findViewById(R.id.bMinus);
        minusButton.setOnClickListener(this);
        output = (TextView) findViewById(R.id.tvLine1);
//        output.setText("");
        output.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        output2 = (TextView) findViewById(R.id.tvLine2);
//        output2.setText("");
        output2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

//        cbMode = (CheckBox) findViewById(R.id.cbAppMode);
//        cbMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    buttonView.setText("Xicoy V10");
//                } else {
//                    buttonView.setText("Xicoy V6");
//                }
//            }
//        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Your Device does not support BT!!!",
                    Toast.LENGTH_LONG).show();
            //   finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_search_device){
            devices.clear();
            // List paired devices before scanning
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                     devices.add(device);
                }
                showScanResults(true);
            } else {
                scanForBTDevices();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bUp:
                if (mConnectedThread != null) {
                    byte[] raw = {(byte) 0xde, (byte) 0xdf, (byte) 0x70,
                            (byte) 0x43, (byte) 0xb3};
                    mConnectedThread.write(raw);
                }
                break;

            case R.id.bDown:
                if (mConnectedThread != null) {
                    byte[] raw = {(byte) 0xde, (byte) 0xdf, (byte) 0x70,
                            (byte) 0x44, (byte) 0xb4};
                    mConnectedThread.write(raw);
                }
                break;

            case R.id.bMinus:
                if (mConnectedThread != null) {
                    byte[] raw = {(byte) 0xde, (byte) 0xdf, (byte) 0x70,
                            (byte) 0x42, (byte) 0xb2};
                    mConnectedThread.write(raw);
                }
                break;

            case R.id.bPlus:
                if (mConnectedThread != null) {
                    byte[] raw = {(byte) 0xde, (byte) 0xdf, (byte) 0x70,
                            (byte) 0x41, (byte) 0xb1};
                    mConnectedThread.write(raw);
                }
                break;
        }
    }

    private void showScanResults(boolean scan) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        if (scan == false) {
            builderSingle.setTitle("Select BT Device:");
        } else {
            builderSingle.setTitle("Select from paired devices:");
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.select_dialog_singlechoice);
        for (BluetoothDevice d : devices) {
            arrayAdapter.add(d.getName());
        }

        if (scan == true) {
            builderSingle.setPositiveButton("Search more",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            scanForBTDevices();
                        }
                    });
        }

        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // chosenDevice = devices.get(which);
                        mConnectThread = new ConnectThread(devices.get(which));
                        mConnectThread.start();
                    }
                });
        builderSingle.show();
    }

    private void scanForBTDevices() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);

        registerReceiver(mReceiver, filter);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG,
                    "Error unregistering broadcast receiver:" + e.getMessage());
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter = null;
    }

    private void startThreadConnected(BluetoothSocket socket) {

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    public class ConnectThread extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;

        private ConnectThread(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = bluetoothDevice
                        .createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.d(TAG, "Error creating socket", e);
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                Log.d(TAG, "Error connecting to socket", e);
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    Log.d(TAG, "Error closing socket", e);
                }
            }

            if (success) {
                startThreadConnected(bluetoothSocket);
            } else {
                // fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(), "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Error closing socket", e);
            }

        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        private boolean send = false;

        private byte[] bufferOut;

        public ConnectedThread(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "Error creating temp streams", e);
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            ArrayList<Integer> buffer = new ArrayList<Integer>();

            while (connectedBluetoothSocket.isConnected()) {
                try {
                    int a = connectedInputStream.read();
                    if (a == (int) 0xfc) {
                        int b = connectedInputStream.read();
                        if (b == (int) 0xfd) {
                            sleep(100);
                            if (send) {
                                connectedOutputStream.write(bufferOut);
                                send = false;
                            }
                            int[] buffer2 = new int[buffer.size()];

                            StringBuilder sb = new StringBuilder();
                            StringBuilder sb2 = new StringBuilder();
                            for (int i = 0; i < buffer.size(); i++) {

//                                if (cbMode.isChecked()) {
//                                    Log.d(TAG, "Xicoy V10");
//                                    buffer2[i] = ((buffer.get(i) ^ (int) 0xff) + buffer
//                                            .get(33));
//                                } else {
//                                }

                                Log.d(TAG, "Xicoy V6");
                                buffer2[i] = (buffer.get(i));


                                buffer2[i] = buffer2[i] & (int) 0xff;

                                if (buffer2[i] == (int) 223) {
                                    buffer2[i] = (int) 176;
                                }
                            }
                            if (buffer2.length > 40) {
                                for (int i = 0; i < 16; i++) {

                                    sb.append((char) buffer2[i]);
                                    sb2.append((char) buffer2[i + 16]);
                                }
                            }
                            final String s = sb.toString();
                            final String s2 = sb2.toString();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    output.setText(s);
                                    output2.setText(s2);
                                }
                            });
                            buffer.clear();
                        } else {
                            buffer.add(a);
                            buffer.add(b);
                        }
                    } else {
                        buffer.add(a);
                    }
                } catch (IOException e) {
                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    Log.d(TAG, msgConnectionLost);
                } catch (Exception ex) {
                    //do nothing
                }
            }
        }

        public void write(byte[] buffer) {
            /*try {
                connectedOutputStream.write(buffer);
			} catch (IOException e) {
				Log.d(TAG, "Error wrirting message to stream", e);
			}*/
            bufferOut = buffer;
            send = true;
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "Error closing socket", e);
            }
        }
    }
}
