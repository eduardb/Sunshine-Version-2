package com.example.android.sunshine.app.gcm;

import android.app.IntentService;
import android.content.Intent;

public class RegistrationIntentService extends IntentService {

    public static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
