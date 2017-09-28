package com.foo.umbrella.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.foo.umbrella.R;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

      //TODO: put in a background image to fill the Frame Layout with something attractive, yet simple

      //TODO: load cached weather data from last time the app was used (if any), along with a datetime stamp
  }

    @Override
    protected void onStart() {
        super.onStart();

        try{new SplashTask().execute();} catch(Exception e){Log.d("Splash AsyncTask fail",e.toString());}
    }

    private class SplashTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {

            // Delay
            try{Thread.sleep(900);} catch(Exception e) {
                Log.d("Splash delay error",e.toString());
            }
            //TODO: check network connection, Toast a warning if none
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            try {
                super.onPostExecute(o);
                Intent i = new Intent(MainActivity.this, getZipCode.class);
                startActivity(i);
            } catch(Exception e) {Log.d("Splash onPostExecute",e.toString());}
        }
    }
}
