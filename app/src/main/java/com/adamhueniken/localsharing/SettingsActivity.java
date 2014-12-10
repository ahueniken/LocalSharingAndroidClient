package com.adamhueniken.localsharing;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SettingsActivity extends Activity implements View.OnClickListener {
    private String mId;
    private String mEmail;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText editText = (EditText) findViewById(R.id.idSetting);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        mId = settings.getString("id", "1");
        editText.setText(mId);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count)
            {
                mId = s.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        EditText emailEditText = (EditText) findViewById(R.id.emailSetting);
        // Restore preferences
        mEmail = settings.getString("email", "");
        emailEditText.setText(mEmail);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count)
            {
                mEmail = s.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        EditText passwordEditText = (EditText) findViewById(R.id.passwordSetting);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        // Restore preferences
        mPassword = settings.getString("password", "");
        passwordEditText.setText(mPassword);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count)
            {
                mPassword = s.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button authButton = (Button) findViewById(R.id.authButton);
        authButton.setOnClickListener(this);

        Button stopBroadcastingButton = (Button) findViewById(R.id.stopBroadcasting);
        stopBroadcastingButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(Constants.TAG, "Stopping Sharing Service");
                Context mContext = getBaseContext();
                Intent mIntent = new Intent(mContext, CurrentAppService.class);
                PendingIntent mPintent = PendingIntent.getService(mContext, 0, mIntent, 0);
                Calendar cal = Calendar.getInstance();
                AlarmManager mAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
                mAlarm.cancel(mPintent);
            }
        });

        Button restartBroadcastingButton = (Button) findViewById(R.id.restartBroadcasting);
        restartBroadcastingButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d(Constants.TAG, "Starting Sharing Service");
                Context mContext = getBaseContext();
                Intent mIntent = new Intent(mContext, CurrentAppService.class);
                PendingIntent mPintent = PendingIntent.getService(mContext, 0, mIntent, 0);
                Calendar cal = Calendar.getInstance();
                AlarmManager mAlarm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
                mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                        3000, mPintent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("id", mId);
        editor.putString("email", mEmail);
        editor.putString("password", mPassword);
        // Commit the edits!
        editor.commit();

        Log.d(Constants.TAG, "Saved mId: " + mId);
    }
    private class GetAuthTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return login();
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("auth_token", result);

            // Commit the edits!
            editor.commit();
            Log.d(Constants.TAG, "Auth token: " + result);
        }
    }

    private String mAuth_token;

    @Override
    public void onClick(View view) {
        GetAuthTokenTask task = new GetAuthTokenTask();
        task.execute(new String[] {"abc"});
    }

    private String login() {
        String username = mEmail;
        String password = mPassword;

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                boolean isRedirect = super.isRedirectRequested(response, context);
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == 301 || responseCode == 302) {
                        return true;
                    }
                }
                return isRedirect;
            }
        });
        HttpPost httppost = new HttpPost("http://local-experiences.herokuapp.com/users/sign_in");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("user[email]", username));
            nameValuePairs.add(new BasicNameValuePair("user[password]", password));
            nameValuePairs.add(new BasicNameValuePair("commit", "Log in"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (header.getName().equals("Auth_token")) {
                    return header.getValue();
                }
            }
            response.getEntity().consumeContent();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(Constants.TAG, e.getMessage());
        }
        return "";
    }
}
