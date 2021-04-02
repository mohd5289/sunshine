/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.sunshine;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.data.SunshinePreferences;
import com.example.sunshine.utilities.NetworkUtils;
import com.example.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.sunshine.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements ForecastAdapter.forecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String []> {

     private  TextView mWeatherTextView;
     private TextView mErrorMessage;
     private ProgressBar mProgressBar;
     private RecyclerView mRecyclerView;
     private ForecastAdapter mForecastAdapter;
    private static final int FORECAST_LOADER_ID = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_forecast);
        mErrorMessage = (TextView)findViewById(R.id.tv_error_message_display);

        mProgressBar = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        // COMPLETED (4) Delete the dummy weather data. You will be getting REAL data from the Internet in this lesson.

        // COMPLETED (3) Delete the for loop that populates the TextView with dummy data
       mForecastAdapter = new ForecastAdapter(this);

        int loaderId = FORECAST_LOADER_ID;

        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;

        Bundle bundleForLoader = null;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

       mRecyclerView.setLayoutManager(layoutManager);


       mRecyclerView.setHasFixedSize(true);
       mRecyclerView.setAdapter(mForecastAdapter);
        // COMPLETED (9) Call loadWeatherData to perform the network request to get the weather
        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, callback);
    }

    // COMPLETED (8) Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData() {
        showWeatherDataView();
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
     //   new FetchWeatherTask().execute(location);
    }
    private void showWeatherDataView(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
  }
  private  void showErrorMessage(){
        mErrorMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
  }

    @Override
    public void onClick(String weatherForTheDay) {
        Context context = this;
        Toast.makeText(context, weatherForTheDay, Toast.LENGTH_SHORT);
    Intent detailToStartActivityIntent = new Intent(this,DetailActivity.class);
    detailToStartActivityIntent.putExtra(Intent.EXTRA_TEXT, weatherForTheDay);
    startActivity(detailToStartActivityIntent);
    }





    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, @Nullable Bundle args) {

        return new AsyncTaskLoader<String[]>(this) {


            /* This String array will hold and help cache our weather data */
            String[] mWeatherData = null;

            // COMPLETED (3) Cache the weather data in a member variable and deliver it in onStartLoading.

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            @Override
            protected void onStartLoading() {
                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                String locationQuery = SunshinePreferences
                        .getPreferredWeatherLocation(MainActivity.this);

                URL weatherRequestUrl = NetworkUtils.buildUrl(locationQuery);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }


            }

            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }
            @Override
            public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mForecastAdapter.setWeatherData(data);
                if (null == data) {
                    showErrorMessage();
                } else {
                    showWeatherDataView();
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<String[]> loader) {

            }
            // COMPLETED (5) Create a class that extends AsyncTask to perform network requests


            private void openLocationInMap() {
                String addressString = "1600 Ampitheatre Parkway, CA";
                Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Couldn't call " + geoLocation.toString()
                            + ", no receiving apps installed!");
                }
            }

            @Override
            public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater menuInflater = getMenuInflater();

                menuInflater.inflate(R.menu.forecast, menu);


                return true;
            }

            @Override
            public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_refresh) {
                    mForecastAdapter.setWeatherData(null);
                    loadWeatherData();
                    return true;
                }
                if (id == R.id.action_map) {
                    openLocationInMap();
                    return true;
                }
                if( id == R.id.action_settings){
                    Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);

                    return true;
                }

                return super.onOptionsItemSelected(item);
            }


        }