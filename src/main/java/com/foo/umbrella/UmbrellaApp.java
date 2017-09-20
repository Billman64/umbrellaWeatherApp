package com.foo.umbrella;

import android.app.Application;
import android.content.Intent;

import com.foo.umbrella.ui.getZipCode;

public class UmbrellaApp extends Application {


    // show a splash screen then go to activity_get_zip_code


    @Override
    public void onCreate() {
        super.onCreate();
        Intent i = new Intent(this,getZipCode.class);
        startActivity(i);



    }



}