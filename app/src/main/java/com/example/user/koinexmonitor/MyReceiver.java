package com.example.user.koinexmonitor;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent responseIntent) {
        Log.i("INFO", "In the notify receiver " + context);

        String response = responseIntent.getStringExtra("response");
        if (response == null) return;

        String bitcoinCurrentValue=null, rippleCurrentValue=null, ethereumCurrentValue = null;
        try {
            JSONObject object = new JSONObject(response);
            bitcoinCurrentValue = object.getJSONObject("prices").getString("BTC");
            rippleCurrentValue = object.getJSONObject("prices").getString("XRP");
            ethereumCurrentValue = object.getJSONObject("prices").getString("ETH");

        }
        catch (JSONException e) {
            Log.i("INFO", response);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, "2")
                        .setSmallIcon(R.drawable.ic_stat_attach_money)
                        .setContentTitle("Check out the prices")
                        .setContentText("BTC " + bitcoinCurrentValue + " | XRP " + rippleCurrentValue + " | ETH " + ethereumCurrentValue);

        int mNotificationId = 1;

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (mNotifyMgr != null) {
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
