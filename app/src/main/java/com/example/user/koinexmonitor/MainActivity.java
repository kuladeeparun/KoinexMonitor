package com.example.user.koinexmonitor; 


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    TextView bitcoinValue, rippleValue, ethereumValue;
    static final String API_URL = "https://koinex.in/api/ticker";
    BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitcoinValue = findViewById(R.id.bitcoinValue);
        rippleValue = findViewById(R.id.rippleValue);
        ethereumValue = findViewById(R.id.ethereumValue);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("INFO", "In the updateUI receiver " + context);
                updateUI(intent.getStringExtra("response"));
            }
        };

        //receiver = (c,i) -> Log.i("INFO", "In the receiver " + c);

        scheduleAlarm();

        Button queryButton = findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getApplicationContext(), BackgroundService.class));
                Log.i("INFO", "In the main activity "+getApplicationContext());
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        //LocalBroadcastManager.getInstance(this).
                registerReceiver(receiver, new IntentFilter("KOINEX_API_FETCH")
        );
    }

    @Override
    protected void onStop() {
        //LocalBroadcastManager.getInstance(this).
                unregisterReceiver(receiver);
        super.onStop();
    }

    public void scheduleAlarm() {

        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        final PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), BackgroundService.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
        }
    }

    public void updateUI(String response){
        if(response == null) {
            Toast.makeText(getApplicationContext(), "Data fetch failed", Toast.LENGTH_LONG).show();
            return;
        }

        String bitcoinCurrentValue=null, rippleCurrentValue=null, ethereumCurrentValue = null;
        try {
            JSONObject object = new JSONObject(response);
            bitcoinCurrentValue = object.getJSONObject("prices").getString("BTC");
            rippleCurrentValue = object.getJSONObject("prices").getString("XRP");
            ethereumCurrentValue = object.getJSONObject("prices").getString("ETH");

            bitcoinValue.setText(bitcoinCurrentValue);
            rippleValue.setText(rippleCurrentValue);
            ethereumValue.setText(ethereumCurrentValue);
        }
        catch (JSONException e) {
            Log.i("INFO", response);
        }


    }
    /*public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent responseIntent) {
            Log.i("INFO", "In the receiver " + context);

            //updateAndNotify(responseIntent.getStringExtra("response"));
        }
    }*/
}

