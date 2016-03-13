/*
    Copyright (c) 2012-2013, BogDan Vatra <bogdan@kde.org>
    Contact: http://www.qt-project.org/legal

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.opencpn;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map;

import android.content.Intent;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.CheckBoxPreference;
import android.text.format.Time;
import android.util.Log;

import android.view.View;
import android.widget.Toast;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceFragment;
import android.preference.PreferenceActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.widget.Button;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import ar.com.daidalos.afiledialog.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencpn.opencpn.R;
import org.opencpn.DownloadFile;
import org.opencpn.OCPNGRIBDisplayPrefActivity;

import static java.util.TimeZone.getTimeZone;

//@ANDROID-11


public class OCPNGRIBActivity extends Activity
        //implements OCPNGRIBFragmentDownload.OnDownloadButtonSelectedListener
{
    public JSONObject  m_grib_PrefsJSON;

    public enum GRIB_model {
        GFS100,
        GFS50,
        GFS25
      }

    public boolean mbs52 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.grib_activity_layout);

        Button button_display = (Button)findViewById(R.id.button_grib_display);
        button_display.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Log.i("GRIB", "button_grib_display");

              Intent intent = new Intent(OCPNGRIBActivity.this, org.opencpn.OCPNGRIBDisplayPrefActivity.class);
              startActivityForResult(intent, OCPNGRIBDisplayPrefActivity.OCPN_GRIB_DISPLAY_REQUEST_CODE);

          }
        });


        Button button_file = (Button)findViewById(R.id.button_grib_file);
        button_file.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Log.i("GRIB", "button_grib_file");

              doSelectFile();
          }
        });

        Button button_download = (Button)findViewById(R.id.button_grib_download);
        button_download.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Log.i("GRIB", "button_grib_download");

              Intent intent = new Intent(OCPNGRIBActivity.this, org.opencpn.OCPNGRIBDownloadPrefActivity.class);
              startActivityForResult(intent, 2 /*OCPNGRIBDisplayPrefActivity.OCPN_GRIB_DISPLAY_REQUEST_CODE*/);

          }
        });





        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Bundle extras = getIntent().getExtras();
        if (preferences != null) {
            String settings = preferences.getString("GRIB_PREFS_JSON", "{}");

            try {
                m_grib_PrefsJSON  = new JSONObject( settings );
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            String model = "GFS100";
            int time_step = 3;
            int days = 2;

            boolean req_wind = true;
            boolean req_pressure = false;

            // Populate the app Preferences bundle with passed JSON fields
            Editor editor = preferences.edit();


            try{
                if(m_grib_PrefsJSON.has("model")){
                    model = m_grib_PrefsJSON.getString("model");
                }
                editor.putString("GRIB_prefs_model", model);



            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            editor.apply();



            m_settings = settings;
            m_newSettings = "stuff";

            //  Create the various settings elements

        }



    }

    public void doSelectFile(){

        String dir = "";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null) {
            String file = preferences.getString("GRIB_dest_file", "?");
            if(file != "?"){
                File fp = new File(file);
                if(null != fp){
                    File parent = fp.getParentFile();
                    dir = parent.getAbsolutePath();
                }
            }
            else{
                dir = "/mnt/sdcard/Download/gribs";
            }

        }

        File fdir = new File(dir);
        if((null == fdir) || !fdir.exists() )
            dir = "/mnt/sdcard/Download/gribs";


        Log.i("GRIB", "OCPNGRIBFragmentFile:dir: " + dir);




        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, dir);
        intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, false);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_ONLY_SELECTABLE, true);
        startActivityForResult(intent, 0);
    }


/*
    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.grib_activity_layout,
           container, false);
       Button button = (Button) view.findViewById(R.id.button_grib_display);
       button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Log.i("GRIB", "button_grib_display");
         }
       });
       return view;
     }
*/
/*
    @Override
    public void onBuildHeaders(List<Header> target) {

        loadHeadersFromResource(R.xml.ocpn_grib_headers, target);


    }

    //  We use this method to pass initial arguments to fragments
    //  Note:  Same bundle goes to all fragments.
    @Override
    public void onHeaderClick(Header header, int position) {



        if(header.fragmentArguments == null)
        {
            header.fragmentArguments = new Bundle();
        }
        if(mbs52)
            header.fragmentArguments.putString("S52", "TRUE");
        else
            header.fragmentArguments.putString("S52", "FALSE");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String settings = extras.getString("GRIB_PREFS_JSON");
            header.fragmentArguments.putString("GRIB_PREFS_JSON", settings);
        }


        super.onHeaderClick(header, position);


    }
*/

/*
    public void addDirectory(View vw)
    {
        Toast.makeText(this, "OCPNsettingsactivity Add Dir ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(OCPNGRIBActivity.this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, "/mnt/sdcard");
        intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_ONLY_SELECTABLE, true);
     //   startActivityForResult(intent, 0);

        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.setCanCreateFiles(true);
        dialog.show();

    }

    public void removeDirectory(View vw)
    {
        Toast.makeText(this, "OCPNsettingsactivity Remove Dir ", Toast.LENGTH_SHORT).show();

        OCPNSettingsFragmentCharts cfrag = OCPNSettingsFragmentCharts.getFragment();
        String removalCandidate = cfrag.getSelected();


        cfrag.updateChartDirListView();
    }
*/
/*
    public void selectFile()
    {
//        Toast.makeText(this, "OCPNsettingsactivity Add Dir ", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(OCPNGRIBActivity.this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, "/mnt/sdcard");
        intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_FULL_PATH_IN_TITLE, true);
        intent.putExtra(FileChooserActivity.INPUT_SHOW_ONLY_SELECTABLE, true);
     //   startActivityForResult(intent, 0);

        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.setCanCreateFiles(false);
        dialog.show();

    }
*/

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       Log.i("GRIB", "onActivityResult");
       // Check which request we're responding to
       if (requestCode == 0) {
           // Make sure the request was successful
           if (resultCode == FileChooser.RESULT_OK) {
               Log.i("GRIB", "onActivityResultFileSelectOK");

               boolean fileCreated = false;
               String filePath = "";

               Bundle bundle = data.getExtras();
               if(bundle != null){
                   File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                   filePath = file.getAbsolutePath();
               }


               SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
               SharedPreferences.Editor editor = preferences.edit();
               editor.putString("GRIB_dest_file", filePath);
               editor.apply();

           }
       }

/*
       if (requestCode == 0xf3ec) {
           Log.i("GRIB", "onActivityResult from Download");

           // Make sure the request was successful
           if (resultCode == RESULT_OK) {
               ShowTextDialog("GRIB file downloaded OK");
           }
           else if(resultCode == DownloadFile.ERROR_NO_INTERNET){
               ShowTextDialog("No Internet available\nPlease check your device's Mobile Data or WiFi settings.");
           }
           else if(resultCode == DownloadFile.ERROR_NO_CONNECTION){
               ShowTextDialog("Connection Failed");
           }
           else {
               ShowTextDialog("Download Failed\n  Please check logs.");
           }
       }
*/



   }

   @Override
    public void onResume() {
        super.onResume();
        Log.i("DEBUGGER_TAG", "GRIB Activity onResume");
    }



   @Override
   protected void onPause()
   {
       super.onPause();

       Log.i("DEBUGGER_TAG", "GRIBActivityonPause");

       //createSettingsString();

       Bundle b = new Bundle();
       b.putString("SettingsString", m_newSettings);
//       Intent i = getIntent(); //gets the intent that called this intent
       Intent i = new Intent();
       i.putExtras(b);
       setResult(RESULT_OK, i);

   }

/*
   public void onFileSelected(String filePath){
       String message = "File selected";
       message += ": " + filePath;
       Log.i("GRIB", message);

       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("GRIB_dest_file", filePath);
       editor.apply();

    }
*/



   @Override
   public void finish() {
       Log.i("DEBUGGER_TAG", "GRIB Activity finish");

       String json = persistJSON();
       Log.i("GRIB", "persist json:" + json);

       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
       SharedPreferences.Editor editor = preferences.edit();
       editor.putString("GRIB_PREFS_JSON", json);
       editor.apply();

       Bundle b = new Bundle();
       b.putString("GRIB_JSON", json);
       Intent i = new Intent();
       i.putExtras(b);
       setResult(RESULT_OK, i);

       super.finish();
   }

   public void JSONPersist(SharedPreferences prefs, Map<String,?> keys, String page){
       try {
            for(Map.Entry<String,?> entry : keys.entrySet()){
//                    Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());

               String testB = "grib_prefb_" + page;
               String testS = "grib_prefs_" + page;
               if(entry.getKey().startsWith(testS, 0)){
                   String jsonkey = entry.getKey().substring(11);
                   //Log.d("jsonkey string: ",jsonkey + ": " + entry.getValue().toString());
                   m_grib_PrefsJSON.put(jsonkey, prefs.getString( entry.getKey(), "?"));
               }
               if(entry.getKey().startsWith(testB, 0)){
                   String jsonkey = entry.getKey().substring(11);
                   //Log.d("jsonkey bool: ",jsonkey + ": " + entry.getValue().toString());
                   m_grib_PrefsJSON.put(jsonkey, prefs.getBoolean( entry.getKey(), true));

               }
            }
       } catch (JSONException e) {
           throw new RuntimeException(e);
       }
   }

    private String persistJSON() {
        //  Update everything interesting to the member JSON, and return a compact string

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences != null) {
            Log.i("GRIB", "persisting");

            preferences.edit().clear();
            PreferenceManager.setDefaultValues(this, R.xml.grib_wind_display_settings, true);
            Map<String,?> keys = preferences.getAll();
            JSONPersist(preferences, keys, "Wind");

            preferences.edit().clear();
            PreferenceManager.setDefaultValues(this, R.xml.grib_pressure_display_settings, true);
            keys = preferences.getAll();
            JSONPersist(preferences, keys, "Pressure");

            try {
/*
            for(Map.Entry<String,?> entry : keys.entrySet()){
//                    Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());

                    if(entry.getKey().startsWith("grib_prefs_Wind", 0)){
                        String jsonkey = entry.getKey().substring(11);
                        Log.d("jsonkey string: ",jsonkey + ": " + entry.getValue().toString());
                        m_grib_PrefsJSON.put(jsonkey, preferences.getString( entry.getKey(), "?"));
                    }
                    if(entry.getKey().startsWith("grib_prefb_Wind", 0)){
                        String jsonkey = entry.getKey().substring(11);
                        Log.d("jsonkey bool: ",jsonkey + ": " + entry.getValue().toString());
                        m_grib_PrefsJSON.put(jsonkey, preferences.getBoolean( entry.getKey(), true));

                    }
            }
*/




                m_grib_PrefsJSON.put("model", preferences.getString("GRIB_prefs_model", "?"));
                m_grib_PrefsJSON.put("days", preferences.getString("GRIB_prefs_days", "?"));
                m_grib_PrefsJSON.put("time_step", preferences.getString("GRIB_prefs_timestep", "?"));

                // Prepend the global "Download" directory to the file spec.
//                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                File nFile = new File(rootDir.getAbsolutePath() , preferences.getString("GRIB_prefs_file", "?"));

                m_grib_PrefsJSON.put("grib_file", preferences.getString("GRIB_dest_file", "?"));

                //  GRIB Display Wind Fragment



            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        try{
            return m_grib_PrefsJSON.toString(2);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


/*
    private String appendBoolSetting(String key, Boolean value)
    {
        String ret = key;
        if(value)
            ret = ret.concat(":1;");
        else
            ret = ret.concat(":0;");

        return ret;
    }

    private String appendStringSetting(String key, String value)
    {
        String ret = key;
        ret = ret.concat(":");
        ret = ret.concat(value);
        ret = ret.concat(";");

        return ret;
    }
*/

        // info at
        //http://stackoverflow.com/questions/19973034/isvalidfragment-android-api-19
    //    @Override
    //    protected boolean isValidFragment(String fragmentName) {
    //      return StockPreferenceFragment.class.getName().equals(fragmentName);
    //    }

    //    @TargetApi(Build.VERSION_CODES.HONEYCOMB)

/*
        @Override
        protected boolean isValidFragment (String fragmentName) {

             return true; //"com.fullpackage.MyPreferenceFragment".equals(fragmentName);

        }
*/







    public static String getSettingsString(){ return m_settings;}

    private static String m_settings;
    private String m_newSettings;

    private String m_dest_file;

    public View.OnClickListener myhandler = new View.OnClickListener() {
        public void onClick(View v) {
            String url = CreateDownloadURL();
            Log.i("GRIB", "url: " + url);
        }
    };

    private void ShowTextDialog(final String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setNeutralButton("OK",
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
//            if(!m_licenseOK)
//                finish();
            }
        });
   /*
   builder1.setNegativeButton("No",
   new DialogInterface.OnClickListener() {
   public void onClick(DialogInterface dialog, int id) {
   dialog.cancel();
   }
   });
   */
        AlertDialog alert11 = builder1.create();
        alert11.show();
   }

    public void onDownloadButtonClick(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String model = preferences.getString("GRIB_prefs_model", "GFS100");

        if(model.startsWith("GFS", 0)){
            //downloadGFSDirect();
        }
        else{
            String url = CreateDownloadURL();
//            Log.i("GRIB", "url: " + url);

/*
        //  Create the destination file name
        Calendar calendar = new GregorianCalendar(getTimeZone ("UTC"));

        String dest_file = "gfs_"
                + calendar.get(Calendar.YEAR)
                + calendar.get(Calendar.MONTH)
                + calendar.get(Calendar.DAY_OF_MONTH)
                + "_"
                + calendar.get(Calendar.HOUR_OF_DAY)
                + calendar.get(Calendar.MINUTE)
                + calendar.get(Calendar.SECOND)
                + ".grb2";
*/

            Time t = new Time(Time.getCurrentTimezone());
            t.setToNow();
            String formattedTime = t.format("%Y%m%d_%H%M%S");

            String tdest_file = "gribs/gfs_" + formattedTime + ".grb2";
            File trootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File tdFile = new File(trootDir.getAbsolutePath() , tdest_file);
            String dest_file = tdFile.getAbsolutePath();

            Log.i("GRIB", "dest_file: " + dest_file);

            // persist the target file name
            Editor editor = preferences.edit();
            editor.putString("GRIB_dest_file", dest_file);
            editor.apply();


            Intent intent = new Intent(this, org.opencpn.DownloadFile.class);
            intent.putExtra("URL",url);
            intent.putExtra("FILE_NAME",dest_file);
            startActivityForResult(intent, 0xf3ec /*OCPN_GRIB_REQUEST_CODE*/);
        }

    }

    private String CreateDownloadURL(){

        //  Craft the download URL from the parameters in preferences bundle and JSON object.
        String url = "";

        //  Get some parameters from the Preferences.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String model = preferences.getString("GRIB_prefs_model", "GFS100");
        int time_step = Integer.parseInt(preferences.getString("GRIB_prefs_timestep", "3"));
        int days = Integer.parseInt(preferences.getString("GRIB_prefs_days", "2"));
        Boolean bWind = preferences.getBoolean("GRIB_prefb_wind", true);
        Boolean bPressure = preferences.getBoolean("GRIB_prefb_pressure", true);

        //String startTime = "";
        int lat_min = 0;
        int lat_max = 1;
        int lon_min = 0;
        int lon_max = 1;
        String serverBase = "";

        //  Get some parameters from the JSON
        if(null != m_grib_PrefsJSON) {

            try {
          //      if (m_grib_PrefsJSON.has("server_base")) {
          //          serverBase = m_grib_PrefsJSON.getString("server_base");
          //      }
          //      if (m_grib_PrefsJSON.has("start_time")) {
          //          startTime = m_grib_PrefsJSON.getString("start_time");
          //      }
                if (m_grib_PrefsJSON.has("latMin")) {
                    lat_min = m_grib_PrefsJSON.getInt("latMin");
                }
                if (m_grib_PrefsJSON.has("latMax")) {
                    lat_max = m_grib_PrefsJSON.getInt("latMax");
                }
                if (m_grib_PrefsJSON.has("lonMin")) {
                    lon_min = m_grib_PrefsJSON.getInt("lonMin");
                }
                if (m_grib_PrefsJSON.has("lonMax")) {
                    lon_max = m_grib_PrefsJSON.getInt("lonMax");
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }



        //  All depends on the model....

        GRIB_model model_enum = GRIB_model.valueOf(model.toUpperCase());
        switch(model_enum) {
            case GFS25:
            case GFS50:
            case GFS100: {

                Time t = new Time(Time.getCurrentTimezone());
                t.setToNow();
                t.switchTimezone("UTC");
                String startTime = t.format("%Y%m%d");
                int tHours = t.hour;
                int hour6 = (tHours / 6) * 6;

                //  GFS forceast are late, sometimes, and UTC0000 forecast is not present yet
                //  So get the 1800 for previous day.
                if(hour6 == 0){
                    Time tp = new Time(Time.getCurrentTimezone());
                    tp.setToNow();
                    tp.switchTimezone("UTC");
                    tp.set(t.monthDay-1, t.month, t.year);
                    startTime = tp.format("%Y%m%d");

                    hour6 = 18;
                }

                startTime = startTime.concat(String.format("%02d", hour6));
/*
                //  Create the start time field
                Calendar calendar = new GregorianCalendar(getTimeZone ("UTC"));


                String dest_file = "gfs_"
                        + calendar.get(Calendar.YEAR)
                        + calendar.get(Calendar.MONTH)
                        + calendar.get(Calendar.DAY_OF_MONTH)
                        + "_"
                        + calendar.get(Calendar.HOUR_OF_DAY)
                        + calendar.get(Calendar.MINUTE)
                        + calendar.get(Calendar.SECOND)
                        + ".grb2";

*/
                serverBase = "http://192.168.37.99/get_grib.php";
                url = serverBase + "?";
                url = url.concat("model=" + model);
                url = url.concat("&start_time=" + startTime);

                url = url.concat("&time_step=" + String.format("%d", time_step));

                int time_count = (days * 24) / time_step;
                url = url.concat("&time_count=" + String.format("%d", time_count));

                url = url.concat("&lat_min=" + String.format("%d", lat_min));
                url = url.concat("&lat_max=" + String.format("%d", lat_max));
                url = url.concat("&lon_min=" + String.format("%d", lon_min));
                url = url.concat("&lon_max=" + String.format("%d", lon_max));

                //Log.i("GRIB", "url: " + url);

                //http://localhost/get_grib.php?model=GFS25&start_time=2016021218&time_step=3&time_count=2
                // &lat_min=20&lat_max=40&lon_min=280&lon_max=300
                break;

            }

            default:
                break;
        }

        return url;
    }

/*
    private String downloadGFSDirect(){


        //  Get some parameters from the Preferences.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String model = preferences.getString("GRIB_prefs_model", "GFS100");
        int time_step = Integer.parseInt(preferences.getString("GRIB_prefs_timestep", "3"));
        int days = Integer.parseInt(preferences.getString("GRIB_prefs_days", "2"));
        Boolean bWind = preferences.getBoolean("GRIB_prefb_wind", true);
        Boolean bPressure = preferences.getBoolean("GRIB_prefb_pressure", true);

        int lat_min = 0;
        int lat_max = 1;
        int lon_min = 0;
        int lon_max = 1;
        String serverBase = "";

        //  Get some parameters from the JSON
        if(null != m_grib_PrefsJSON) {

            try {
                if (m_grib_PrefsJSON.has("latMin")) {
                    lat_min = m_grib_PrefsJSON.getInt("latMin");
                }
                if (m_grib_PrefsJSON.has("latMax")) {
                    lat_max = m_grib_PrefsJSON.getInt("latMax");
                }
                if (m_grib_PrefsJSON.has("lonMin")) {
                    lon_min = m_grib_PrefsJSON.getInt("lonMin");
                }
                if (m_grib_PrefsJSON.has("lonMax")) {
                    lon_max = m_grib_PrefsJSON.getInt("lonMax");
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        Time tm = new Time(Time.getCurrentTimezone());
        tm.setToNow();
        tm.switchTimezone("UTC");
        String startTime = tm.format("%Y%m%d");
        int tHours = tm.hour;
        int hour6 = (tHours / 6) * 6;

        //  GFS forcasts are late, sometimes, and UTC0000 forecast is not present yet
        //  So get the 1800 for previous day.
        if(hour6 == 0){
            Time tp = new Time(Time.getCurrentTimezone());
            tp.setToNow();
            tp.switchTimezone("UTC");
            if(tm.monthDay > 1)
                tp.set(tm.monthDay-1, tm.month, tm.year);
            else
                tp.set(29, tm.month-1, tm.year);

            startTime = tp.format("%Y%m%d");

            hour6 = 18;
        }
        else
            hour6 -= 6;

        startTime = startTime.concat(String.format("%02d", hour6));
        String T0 = startTime.substring( startTime.length()-2, startTime.length());

        int loop_count = (days * 24) / time_step;
//        String msga = String.format( "%d %d %d\n", days, time_step, loop_count);
//        Log.i("GRIB", msga);


        Time tn = new Time(Time.getCurrentTimezone());
        tn.setToNow();
        String formattedTime = tn.format("%Y%m%d_%H%M%S");

        String tdest_file = "gribs/gfs_" + formattedTime + ".grb2";
        File trootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File tdFile = new File(trootDir.getAbsolutePath() , tdest_file);
        String dest_file = tdFile.getAbsolutePath();

        Log.i("GRIB", "dest_file: " + dest_file);

        // persist the target file name
        Editor editor = preferences.edit();
        editor.putString("GRIB_dest_file", dest_file);
        editor.apply();

        String gfsFilter = "";
        if(model == "GFS25")
            gfsFilter = "0p25.";
        else if(model == "GFS50")
            gfsFilter = "0p50.";
        else
            gfsFilter = "1p00.";


        String URL_GETGFS = "http://nomads.ncep.noaa.gov/cgi-bin/filter_gfs_" + gfsFilter + "pl?";
        String level = "lev_10_m_above_ground=on";
        String DIR = "%2Fgfs." + startTime;
        String fileSaveName = "gribs/";  // + $_SERVER['REQUEST_TIME'] .$_SERVER['REMOTE_ADDR'];


        ArrayList<String> URLList = new ArrayList<String>();
        ArrayList<String> fileNameList = new ArrayList<String>();

        int t=0;
        // in a loop, get the files
        for(int x=0 ; x < loop_count ; x++){
            String time = String.format("f%03d", t);
            String fileName = "file=gfs.t" + T0 + "z.pgrb2." + gfsFilter + time;  //file=gfs.t18z.pgrb2.0p25.f000


            //  Make the required URL
            String URL_FETCH = URL_GETGFS + fileName;
            if(bWind){
                URL_FETCH += "&lev_10_m_above_ground=on&var_UGRD=on&var_VGRD=on";
            }
            if(bPressure){
                URL_FETCH += "&lev_mean_sea_level=on&var_PRMSL=on";
            }

            URL_FETCH +="&subregion="
                + "&leftlon=" + String.format("%d", lon_min)
                + "&rightlon=" + String.format("%d", lon_max)
                + "&toplat=" + String.format("%d", lat_max)
                + "&bottomlat=" + String.format("%d", lat_min)
                + "&dir=" + DIR;

//            Log.i("GRIB", "URL_FETCH: " + URL_FETCH);

            // Make the server local storage file name
            String sequence = String.format("SEQ%02d", t);
            String localFileName = fileSaveName + sequence;
            File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dFile = new File(rootDir.getAbsolutePath() , localFileName);
            String fullFileName = dFile.getAbsolutePath();

//            Log.i("GRIB", "localFileName: " + fullFileName);

            URLList.add(URL_FETCH);
            fileNameList.add(fullFileName);

            t += time_step;

        }

        //  Calculate estimate block size
        double nBlock = (lat_max - lat_min) * (lon_max - lon_min);
        double factor = 1.0;
        if(model == "GFS25")
            factor = 16;;
        if(model == "GFS50")
            factor = 4;

        nBlock *= factor;
        int nParam = 0;
        nParam = 0;
        if(bWind)
            nParam += 2;
        if(bPressure)
            nParam ++;

        nBlock *= nParam;
        int niBlock = (int)nBlock;

        String bmsg = String.format("%d\n", niBlock);
        Log.i("GRIB", "nBlock: " + bmsg);


        Intent intent = new Intent(this, org.opencpn.downloadGFSCombine.class);
        intent.putExtra("URLList",URLList);
        intent.putExtra("fileNameList",fileNameList);

        intent.putExtra("GRIB_dest_file",dest_file);
        intent.putExtra("niBlock",niBlock);
        startActivityForResult(intent, 0xf3ec OCPN_GRIB_REQUEST_CODE);
*/

/*
        $t = 0;
         for ($x = 0; $x < $loop_count; $x++) {

             $TIME = sprintf("f%03d", $t);

             $FILE_NAME = "file=gfs.t" . $T0 . "z.pgrb2." . $gfs_filter . $TIME;  //file=gfs.t18z.pgrb2.0p25.f000

             $t += $_GET['time_step'];

             //  Make the required URL
             $URL_FETCH = $URL_GETGFS . $FILE_NAME . "&" . $LEVEL . "&var_UGRD=on&var_VGRD=on" . "&subregion="
                 . "&leftlon=" . $LEFT_LON
                 . "&rightlon=" . $RIGHT_LON
                 . "&toplat=" . $TOP_LAT
                 . "&bottomlat=" . $BOTTOM_LAT
                 . "&dir=" . $DIR;


#                echo $URL_FETCH . "<br>";

             // Make the server local storage file name
             $SEQ = sprintf("SEQ%02d", $x);
             $localFileName = $fileSaveName.$SEQ;
             #echo $localFileName . "<br>";

             //  Get the file
             get_grib( $URL_FETCH, $localFileName);

             #usleep(200000);         // sleep 0.2 secs.

#                echo "<br>";

         }



        return "OK";
    }
*/

}





