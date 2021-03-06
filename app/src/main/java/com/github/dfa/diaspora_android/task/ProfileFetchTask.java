package com.github.dfa.diaspora_android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import com.github.dfa.diaspora_android.App;
import com.github.dfa.diaspora_android.data.WebUserProfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Gregor Santner (de-live-gdev) on 30.03.16.
 */
public class ProfileFetchTask extends AsyncTask<Void, Void, Void> {
    // Code for getting the profile async without any UI/WebView
    // TODO: This is an early version,needs to be converted to Service

    final App app;
    final Context context;

    public ProfileFetchTask(final App app) {
        this.context = app.getApplicationContext();
        this.app = app;
    }


    @Override
    protected Void doInBackground(Void... params) {
        String extractedProfileData = null;
        final CookieManager cookieManager = app.getCookieManager();
        String cookies = cookieManager.getCookie("https://" + app.getSettings().getPodDomain());
        Log.d(App.TAG, cookies);

        try {
            URL url = new URL("https://" + app.getSettings().getPodDomain() + "/stream");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            if (cookies != null) {
                conn.setRequestProperty("Cookie", cookies);
            }
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            final String TARGET_TAG = "window.gon={};gon.user=";
            while ((line = br.readLine()) != null && !line.startsWith("<body")) {
                if (line.startsWith(TARGET_TAG)) {
                    extractedProfileData = line.substring(TARGET_TAG.length());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (extractedProfileData != null) {
            WebUserProfile profile = new WebUserProfile(app);
            profile.parseJson(extractedProfileData);
            Log.d(App.TAG, "Extracted new_messages (service):"+profile.getUnreadMessagesCount());
        }

        return null;
    }
}
