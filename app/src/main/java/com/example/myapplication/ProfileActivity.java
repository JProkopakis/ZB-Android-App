package com.example.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;



public class ProfileActivity extends Activity {

    private String height;
    private String weight;
    private String gender;
    private EditText birthday;
    private String dateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent i = getIntent();
        System.out.println(i);
        //String message = i.getStringExtra("Cool");

        //((TextView)findViewById(R.id.titleView)).setText(message);
        String[] userValues = loadData();

        if (userValues.length != 0){
            System.out.println("mpainw na kanw set data");
            EditText et = (EditText) findViewById(R.id.labelGender);
            et.setText(userValues[0]);
            et = (EditText) findViewById(R.id.labelHeight);
            et.setText(userValues[1]);
            et = (EditText) findViewById(R.id.labelWeight);
            et.setText(userValues[2]);
            et = (EditText) findViewById(R.id.labelBirthday);
            et.setText(userValues[3]);
        }
        /*
        EditText et = (EditText) findViewById(R.id.labelGender);
        gender = et.getText().toString();
        */

        birthday = findViewById(R.id.labelBirthday);

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the current date
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new DatePickerDialog instance
                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                                // Update the EditText with the selected date
                                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                birthday.setText(selectedDate);
                            }
                        }, year, month, day);

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {

        EditText et = findViewById(R.id.labelGender);
        gender = et.getText().toString();
        et = findViewById(R.id.labelHeight);
        height = et.getText().toString();
        et = findViewById(R.id.labelWeight);
        weight = et.getText().toString();
        et = findViewById(R.id.labelBirthday);
        dateString = et.getText().toString();

        saveData(new String[]{gender, height, weight, dateString});

        // Create an intent to store the data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("gender", gender);
        resultIntent.putExtra("height", height);
        resultIntent.putExtra("weight", weight);
        resultIntent.putExtra("dateString", dateString);

        // Set result to be sent back to main activity
        setResult(Activity.RESULT_OK, resultIntent);

        // Finish the activity
        super.onBackPressed(); // This will call the default back button behavior (finish activity)
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



}
