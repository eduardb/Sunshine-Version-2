package com.example.android.sunshine.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    public static final String TAG = "RegistrIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            synchronized (TAG) {
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                sendRegistrationToServer(token);

                sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
        }
    }

    /**
     * Normally, you would want to persist the registration to third-party servers. Because we do
     * not have a server, and are faking it with a website, you'll want to log the token instead.
     * That way you can see the value in logcat, and note it for future use in the website.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Log.i(TAG, "GCM Registration Token: " + token);
    }
}
