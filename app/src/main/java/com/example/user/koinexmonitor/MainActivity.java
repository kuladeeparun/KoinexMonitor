package com.example.user.koinexmonitor; 


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitcoinValue = findViewById(R.id.bitcoinValue);
        rippleValue = findViewById(R.id.rippleValue);
        ethereumValue = findViewById(R.id.ethereumValue);

        /*Button queryButton = findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleAlarm();
            }
        });*/
        scheduleAlarm();
    }


    public void scheduleAlarm() {

        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        final PendingIntent pIntent = PendingIntent.getService(this, BackgroundService.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
        }
    }


    public class BackgroundService extends IntentService {

        public static final int REQUEST_CODE = 12345;

        public BackgroundService(){
            super("No argument");
        }

        public BackgroundService(String name) {
            super(name);
        }

        String response;

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);

                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    response = stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                response = null;
            } catch (IOException e) {
                response =  null;
            }

            updateAndNotify(response);
        }

        public void updateAndNotify(String response){
            if(response == null) {
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

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext(), "2")
                            .setSmallIcon(R.drawable.ic_stat_attach_money)
                            .setContentTitle("Check out the prices")
                            .setContentText("BTC " + bitcoinCurrentValue + " | XRP " + rippleCurrentValue + " | ETH " + ethereumCurrentValue);

            int mNotificationId = 1;

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (mNotifyMgr != null) {
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }
        }
    }

}

