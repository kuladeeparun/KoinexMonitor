package com.example.user.koinexmonitor; 


        import android.app.IntentService;
        import android.app.NotificationManager;
        import android.content.Context;
        import android.content.Intent;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.support.v4.app.NotificationCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ProgressBar;
        import android.widget.Spinner;
        import android.widget.TextView;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;
        import org.json.JSONTokener;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;


public class MainActivity extends AppCompatActivity {

    //EditText emailText;
    TextView responseView;
    TextView bitcoinValue, rippleValue, ethereumValue;
    EditText alertThreshold;
    ProgressBar progressBar;
    Spinner currencies;

    //static final String API_KEY = "USE_YOUR_OWN_API_KEY";
    static final String API_URL = "https://koinex.in/api/ticker";
    //static final String API_URL = "http://www.google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //responseView = findViewById(R.id.responseView);
        //emailText = (EditText) findViewById(R.id.emailText);
        progressBar = findViewById(R.id.progressBar);

        bitcoinValue = findViewById(R.id.bitcoinValue);
        rippleValue = findViewById(R.id.rippleValue);
        ethereumValue = findViewById(R.id.ethereumValue);

        //alertThreshold = findViewById(R.id.alertThreshold);

        //currencies = findViewById(R.id.spinner);
        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencies.setAdapter(adapter);*/

        Button queryButton = findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RetrieveFeedTask().execute();

            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

       // Intent in = new Intent(getApplicationContext(), BackgroundService.class);


        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("Fetching data...");
        }

        protected String doInBackground(Void... urls) {
            //String email = emailText.getText().toString();
            // Do some validation here

            try {
                URL url = new URL(API_URL);// + "email=" + email + "&apiKey=" + API_KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);



                /*
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    connected = true;
                }
                else
                    connected = false;
                */

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
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                return "URLexception";
            } catch (IOException e) {
                return "IOException";
            }

        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);

            //responseView.setText(response);

            String bitcoinValue2=null, rippleValue2=null, ethereumValue2 = null;
            try {
                JSONObject object = new JSONObject(response);
                bitcoinValue2 = object.getJSONObject("prices").getString("BTC");
                rippleValue2 = object.getJSONObject("prices").getString("XRP");
                ethereumValue2 = object.getJSONObject("prices").getString("ETH");

                bitcoinValue.setText(bitcoinValue2);
                rippleValue.setText(rippleValue2);
                ethereumValue.setText(ethereumValue2);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            //double threshold = Double.parseDouble(alertThreshold.getText().toString());
            double currentAmount = Double.parseDouble(rippleValue.getText().toString());

            //if ( (currentAmount < (threshold - 0.15*threshold)) || (currentAmount > (threshold + 0.15*threshold)) ) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext(), "2")
                                .setSmallIcon(R.drawable.ic_stat_attach_money)
                                .setContentTitle("Check out the prices")
                                .setContentText("BTC " + bitcoinValue2 + " | XRP " + rippleValue2 + " | ETH " + ethereumValue2);

                int mNotificationId = 001;

                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            //}
        }
    }

}

