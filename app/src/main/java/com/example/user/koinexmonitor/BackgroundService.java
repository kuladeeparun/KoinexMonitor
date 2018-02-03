package com.example.user.koinexmonitor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BackgroundService extends IntentService {

    public static final int REQUEST_CODE = 12345;

    public BackgroundService(){
        super("BackgroundService");
    }


    String response = null;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i("INFO", "In the service " + getApplicationContext());

        try {
            URL url = new URL(MainActivity.API_URL);
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

        //LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        Intent returnIntent = new Intent();
        returnIntent.setAction("KOINEX_API_FETCH");
        returnIntent.putExtra("response", response);
        sendBroadcast(returnIntent);

    }
}
