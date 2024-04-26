package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiRequestTask extends AsyncTask<String, Void, String> {

    private ApiCallback callback;

    public ApiRequestTask(ApiCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String apiUrl = params[0];
        //System.out.println(params);
        try {
            URL url = new URL(apiUrl);
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            // Get the response from the API
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();


            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            System.out.println("API Response: " + result);
        } else {
            System.out.println("API Request Failed");
        }
        if (callback != null) {
            callback.onApiResponse(result);
        }
    }

}

