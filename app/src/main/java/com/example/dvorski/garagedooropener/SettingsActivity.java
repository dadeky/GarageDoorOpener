package com.example.dvorski.garagedooropener;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends ActionBarActivity {

    private SeekBar seekBarSpeedUp;
    private TextView textViewSpeedUp;

    private SeekBar seekBarSpeedDown;
    private TextView textViewSpeedDown;

    private SeekBar seekBarMaxCurrentUp;
    private TextView textViewMaxCurrentUp;

    private SeekBar seekBarMaxCurrentDown;
    private TextView textViewMaxCurrentDown;

    private static ConnectedThread mStreamThr = null;
    private static Handler socketCommHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mStreamThr = MainActivity.getConThread();
        //handler that processes the messages arriving from the thread
        socketCommHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // get the bundle and extract data by key
                Bundle b = msg.getData();
                String jsonString = b.getString("message");
                /*{
                "ard_settings":
                   {
                      "speedUp":"144",
                      "speedDown":"200",
                      "maxCurrentUp":"512",
                      "maxCurrentDown":"758"
                   }
                }*/
                try{
                    JSONObject reader = new JSONObject(jsonString);
                    JSONObject sys  = reader.getJSONObject("ard_settings");
                    String speedUp = sys.getString("speedUp");
                    String speedDown = sys.getString("speedDown");
                    String maxCurrentUp = sys.getString("maxCurrentUp");
                    String maxCurrentDown = sys.getString("maxCurrentDown");
                    seekBarSpeedUp.setProgress(Integer.parseInt(speedUp));
                    textViewSpeedUp.setText("Brzina otvaranja vrata: " + speedUp + "/" + seekBarSpeedUp.getMax());
                    seekBarSpeedDown.setProgress(Integer.parseInt(speedDown));
                    textViewSpeedDown.setText("Brzina zatvaranja vrata: " + speedDown + "/" + seekBarSpeedDown.getMax());
                    seekBarMaxCurrentUp.setProgress(Integer.parseInt(maxCurrentUp));
                    textViewMaxCurrentUp.setText("Max. protok struje otvaranja vrata: " + maxCurrentUp + "/" + seekBarMaxCurrentUp.getMax());
                    seekBarMaxCurrentDown.setProgress(Integer.parseInt(maxCurrentDown));
                    textViewMaxCurrentDown.setText("Max. protok struje zatvaranja vrata: " + maxCurrentDown + "/" + seekBarMaxCurrentDown.getMax());
                }catch (JSONException jex){}
            }
        };
        //the thread in charge with serial bluetooth communication
        if(mStreamThr != null){
            mStreamThr.setMmHandler(socketCommHandler);
        }
        initializeVariables();

        /* speed Up slider */
        textViewSpeedUp.setText("Brzina otvaranja vrata: " + seekBarSpeedUp.getProgress() + "/" + seekBarSpeedUp.getMax());

        seekBarSpeedUp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewSpeedUp.setText("Brzina otvaranja vrata: " + progress + "/" + seekBar.getMax());
                //slanje json arraya u arduino preko bluetootha...
                String sendString = "{s:spu:"+progress+"}";
                if(mStreamThr != null){
                    mStreamThr.write(sendString);
                }else{
                    Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*************************************************/

        /* speed Down slider */
        textViewSpeedDown.setText("Brzina zatvaranja vrata: " + seekBarSpeedDown.getProgress() + "/" + seekBarSpeedDown.getMax());

        seekBarSpeedDown.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewSpeedDown.setText("Brzina zatvaranja vrata: " + progress + "/" + seekBar.getMax());
                //slanje json arraya u arduino preko bluetootha...
                String sendString = "{s:spd:"+progress+"}";
                if(mStreamThr != null){
                    mStreamThr.write(sendString);
                }else{
                    Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*************************************************/

        /* max Current Up slider */
        textViewMaxCurrentUp.setText("Max. protok struje otvaranja vrata: " + seekBarMaxCurrentUp.getProgress() + "/" + seekBarMaxCurrentUp.getMax());

        seekBarMaxCurrentUp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewMaxCurrentUp.setText("Max. protok struje otvaranja vrata: " + progress + "/" + seekBar.getMax());
                //slanje json arraya u arduino preko bluetootha...
                String sendString = "{s:mcu:"+progress+"}";
                if(mStreamThr != null){
                    mStreamThr.write(sendString);
                }else{
                    Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*************************************************/

        /* max Current Down slider */
        textViewMaxCurrentDown.setText("Max. protok struje zatvaranja vrata: " + seekBarMaxCurrentDown.getProgress() + "/" + seekBarMaxCurrentDown.getMax());

        seekBarMaxCurrentDown.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewMaxCurrentDown.setText("Max. protok struje zatvaranja vrata: " + progress + "/" + seekBar.getMax());
                //slanje json arraya u arduino preko bluetootha...
                String sendString = "{s:mcd:"+progress+"}";
                if(mStreamThr != null){
                    mStreamThr.write(sendString);
                }else{
                    Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*************************************************/

    }

    // A private method to help us initialize our variables.
    private void initializeVariables() {
        seekBarSpeedUp = (SeekBar) findViewById(R.id.seekBarSpeedUp);
        textViewSpeedUp = (TextView) findViewById(R.id.textViewSpeedUp);

        seekBarSpeedDown = (SeekBar) findViewById(R.id.seekBarSpeedDown);
        textViewSpeedDown = (TextView) findViewById(R.id.textViewSpeedDown);

        seekBarMaxCurrentUp = (SeekBar) findViewById(R.id.seekBarMaxCurrentUp);
        textViewMaxCurrentUp = (TextView) findViewById(R.id.textViewMaxCurrentUp);

        seekBarMaxCurrentDown = (SeekBar) findViewById(R.id.seekBarMaxCurrentDown);
        textViewMaxCurrentDown = (TextView) findViewById(R.id.textViewMaxCurrentDown);

        if(mStreamThr != null){
            mStreamThr.write("{g}");
        }else{
            Toast.makeText(getBaseContext(), "Nema bluetooth konekcije.", Toast.LENGTH_SHORT).show();
        }

    }

}
