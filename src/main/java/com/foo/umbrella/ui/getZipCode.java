package com.foo.umbrella.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.foo.umbrella.R;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.leakcanary.LeakCanary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class getZipCode extends AppCompatActivity {

    private ArrayAdapter<String> listAdapter;
    public String strData;
    public String state;
    public String city;
    public String weather;
    public double temp_f;
    public String humidity; // data includes %
    public double wind;
    public String precipitation;

    public String hourlyArr[][] = {{"a","b"}, {"a2","b2"} };
    public int rMax=0;
    public int buttonPresses=0;

    String strResult = "";


      @Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_get_zip_code);

          if (LeakCanary.isInAnalyzerProcess(this)) {
              // This process is dedicated to LeakCanary for heap analysis.
              // You should not init your app in this process.
              return;
          }
          LeakCanary.install(getApplication());


          AndroidThreeTen.init(this);
          TextView tv = (TextView) findViewById(R.id.tvBanner);


          // AppLink
          // ATTENTION: This was auto-generated to handle app links.
          Intent appLinkIntent = getIntent();
          String appLinkAction = appLinkIntent.getAction();
          Uri appLinkData = appLinkIntent.getData();
          // if there is an appLink intent, then use it to pre-fill zip code
          try {
              Log.d("appLink", appLinkData.getQuery() + "");

              if (appLinkData.toString().length() > 0) {
                  EditText et = (EditText) findViewById(R.id.etZip);
                  et.setText(appLinkData.getPathSegments().get(0).substring(0,4)); //TODO: extract zip code from AppLink intent data
              }
          } catch(Exception e) {
              if(e.getMessage().length()>0) Log.d("AppLink Error",e.getMessage());
          }

          // ? Use GPS get a default zip code (using WU's geolookup API, converting lat.+long. to zip)

      }

    public void getWeather(View view) {

        //TODO: error-trap zip input

        final TextView tv = (TextView) findViewById(R.id.tvBanner);
        final TextView tvCity = (TextView) findViewById(R.id.tvCity);
        EditText et = (EditText) findViewById(R.id.etZip);
        ListView lv = (ListView) findViewById(R.id.lvOutput);
        Button btn = (Button) findViewById(R.id.button);
        tv.setAlpha(1); // make sure tvBanner visible (2nd+ button presses)
        tv.setText(et.getText());
        ImageView ivC = (ImageView) findViewById(R.id.ivCurrent);

        if(!btn.isEnabled()) return;  // Don't run if not enabled.

        // hide soft keyboard
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken() ,0);

        //TODO: Conserve bandwidth, in accordance to Weather Underground's Stratus policy (<10 data pulls per min.), via a delay (9th button press has a delay)
        buttonPresses++; Log.d("buttonPresses",String.valueOf(buttonPresses));
        if(buttonPresses % 9 == 0) {
            btn.setEnabled(false);  //disable button to prevent abuse of bandwidth

//            Thread buttonDelay = new Thread(new Runnable() {  // replaced with Lambda to make thread code more concise.
            Runnable buttonDelayRunnable = () -> {

                    try{
                        Log.d("ButtonPress","delay for 9th press activated for bandwidth limitations");
                        Thread.sleep(2000); //TODO: implement delay values and messages in Strings.xml, in case of any WU data policy changes
                        Log.d("ButtonPress","slept 2 sec.'s");

                        Handler h = new Handler(getApplicationContext().getMainLooper());

                        Runnable delayMessage = () ->
                            Toast.makeText(getZipCode.this, "API bandwidth restriction delay", Toast.LENGTH_SHORT).show();

                        h.post(delayMessage);


                        Thread.sleep(58000);

                        Runnable delayFinished = () -> {
                                btn.setEnabled(true);   //re-enable button, after delay
                                buttonPresses = 0;  // reset to "first" press, preventing out of bound integer
                                tv.setText(R.string.delay_finished);
                                getWeather(view);   // re-run

                                Log.d("buttonDelay","button re-enabled");
                            };
                        h.post(delayFinished);

                    } catch (Exception e) {Log.d("buttonDelay",e.toString());}
                };

            new Thread(buttonDelayRunnable).start();
            if(!btn.isEnabled()) return;    // Prevent API call code from running, let the thread handle the rest.
        }


        // remove default launcher icon from current weather section
//        ImageView ivC = (ImageView) findViewById(R.id.ivCurrent);
        if(ivC.getDrawable() ==  ContextCompat.getDrawable(this,R.mipmap.ic_launcher)) {
            ivC.setImageResource(0);
            ivC.setContentDescription("");
        }

        final Handler handler = new Handler(getApplicationContext().getMainLooper());


        // API call (TODO: refactor into data model area of code)
        String zip = et.getText().toString();
        final String strUrl = "http://api.wunderground.com/api/" + getString(R.string.api_key) + "/conditions/hourly/q/" + zip + ".json";
        tv.setText(strUrl);
        btn.setText(R.string.button_label);


        Thread t = new Thread(() -> {


            try {
//                handler.post(new Runnable() {
                    Runnable connectingMsg = () -> {

                        tv.setText(R.string.connecting); // connecting to API
                        btn.setEnabled(false);
                        btn.setText("");
//                            tv.setBackgroundColor(tv.getBackground()-10);
                    };
                    handler.post(connectingMsg);

                // Gather JSON data from API ----------------------
                URL url = new URL(strUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();    // needs to run on a thread

                InputStreamReader isr = new InputStreamReader(connection.getInputStream()) {
                    @Override
                    public int read() throws IOException {
                        Log.d("myError", "inputStreamReader error");
                        return 0;
                    }
                };
                Log.d("connection", connection.toString());
//                    Reader rdr = new InputStreamReader(isr);

                BufferedReader reader = new BufferedReader(isr);

                StringBuilder json = new StringBuilder(1024);
                String tmp;

                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp).append("\n");
                }
                reader.close();

                final JSONObject data = new JSONObject(json.toString());
                strResult = "error: after JSONObject initialization";

                if (data.length() == 0) {
                    strResult = "error: zero-length data"; Log.d("JSON data length","0");
                } else {

                    // Parse JSON data -------------------------
                    final JSONObject current = new JSONObject(data.getString("current_observation"));
                    final JSONObject display_location = new JSONObject(current.getString("display_location"));
                    final JSONArray hourly = new JSONArray(data.getString("hourly_forecast")); // error - need JSON array
                    rMax = hourly.length();

                    Log.d("forecastDataSample",current.toString().substring(0,50));
                    Log.d("dataSample", data.toString().substring(0, 50));

                    // Gather data points - current weather
                    if (current.toString().length()>0) {
                            city = display_location.getString("city");
                            state = display_location.getString("state");
                            weather = current.getString("weather");
                            temp_f = current.getDouble("feelslike_f");
                            humidity = current.getString("relative_humidity");
                            wind = current.getDouble("wind_mph");
                            precipitation = current.getString("precip_1hr_in");
                            Log.d("cityState",city + "," + state);
                        } else Log.d("Current weather", "data not found");

                    // Gather data points - hourly weather
                    strResult = "current JSON created. starting hourly JSON";
                    Log.d("JSON gathering","current complete");

                    if (hourly.length() >0) {
                        Log.d("JSON gathering", "process started! array length: "+hourly.length());
                        int r;
                        JSONObject hourlyObj;
                        hourlyArr = new String[hourly.length()][6];

                        Log.d("JSON gathering", "hourlyObj created");
                        // loop through hourly JSON data and gather it

                        for(r=0;r<hourly.length();r++) {
                            hourlyObj = new JSONObject(hourly.getString(r));    // grab next JSON object in set
                            hourlyArr[r][0] = hourlyObj.getJSONObject("FCTTIME").getString("civil");
                            hourlyArr[r][1] = hourlyObj.getString("condition");
                            hourlyArr[r][2] = hourlyObj.getJSONObject("feelslike").getString("english");
                            hourlyArr[r][3] = hourlyObj.getString("humidity");
                            hourlyArr[r][4] = hourlyObj.getJSONObject("wspd").getString("english");
                            hourlyArr[r][5] = hourlyObj.getJSONObject("snow").getString("english") ;
                        }

                        Log.d("hourly data sample",hourlyArr[0][0] +" "+ hourlyArr[0][1]);



                        } else Log.d("Hourly weather", "data not found");
                    }

//                handler.post(new Runnable() {
                    Runnable presentData = () -> {

                        // main thread communication

                        strData = data.toString();
                        tv.setText(R.string.forecast_for); // forecast for:
                        tvCity.setText(city+ ", " + state);

                        btn.setEnabled(true);
                        btn.setText(R.string.button_label2);

                        // set image icon for current conditions at the top
                        //TODO: refactor this into a function, setWeatherIcon_Current(String weatherCondition)
//                        ImageView ivC1 = (ImageView) findViewById(R.id.ivCurrent);

                        getWindow().getDecorView().setBackgroundColor(Color.rgb(250,250,250));  // subtle background color - default for most conditions
                        switch(weather){
                            case "Clear":
                            case "Sunny":
                                ivC.setImageResource(R.drawable.weather_sunny);
                                ivC.setColorFilter(Color.rgb(200,200,0)); // Color.YELLOW is too bright over off-white
                                getWindow().getDecorView().setBackgroundColor(Color.rgb(255,253,250));  // subtle background color
                                break;
                            case "Partly Cloudy":
                            case "Overcast":
                                ivC.setImageResource(R.drawable.weather_partlycloudy);
                                ivC.setColorFilter(Color.GRAY);
                                getWindow().getDecorView().setBackgroundColor(Color.rgb(253,253,250));
                                break;
                            case "Cloudy":
                            case "Mostly Cloudy":
                                ivC.setImageResource(R.drawable.weather_cloudy);
                                ivC.setColorFilter(Color.GRAY);
                                break;
                            case "Lightning":
                                ivC.setImageResource(R.drawable.weather_lightning);
                                break;
                            case "Fog":
                                ivC.setImageResource(R.drawable.weather_fog);
                                ivC.setColorFilter(Color.GRAY);
                                break;
                            case "Hail":
                                ivC.setImageResource(R.drawable.weather_hail);
                                ivC.setColorFilter(Color.GRAY);
                                break;
                            case "Lightning Rainy":
                                ivC.setImageResource(R.drawable.weather_lightning_rainy);
                                ivC.setColorFilter(Color.GRAY);
                                getWindow().getDecorView().setBackgroundColor(Color.rgb(240,240,245));
                                break;
                            case "Rainy":
                            case "Chance of Rain":
                                ivC.setImageResource(R.drawable.weather_rainy);
                                ivC.setColorFilter(Color.BLUE);
                                break;
                            case "Snowy":
                                ivC.setImageResource(R.drawable.weather_snowy);
                                ivC.setColorFilter(Color.LTGRAY);
                                getWindow().getDecorView().setBackgroundColor(Color.rgb(254,254,254));
                                break;
                            case "Snowy Rainy":
                                ivC.setImageResource(R.drawable.weather_snowy_rainy);
                                ivC.setColorFilter(Color.LTGRAY);
                                getWindow().getDecorView().setBackgroundColor(Color.rgb(250,250,254));
                                break;
                            case "Windy Variant":
                                ivC.setImageResource(R.drawable.weather_windy_variant);
                                ivC.setColorFilter(Color.GRAY);
                                break;
                        }
                        ivC.setContentDescription(weather); // update content description for accessibility

                        // hide banner for until next button press
                        tv.setAlpha(0);
//                            tv.setVisibility(View.GONE);
//                            tvCity.setTop(10);  //TODO: move top elements up to make more room -> +UX
//                            tvCity.setLeft(200);
                        //ViewPropertyAnimator.animate(view).translationYBy(-yourY).translationXBy(-yourX).setDuration(0);

                            // bonus: filter out insignificant data for better UX (ie: winds <2mph, humidity 0%)
                            String currentWeather = "Now:  "+ weather +" "+ String.valueOf(temp_f) +"°\n";
                            String currentWeatherDetail = "";
                            if(!humidity.equals("0%")) currentWeatherDetail = currentWeatherDetail +"\t"+ humidity +" humid.\t";
                            if(wind > 2) currentWeatherDetail = currentWeatherDetail +" "+ wind +" mph winds\t";
                            if(Double.parseDouble(precipitation) >= 1.0 ) currentWeatherDetail = currentWeatherDetail +" "+ precipitation +" precip.";

                            // display data - populating listView

                            // display current weather
                            TextView tvCurrent = (TextView) findViewById(R.id.tvCurrentConditions);
                            tvCurrent.setText(currentWeather);
                            TextView tvCurrentDetail = (TextView) findViewById(R.id.tvCurrentDetails);
                            tvCurrentDetail.setText(currentWeatherDetail);
                        Log.d("CurrentWeatherDetail",tvCurrentDetail.getText() +"");

                            // color-code background color based on temperature
                            if(temp_f>=60) tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.weather_warm));
                                else tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.weather_cool));
//                                tvCurrentDetail.setBackgroundDrawable(tvCurrent.getBackground()); //deprecated
                            tvCurrentDetail.setBackground(tvCurrent.getBackground());


                            // prepare listView
                            String[] arr = new String[hourlyArr.length];
                            int r=0;
                            int high=0;
                            int low=999;
                            Log.d("arr[] rMax|length: ",String.valueOf(rMax) +"|"+ String.valueOf(hourlyArr.length));
                            while (r<hourlyArr.length) {
                                arr[r] = hourlyArr[r][0] + " " + hourlyArr[r][1] + " " + hourlyArr[r][2] + "°\n\t";

                                // filter out 0% humidity
                                if(Double.valueOf(hourlyArr[r][3])>0.0) arr[r] = arr[r] +" "+ hourlyArr[r][3] +"% humid";
                                // filter out 0mph wind
                                if(Double.valueOf(hourlyArr[r][4])>0) arr[r] = arr[r] +" "+ hourlyArr[r][4] +" mph wind";
                                // filter out 0 inches of snow
                                if(Double.valueOf(hourlyArr[r][5]) > 0.0) arr[r] = arr[r] + " " + hourlyArr[r][5] +" in. snow";

                                // get the high and low temperatures
                                if(Integer.valueOf(hourlyArr[r][2]) > high) high = Integer.valueOf(hourlyArr[r][2]);
                                if(Integer.valueOf(hourlyArr[r][2]) < low) low = Integer.valueOf(hourlyArr[r][2]);
                                Log.d("r",String.valueOf(r));
                                r++;
                            }


                        listAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, arr);
                        Log.d("listAdapater getCount()",listAdapter.getCount() +"");
                        lv.setAdapter(listAdapter);
                        Log.d("lv child count", lv.getCount() +"");

                        // TODO:color-code the highest and lowest temperatures list items
                        r =0;
                        TextView tvRow = new TextView(getBaseContext());

                        ViewGroup.LayoutParams params = new ActionBar.LayoutParams(500,100);
//                            lv.addView()
                        lv.setAdapter(listAdapter);
                        while(r<listAdapter.getCount()){
//                                Log.d("listView data sample",r +". "+ lv.getItemAtPosition(r).toString() +" | "+ lv.getAdapter().getView(0,null, lv).toString());

//                                LayoutInflater inflater = ((Activity) getBaseContext()).getLayoutInflater();
//                                View row = inflater.inflate(this,lv,false);
//                                Log.d("high/low",high +" "+ low);
//                                Log.d("listAdapter" + r, listAdapter.get );

//                                row = lv.getAdapter().getView(r,null,lv);
//                                Log.d("rowTest",row.toString()+"");

                            if(Integer.valueOf(hourlyArr[r][2]) == high) {
//                                    row.setBackgroundColor(Color.RED);
//                                    listAdapter.getView(r,view,lv).setBackgroundColor(Color.RED);  // affects button instead. might need a custon layout
                                //TODO: access individual listView item to color it. May need a custom adapter.
                                Log.d("listAdapt getItmVT",listAdapter.getItemViewType(r) +"");
                                lv.setBackgroundColor(Color.argb(10,200,200,200));

                                Log.d("High row found", high +" at #"+ r);
                            }
                                    //setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.weather_warm));
                            //if(temp_f>=60) tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.weather_warm));

                            r++;
                        }
                        Log.d("r",r +"");

//                            lv.setAdapter(listAdapter);

                };
                handler.post(presentData);
//                    handler.removeCallbacks(); // prevent memory leak?

                //TODO: color-code ListView with colors from given colors.xml.
            } catch (Exception e) {
                strData = e.toString() + "\nData connection failed.";

                Log.d("MyError", "Data pull not working (maybe network connection issue?)" + e.toString());
                Log.d("MyErrorLocation", strResult);

                Runnable connectionError = () -> {
                        tv.setText(R.string.connetion_error);
                        btn.setEnabled(true);
                        btn.setText(R.string.button_label2);
                };
                handler.post(connectionError);
            }

        });    // end of thread
        t.start();

    }
}