package com.foo.umbrella;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.foo.umbrella.ui.MainActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;

// can not use findViewById and setContentView() here for some reason, so redirecting to an activity/class that can

public class UmbrellaApp extends Application {
  private ArrayAdapter<String> listAdapter;
  public String strData;
  String strResult = "";


//  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate();
    AndroidThreeTen.init(this);
//    setContentView()

      Intent i = new Intent(this, MainActivity.class);
    startActivity(i);
  }

}