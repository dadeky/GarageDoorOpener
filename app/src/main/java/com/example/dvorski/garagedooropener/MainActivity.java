package com.example.dvorski.garagedooropener;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String HC_05_MAC = "98:D3:31:40:16:E7";
    //private final static String HC_05_MAC = "00:24:7E:B5:11:F6";
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static ConnectedThread mStreamThread = null;
    private static BtThread mBtThread = null;
    private static Handler mHandler = null;
    private static Handler ctHandler = null;
    private ProgressDialog connProgressDialog;
    private boolean wasBtEnabled = true;
    private static final int PROGRESS = 0x1;
    private ProgressBar mProgress;
    private int curr2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // get the bundle and extract data by key
                Bundle b = msg.getData();
                String text = b.getString("message");
                if(msg.what == 2){
                    connProgressDialog = ProgressDialog.show(MainActivity.this,"Molim pričekajte ...", text, true);
                    connProgressDialog.setCancelable(true);
                }else if (msg.what == 1){
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
                }else if (msg.what == 3){
                    if(connProgressDialog.isShowing()){
                        connProgressDialog.dismiss();
                        mStreamThread = mBtThread.getStreamThread();
                    }
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
                }
            }
        };

        ctHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // get the bundle and extract data by key
                Bundle b = msg.getData();
                String text = b.getString("message");
                try{
                    //{"sensor":{"current":"123"}}
                    JSONObject reader = new JSONObject(text);
                    if(reader.has("sensor")){
                        JSONObject sensorObj  = reader.getJSONObject("sensor");
                        String current = sensorObj.getString("current");
                        curr2 = Integer.parseInt(current);
                        mProgress.setProgress(curr2);
                    }
                }catch (JSONException jex){
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                }
            }
        };

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getBaseContext(), "Vaš uređaj ne podržava bluetooth.", Toast.LENGTH_SHORT).show();
        }else{
            if (!mBluetoothAdapter.isEnabled()) {
                wasBtEnabled = false;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                connectToBluetoothDevice();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            this.closeOptionsMenu();
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToBluetoothDevice();
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(!wasBtEnabled){
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }else{
            if(mStreamThread != null){
                //cancel the thread
                mStreamThread.cancel();
            }
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        if(mStreamThread != null){
            mStreamThread.setMmHandler(ctHandler);
        }
    }

    /** Connects to bluetooth */
    public void connectToBluetoothDevice(){
        mBtThread = new BtThread(mBluetoothAdapter,HC_05_MAC,mHandler,ctHandler);
        mBtThread.start();
    }

    /** Called when the user clicks the Open button */
    public void openDoor(View view) {
        String message = "{c:ope}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user clicks the Close button */
    public void closeDoor(View view) {
        String message = "{c:clo}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user clicks the Stop button */
    public void stopDoor(View view){
        String message = "{c:sto}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    public static ConnectedThread getConThread(){
        return mStreamThread;
    }

}
