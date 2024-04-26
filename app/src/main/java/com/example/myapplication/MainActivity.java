package com.example.myapplication;

import android.Manifest;
import android.app.Activity;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import zephyr.android.BioHarnessBT.*;

import com.highsoft.highcharts.core.*;
import com.highsoft.highcharts.common.hichartsclasses.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity implements ApiCallback{
    /** Called when the activity is first created. */
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE = 0x100;
    private final int RESPIRATION_RATE = 0x101;
    private final int SKIN_TEMPERATURE = 0x102;
    private final int POSTURE = 0x103;
    private final int PEAK_ACCLERATION = 0x104;
    private final int BATTERY_STATUS = 0x105;
    private final int WORN_STATUS = 0x106;
    private final int ACTIVITY = 0x107;
    private final int HR_CONF = 0x108;

    private static final int REQUEST_CODE_PROFILE_ACTIVITY = 1;


    HILine series = new HILine();
    HIData data = new HIData();

    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String height;
    private String weight;
    private String gender;
    private EditText birthday;
    private String dateString;
    private int differenceInDays;
    private float calories;
    JSONObject jsonParams = new JSONObject();
    JSONObject jsonToCsv = new JSONObject();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS");
    private ArrayList<JSONObject> jsonObjectsList = new ArrayList<>();
    private ArrayList<Float> calsList = new ArrayList<>();
    ImageView imageWorn;
    boolean wornTextPrev = false;
    boolean isStarted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        //Obtaining the handle to act on the CONNECT button
        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
        String ErrorText = "Not Connected to BioHarness !";
        tv.setText(ErrorText);

        String[] userValues = loadData();

        if (userValues.length != 0){
            System.out.println("mpainw na kanw set data");
            gender = userValues[0];
            height = userValues[1];
            weight = userValues[2];
            dateString = userValues[3];
        }

        System.out.println(gender);
        System.out.println(height);
        System.out.println(weight);
        System.out.println(dateString);


        Button btnProfile = (Button) findViewById(R.id.buttonProfile);


        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        btnDisconnect.setEnabled(false);

        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);
        if (btnConnect != null) {
            btnConnect.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Log.d("message", String.valueOf(Build.VERSION.SDK_INT));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.BLUETOOTH_CONNECT},100);
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.BLUETOOTH_SCAN},101);
                        return;
                    }

                    String BhMacID = "00:07:80:9D:8A:E8";
                    //String BhMacID = "00:07:80:88:F6:BF";
                    adapter = BluetoothAdapter.getDefaultAdapter();



                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();


                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device : pairedDevices)
                        {
                            if (device.getName().startsWith("BH"))
                            {
                                BluetoothDevice btDevice = device;
                                BhMacID = btDevice.getAddress();
                                break;

                            }
                        }


                    }

                    Log.d("message",BhMacID);

                    //BhMacID = btDevice.getAddress();
                    BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
                    String DeviceName = Device.getName();
                    _bt = new BTClient(adapter, BhMacID);
                    _NConnListener = new NewConnectedListener(Newhandler,Newhandler);
                    _bt.addConnectedEventListener(_NConnListener);

                    TextView tv1 = findViewById(R.id.labelCals);
                    tv1.setText("000");
                    tv1.setEnabled(false);

                    /*
                    tv1 = 	(EditText)findViewById(R.id.labelGender);
                    //gender = tv1.getText().toString();
                    System.out.println(gender);
                    tv1.setEnabled(false);

                    tv1 = 	(EditText)findViewById(R.id.labelHeight);
                    //height = tv1.getText().toString();
                    System.out.println(height);
                    tv1.setEnabled(false);

                    tv1 = 	(EditText)findViewById(R.id.labelWeight);
                    //weight = tv1.getText().toString();
                    System.out.println(weight);
                    tv1.setEnabled(false);

                    tv1 = 	(EditText)findViewById(R.id.labelBirthday);
                    //dateString = tv1.getText().toString();
                    System.out.println(dateString);
                    tv1.setEnabled(false);
                    */

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    Date pastDate = null;
                    try {
                        pastDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Calendar todayCalendar = Calendar.getInstance();
                    Date currentDate = todayCalendar.getTime();

                    long differenceInMillis = currentDate.getTime() - pastDate.getTime();
                    differenceInDays = (int) (differenceInMillis / (1000 * 60 * 60 * 24));
                    System.out.println(differenceInDays);


                    //saveData(new String[]{gender, height, weight, dateString});

                    btnProfile.setEnabled(false);

                    System.out.println("on connect");
                    System.out.println(gender);
                    System.out.println(height);
                    System.out.println(weight);
                    System.out.println(dateString);

                    try {
                        if (gender=="F") {
                            jsonParams.put("female", "1");
                            jsonParams.put("male", "0");
                        } else{
                            jsonParams.put("female", "0");
                            jsonParams.put("male", "1");
                        }
                        jsonParams.put("HR","" );
                        jsonParams.put("BR","" );
                        jsonParams.put("Posture", "");
                        jsonParams.put("Activity", "");
                        jsonParams.put("Acceleration", "");
                        jsonParams.put("XMin", "");
                        jsonParams.put("XPeak", "");
                        jsonParams.put("YMin", "");
                        jsonParams.put("YPeak", "");
                        jsonParams.put("ZMin", "");
                        jsonParams.put("ZPeak", "");
                        jsonParams.put("weight", weight);
                        jsonParams.put("height", height);
                        jsonParams.put("age", String.valueOf(differenceInDays));

                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    jsonToCsv = jsonParams;
                    /*
                    String apiUrl = "https://api.jprokopakis.gr/random-forest"+paramsInURL(jsonParams);
                    System.out.println(apiUrl);
                    new ApiRequestTask(MainActivity.this).execute(apiUrl);
                    */
                    if(_bt.IsConnected())
                    {
                        _bt.start();
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        //String ErrorText  = "Connected to BioHarness "+DeviceName;
                        String ErrorText  = "Connected to BioHarness";
                        tv.setText(ErrorText);

                        //Reset all the values to 0s
                        btnDisconnect.setEnabled(true);
                        btnConnect.setEnabled(false);

                    }
                    else
                    {
                        TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                        String ErrorText  = "Unable to Connect !";
                        tv.setText(ErrorText);
                    }
                }
            });
        }
        /*Obtaining the handle to act on the DISCONNECT button*/
        //Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null)
        {
            btnDisconnect.setOnClickListener(new OnClickListener() {
                @Override
                /*Functionality to act if the button DISCONNECT is touched*/
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    /*Reset the global variables*/
                    TextView tv = (TextView) findViewById(R.id.labelStatusMsg);
                    String ErrorText  = "Disconnected from BioHarness!";
                    tv.setText(ErrorText);

                    /*This disconnects listener from acting on received messages*/
                    _bt.removeConnectedEventListener(_NConnListener);
                    /*Close the communication with the device & throw an exception if failure*/
                    _bt.Close();

                    btnDisconnect.setEnabled(false);
                    btnConnect.setEnabled(true);


                }
            });
        }
        /*Instantiate chart*/
        HIChartView chartView = (HIChartView) findViewById(R.id.hc);

        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        //chart.setType("column");
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Total Calories expended");
        options.setTitle(title);

        options.setLegend(new HILegend());
        options.getLegend().setEnabled(false);

        HIExporting exporting = new HIExporting();
        exporting.setEnabled(false);
        options.setExporting(exporting);


        options.setSeries(new ArrayList<>(Collections.singletonList(series)));

        chartView.setOptions(options);




    }

    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("btbond receiver");
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("btbroadcast receiver");
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
                byte[] pin = (byte[])m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    final  Handler Newhandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg)
        {
            TextView tv;

            switch (msg.what)
            {
                case HEART_RATE:
                    String HeartRatetext = msg.getData().getString("HeartRate");
                    tv = findViewById(R.id.labelHr);
                    System.out.println("Heart Rate Info is "+ HeartRatetext);
                    if (tv != null)tv.setText(HeartRatetext);

                    break;

                case RESPIRATION_RATE:
                    String RespirationRatetext = msg.getData().getString("RespirationRate");
                    tv = findViewById(R.id.labelBr);
                    if (tv != null)tv.setText(RespirationRatetext);

                    break;
                case ACTIVITY:
                    String Activitytext = msg.getData().getString("Activity");
                    tv = findViewById(R.id.labelActivity);
                    if (tv != null)tv.setText(Activitytext);

                    break;
                case HR_CONF:
                    String HRConftext = msg.getData().getString("HeartRateConfidence");
                    tv = findViewById(R.id.labelHrConf);
                    if (tv != null)tv.setText(HRConftext);

                    break;
                /*
                case SKIN_TEMPERATURE:
                    String SkinTemperaturetext = msg.getData().getString("SkinTemperature");
                    tv = (EditText)findViewById(R.id.labelGender);
                    if (tv != null)tv.setText(SkinTemperaturetext);

                    break;

                case POSTURE:
                    String PostureText = msg.getData().getString("Posture");
                    tv = (EditText)findViewById(R.id.labelWeight);
                    if (tv != null)tv.setText(PostureText);


                    break;

                case PEAK_ACCLERATION:
                    String PeakAccText = msg.getData().getString("PeakAcceleration");
                    tv = (EditText)findViewById(R.id.labelHeight);
                    if (tv != null)tv.setText(PeakAccText);

                    break;
                */
                case BATTERY_STATUS:
                    /*
                    String BatteryText = msg.getData().getString("Battery");
                    tv = (EditText)findViewById(R.id.labelBattery);
                    if (tv != null)tv.setText(BatteryText);
                    */
                    if (isStarted) makeApiRequest();

                    break;
                case WORN_STATUS:
                    String wornText = msg.getData().getString("isWorn");
                    System.out.println("worntext "+wornText);
                    tv = findViewById(R.id.labelWorn);
                    if (wornText.equals("1")){
                        tv.setText("Yes");
                    } else  {
                        tv.setText("No");
                    }

                    break;

            }
        }

    };

    private String paramsInURL(JSONObject params) {
        StringBuilder encodedParams = new StringBuilder();
        encodedParams.append("?");
        Iterator<String> keys = params.keys();


        boolean first = true;

        while (keys.hasNext()) {
            String key = keys.next();
            String value = null;
            try {
                value = params.getString(key);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (!first) {
                encodedParams.append("&");
            }

            encodedParams.append(key)
                    .append("=")
                    .append(value);
            first = false;
        }

        return encodedParams.toString();
    }

    private void makeApiRequest(){
        //String apiUrl = "https://api.jprokopakis.gr/random-forest"+paramsInURL(jsonParams);
        //System.out.println(apiUrl);
        try {
            jsonParams.put("HR", _NConnListener.jsonObj.getString("HR"));
            jsonParams.put("BR", _NConnListener.jsonObj.getString("BR"));
            jsonParams.put("Posture", _NConnListener.jsonObj.getString("Posture"));
            jsonParams.put("Activity", _NConnListener.jsonObj.getString("Activity"));
            jsonParams.put("Acceleration", _NConnListener.jsonObj.getString("Acceleration"));
            jsonParams.put("XMin", _NConnListener.jsonObj.getString("XMin"));
            jsonParams.put("XPeak", _NConnListener.jsonObj.getString("XPeak"));
            jsonParams.put("YMin", _NConnListener.jsonObj.getString("YMin"));
            jsonParams.put("YPeak", _NConnListener.jsonObj.getString("YPeak"));
            jsonParams.put("ZMin", _NConnListener.jsonObj.getString("ZMin"));
            jsonParams.put("ZPeak", _NConnListener.jsonObj.getString("ZPeak"));
            /*--------------------------------------------------------------------------------------------*/
            //jsonToCsv = jsonParams;
            jsonToCsv = new JSONObject(jsonParams.toString());
            jsonToCsv.put("phone_time", _NConnListener.jsonObj.getString("phone_time"));
            jsonToCsv.put("timestamp", _NConnListener.jsonObj.getString("timestamp"));

        } catch (JSONException e){
            e.printStackTrace();
        }
        String apiUrl = "https://api.jprokopakis.gr/random-forest"+paramsInURL(jsonParams);
        //System.out.println(apiUrl);
        System.out.println(jsonParams);
        jsonObjectsList.add(jsonToCsv);
        //System.out.println(jsonObjectsList);
        new ApiRequestTask(MainActivity.this).execute(apiUrl);
    }

    @Override
    public void onApiResponse(String result) {
    // Handle the API response here and update the UI
        if (result != null) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                String value = jsonResponse.getString("1");

                System.out.println("from main activity");
                calsList.add(Float.parseFloat(value));
                calories += Float.parseFloat(value);

                TextView calsTv = findViewById(R.id.labelCals);
                calsTv.setText(decimalFormat.format(calories));

                data.setY(calories);
                series.addPoint(data,true,false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // Handle the case where the API request fails
            // You can show an error message or retry the request, etc.

        }
    }

    public void exportToCSV(View view) {
        saveToCSV();
        System.out.println("clicked");
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToCSV() {
        try {
            FileWriter csvWriter = new FileWriter(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), simpleDateFormat.format(new Date(System.currentTimeMillis())) + "data.csv"));
            //System.out.println(jsonObjectsList);
            // Get the keys dynamically from the first JSON object in the list
            JSONObject firstJsonObject = jsonObjectsList.get(0);
            Iterator<String> keysIterator = firstJsonObject.keys();

            // Write column headers to the CSV file
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                csvWriter.append(key);
                csvWriter.append(",");

            }
            csvWriter.append("cals");
            csvWriter.append("\n");

            // Iterate through the list of JSON objects and write values to the CSV file
            int i=0;
            for (JSONObject jsonObject : jsonObjectsList) {
                keysIterator = jsonObject.keys();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    String value = jsonObject.optString(key, "");
                    csvWriter.append(value);
                    csvWriter.append(",");

                }
                csvWriter.append(calsList.get(i).toString());
                csvWriter.append("\n");
                i++;
            }

            csvWriter.flush();
            csvWriter.close();

            showToast("CSV file saved successfully");
            System.out.println("CSV file saved successfully");

        } catch (Exception e) {
            e.printStackTrace();
            showToast("ERROR while trying to save");
            System.out.println("ERROR while trying to save");
        }
    }

    private void saveData(String[] strings) {
        JSONArray jsonArray = new JSONArray();
        for (String str : strings) {
            jsonArray.put(str);
        }

        String jsonString = jsonArray.toString();

        String filename = "data.json";
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(jsonString);
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] loadData() {
        String filename = "data.json";
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0]; // Return an empty array if data cannot be loaded
        }

        try {
            JSONArray jsonArray = new JSONArray(sb.toString());
            String[] strings = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                strings[i] = jsonArray.getString(i);
            }
            return strings;
        } catch (JSONException e) {
            e.printStackTrace();
            return new String[0]; // Return an empty array if JSON parsing fails
        }
    }



    public void startRec(View view){
        /*
        Intent i = new Intent(this, SecondActivity.class);

        i.putExtra("Cool", "this message");
        startActivity(i);
        */
        isStarted = true;
    }

    public void launchProfile(View view){
        Intent i = new Intent(this, ProfileActivity.class);

        i.putExtra("Cool", "this message");
        startActivityForResult(i, REQUEST_CODE_PROFILE_ACTIVITY);

    }

    // Override onActivityResult() method to retrieve data from second activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                // Retrieve data from the intent
                gender = data.getStringExtra("gender");
                height = data.getStringExtra("height");
                weight = data.getStringExtra("weight");
                dateString = data.getStringExtra("dateString");

                // Do something with the received data
                // For example, update UI or perform any other operation
            }
        }
    }





}





