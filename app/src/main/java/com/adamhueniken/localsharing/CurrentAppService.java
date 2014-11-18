package com.adamhueniken.localsharing;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam Hueniken on 11/18/2014.
 */
public class CurrentAppService extends IntentService {

    public CurrentAppService() {
        super(null);
        Log.i(Constants.TAG, "Service is being constructed");
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        Log.i(Constants.TAG, "Intent Starting");

        String dataString = workIntent.getDataString();

        ActivityManager am = (ActivityManager) this
                .getSystemService(Activity.ACTIVITY_SERVICE);
        final String packageName = am.getRunningTasks(1).get(0).topActivity
                .getPackageName();

        String[] strings = packageName.split("\\.");
        String appName = strings[strings.length - 1];
        postData(appName, packageName, Constants.ShareURL);
    }
    public void postData(String appName, String description, String url) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("title", appName));
            nameValuePairs.add(new BasicNameValuePair("description", description));
            nameValuePairs.add(new BasicNameValuePair("customer_id", "1"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            //Log.i(Constants.TAG, EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(Constants.TAG, e.getMessage());
        }
    }

}