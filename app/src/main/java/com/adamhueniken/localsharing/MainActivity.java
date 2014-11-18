package com.adamhueniken.localsharing;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.util.Calendar;


public class MainActivity extends Activity {

    private WebView webView;
    PendingIntent mPintent;
    Intent mIntent;
    AlarmManager mAlarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://local-experiences.herokuapp.com/actions/show");

        mIntent = new Intent(this, CurrentAppService.class);
      	mPintent = PendingIntent.getService(this, 0, mIntent, 0);

        Calendar cal = Calendar.getInstance();
        mAlarm = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
        				3000, mPintent);

        Log.i(Constants.TAG, "app launched");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
}
