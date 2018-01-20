package com.example.user.koinexmonitor;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;


public class BackgroundService extends IntentService {

    public BackgroundService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
