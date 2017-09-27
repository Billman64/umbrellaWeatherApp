package com.foo.umbrella.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.foo.umbrella.R;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//import com.foo.umbrella.debug.R;

public class getZipCode extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_get_zip_code);
//    }


//  @Override public void onCreate() {
//    super.onCreate();
//    AndroidThreeTen.init(this);
//
//  }

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

    String strResult = "";


      @Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_get_zip_code);

          AndroidThreeTen.init(this);
          TextView tv = (TextView) findViewById(R.id.tvBanner);
          ImageView ivC = (ImageView) findViewById(R.id.ivCurrent);

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


      }

    public void getWeather(View view) {

        //TODO: error-trap zip input

        final TextView tv = (TextView) findViewById(R.id.tvBanner);
        final TextView tvCity = (TextView) findViewById(R.id.tvCity);
        EditText et = (EditText) findViewById(R.id.etZip);
        ListView lv = (ListView) findViewById(R.id.lvOutput);
        tv.setText(et.getText());


        final Handler handler = new Handler(getApplicationContext().getMainLooper());


        // API call (TODO: refactor into data model area of code)
        String zip = et.getText().toString();
        final String strUrl = "http://api.wunderground.com/api/" + getString(R.string.api_key) + "/conditions/hourly/q/" + zip + ".json";
        tv.setText(strUrl);
        Button btn = (Button) findViewById(R.id.button);
        btn.setText(R.string.button_label);


        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {


                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(R.string.connecting); // connecting to API
                            btn.setEnabled(false);
                            btn.setText("");
//                            tv.setBackgroundColor(tv.getBackground()-10);
                        }
                    });

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

                    StringBuffer json = new StringBuffer(1024); // use StringBuilder instead?
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

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // main thread communication

                            strData = data.toString();
                            tv.setText(R.string.forecast_for); // forecast for:
                            tvCity.setText(city+ ", " + state);

                            btn.setEnabled(true);
                            btn.setText(R.string.button_label2);

                            // set image icon for current conditions at the top
                            //TODO: refactor this into a function, setWeatherIcon_Current(String weatherCondition)
                            ImageView ivC = (ImageView) findViewById(R.id.ivCurrent);
                            switch(weather){
                                case "Clear":
                                    ivC.setImageResource(R.drawable.weather_sunny);
                                    ivC.setColorFilter(Color.YELLOW);
                                    break;
                                case "Partly Cloudy":
                                    ivC.setImageResource(R.drawable.weather_partlycloudy);
                                    ivC.setColorFilter(Color.GRAY);
                                    break;
                                case "Cloudy":
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
                                    break;
                                case "Rainy":
                                    ivC.setImageResource(R.drawable.weather_rainy);
                                    ivC.setColorFilter(Color.BLUE);
                                    break;
                                case "Snowy":
                                    ivC.setImageResource(R.drawable.weather_snowy);
                                    ivC.setColorFilter(Color.LTGRAY);
                                    break;
                                case "Snowy Rainy":
                                    ivC.setImageResource(R.drawable.weather_snowy_rainy);
                                    ivC.setColorFilter(Color.LTGRAY);
                                    break;
                                case "Windy Variant":
                                    ivC.setImageResource(R.drawable.weather_windy_variant);
                                    ivC.setColorFilter(Color.GRAY);
                                    break;
                            }



                                // bonus: filter out insignificant data for better UX (ie: winds <2mph, humidity 0%)
                                String currentWeather = "Currently:  "+ weather +" "+ String.valueOf(temp_f) +"°\n";
                                if(!humidity.equals("0%")) currentWeather = currentWeather +"\t"+ humidity +" humid.\t";
                                if(wind > 2) currentWeather = currentWeather +" "+ wind +" mph winds\t";
                                if(Double.parseDouble(precipitation) >= 1.0 ) currentWeather = currentWeather +" "+ precipitation +" precip.";

                                // display data - populating listView

                                // display current weather
                                TextView tvCurrent = (TextView) findViewById(R.id.tvCurrentConditions);
                                tvCurrent.setText(currentWeather);
                                // color-code background color based on temperature
                                if(temp_f>=60) tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.weather_warm));
                                    else tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.weather_cool));

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
                                Log.d("status","arr[] filled. about to update listAdapter");

                                listAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, arr);

                            Log.d("status","listAdapter created");
                            lv.setAdapter(listAdapter); Log.d("lv", "adapter set");

                            // color-code the highest and lowest temperatures list items
                            r =0;
                            View row;
                            while(r<hourlyArr.length){
//                                Log.d("listView data sample",r +". "+ lv.getItemAtPosition(r).toString() +" | "+ lv.getAdapter().getView(0,null, lv).toString());

//                                LayoutInflater inflater = ((Activity) getBaseContext()).getLayoutInflater();
//                                View row = inflater.inflate(this,lv,false);
                                Log.d("high/low",high +" "+ low);

                                row = lv.getAdapter().getView(r,null,lv);
                                    Log.d("rowTest",row.toString()+"");
                                if(Integer.valueOf(hourlyArr[r][2]) == high) {
                                    row.setBackgroundColor(Color.RED);
                                    Log.d("High row found", high +" at #"+ r);
                                }


                                        //setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.weather_warm));
                                //if(temp_f>=60) tvCurrent.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.weather_warm));

                                r++;
                            }

                        }
                    });
//                    handler.removeCallbacks(); // prevent memory leak?

//                    handler.sendMessage(msgObj);

                    // create a handler to communicate JSON data back to main thread
//            private final Handler handler = new Handler() { //TODO: handler potential memory leak, since it's nested (clear on onDestroy())
//                public void handleMessage(Message msg){
//                    String response = msg.getData().getString("message");
//                }
//            };
                    //TODO: filter JSON data to only desired fields
                    //TODO: populate RecyclerView or ListView with hourly weather data
                    //TODO: color-code RecyclerView with colors from colors.xml (from the challenge)
                } catch (Exception e) {
                    strData = e.toString() + "\nData connection failed.";

                    Log.d("MyError", "Data pull not working (maybe network connection issue?)" + e.toString());
                    Log.d("MyErrorLocation", strResult);


                    handler.post(new Runnable(){
                        @Override
                        public void run() {
                            tv.setText(R.string.connetion_error);
                            btn.setEnabled(true);
                            btn.setText(R.string.button_label2);
                        }
                    });
                }

            }
        });    // end of thread
        t.start();

    }
}