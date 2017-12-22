package com.example.dvorski.garagedooropener;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String HC_05_MAC = "98:D3:31:40:16:E7";
    //private final static String HC_05_MAC = "F4:06:69:91:9D:20";
    private static BluetoothAdapter mBluetoothAdapter = null;
    private static ConnectedThread mStreamThread = null;
    private static BtThread mBtThread = null;
    private static Handler mHandler = null;
    private static Handler ctHandler = null;
    private ProgressDialog connProgressDialog;
    private boolean wasBtEnabled = true;
    private static final int PROGRESS = 0x1;
    private GraphView graph;
    private LineGraphSeries mSeries;
    private int curr2 = 0;
    private int xValue = 0;
    private static final int MIN_Y = 500;
    private static final int MAX_Y = 750;
    private static final int MIN_X = 0;
    private static final int MAX_X = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateGraph();

        enableAndConnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnected();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToBluetoothDevice();
            }
        }
    }

    private void checkConnected() {
        Button button = (Button) findViewById(R.id.button4);
        button.setBackgroundColor(Color.RED);
        if (null != mStreamThread)
        {
            if (mStreamThread.isConnected())
            {
                button.setBackgroundColor(Color.LTGRAY);
            }
        }
    }

    public String getMacAddress() {
        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String macAddress = sharedPref.getString(getString(R.string.mac_address), "");
        if (macAddress.matches("([\\da-fA-F]{2}(?:\\:|$)){6}")){
            return macAddress;
        }
        return HC_05_MAC;
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
    protected void onDestroy(){
        super.onDestroy();
        mBtThread.cancel();
        mStreamThread.cancel();
    }

    /** Enables the bluetooth if not enabled and connects to the device */
    public void enableAndConnect() {
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
                checkConnected();
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
                        mSeries.appendData(new DataPoint(xValue,curr2), false, MAX_X, false);
                        xValue += 1;
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

    /** Connects to bluetooth */
    public void connectToBluetoothDevice(){
        mBtThread = new BtThread(mBluetoothAdapter,getMacAddress(),mHandler,ctHandler);
        mBtThread.start();
    }

    /** Called when the user clicks the Open button */
    public void openDoor(View view) {
        resetSeries();
        String message = "{c:ope}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user clicks the Close button */
    public void closeDoor(View view) {
        resetSeries();
        String message = "{c:clo}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user clicks the Stop button */
    public void stopDoor(View view){
        resetSeries();
        String message = "{c:sto}";
        if(mStreamThread != null){
            mStreamThread.write(message);
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user hits the Reconnect button */
    public void reconnect(View view) {
        connectToBluetoothDevice();
    }

    private void resetSeries() {
        xValue = 0;
        graph.removeAllSeries();
        mSeries = new LineGraphSeries<>();
        mSeries.setThickness(2);
        graph.addSeries(mSeries);
    }

    private void initiateGraph() {
        graph = (GraphView) findViewById(R.id.graph);
        resetSeries();
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(MIN_Y);
        graph.getViewport().setMaxY(MAX_Y);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(MIN_X);
        graph.getViewport().setMaxX(MAX_X);
    }

    public static ConnectedThread getConThread(){
        return mStreamThread;
    }
}
