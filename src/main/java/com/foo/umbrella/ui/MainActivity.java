package com.foo.umbrella.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.foo.umbrella.R;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

      //TODO: put in a background image to fill the Frame Layout with something attractive, yet simple
      //TODO: Check network connection, toast a warning message if none

      //TODO: load cached weater data from last time the app was used (if any), along with a datetime stamp
  }

    @Override
    protected void onStart() {
        super.onStart();

        //    sleep((int) R.integer.splash_delay);  // prevents textView from displaying for some reason
        int splashDelay = 1000;
        TextView tv = (TextView) findViewById(R.id.textView3);
//        sleep(splashDelay/3);
//        tv.setText("3");
//        sleep(splashDelay/3);
//        tv.setText("2");
//        sleep(splashDelay/3);
//        tv.setText("1");
//  sleep(0);
        Intent i = new Intent(this,getZipCode.class);
        startActivity(i);

    }
}
