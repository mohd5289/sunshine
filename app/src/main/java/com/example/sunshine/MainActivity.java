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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.data.SunshinePreferences;
import com.example.sunshine.data.WeatherContract;
import com.example.sunshine.sync.SunshineSyncUtils;
import com.example.sunshine.utilities.NetworkUtils;
import com.example.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;


public class MainActivity extends AppCompatActivity
        implements ForecastAdapter.forecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>
       {
           private static final int ID_FORECAST_LOADER = 44;
           public static final String[] MAIN_FORECAST_PROJECTION = {
                   WeatherContract.WeatherEntry.COLUMN_DATE,
                   WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                   WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                   WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
           };
           public static final int INDEX_WEATHER_DATE = 0;
           public static final int INDEX_WEATHER_MAX_TEMP = 1;
           public static final int INDEX_WEATHER_MIN_TEMP = 2;
           public static final int INDEX_WEATHER_CONDITION_ID = 3;
           private int mPosition = RecyclerView.NO_POSITION;
           private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
     private  TextView mWeatherTextView;
     private TextView mErrorMessage;
     private ProgressBar mProgressBar;
     private RecyclerView mRecyclerView;
     private ForecastAdapter mForecastAdapter;
    private static final int FORECAST_LOADER_ID = 44;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */


        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_forecast);
        mErrorMessage = (TextView)findViewById(R.id.tv_error_message_display);

        mProgressBar = (ProgressBar)findViewById(R.id.pb_loading_indicator);
        // COMPLETED (4) Delete the dummy weather data. You will be getting REAL data from the Internet in this lesson.

        // COMPLETED (3) Delete the for loop that populates the TextView with dummy data
       mForecastAdapter = new ForecastAdapter(this, this);

        int loaderId = FORECAST_LOADER_ID;

        LoaderManager.LoaderCallbacks<Cursor> callback = MainActivity.this;

        Bundle bundleForLoader = null;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

       mRecyclerView.setLayoutManager(layoutManager);


       mRecyclerView.setHasFixedSize(true);
       mRecyclerView.setAdapter(mForecastAdapter);
        // COMPLETED (9) Call loadWeatherData to perform the network request to get the weather
        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
        getSupportLoaderManager().initLoader(loaderId, bundleForLoader, MainActivity.this);
        Log.d(TAG, "onCreate: registering preference changed listener");
        SunshineSyncUtils.initialize(this);

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





          private void showLoading() {
               /* Then, hide the weather data */
               mRecyclerView.setVisibility(View.INVISIBLE);
               /* Finally, show the loading indicator */
               mProgressBar.setVisibility(View.VISIBLE);
           }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

                 switch (id) {

//          COMPLETED (22) If the loader requested is our forecast loader, return the appropriate CursorLoader
                case ID_FORECAST_LOADER:
                    /* URI for all rows of weather data in our weather table */
                    Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                    /* Sort order: Ascending by date */
                    String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                    /*
                     * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                     * want all weather data from today onwards that is stored in our weather table.
                     * We created a handy method to do that in our WeatherEntry class.
                     */
                    String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                    return new CursorLoader(this,
                            forecastQueryUri,
                            MAIN_FORECAST_PROJECTION,
                            selection,
                            null,
                            sortOrder);

                default:
                    throw new RuntimeException("Loader Not Implemented: " + id);
            }

        };


           @Override
           public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
               mForecastAdapter.swapCursor(data);
//      COMPLETED (29) If mPosition equals RecyclerView.NO_POSITION, set it to 0
               if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
//      COMPLETED (30) Smooth scroll the RecyclerView to mPosition
               mRecyclerView.smoothScrollToPosition(mPosition);

//      COMPLETED (31) If the Cursor's size is not equal to 0, call showWeatherDataView
               if (data.getCount() != 0) showWeatherDataView();
           }

           @Override
           public void onLoaderReset(@NonNull Loader<Cursor> loader) {
               mForecastAdapter.swapCursor(null);
           }




            // COMPLETED (5) Create a class that extends AsyncTask to perform network requests


            private void openLocationInMap() {
                String addressString  = SunshinePreferences.getPreferredWeatherLocation(this);;
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


    @Override
    protected void onStart() {
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(TAG, "onStart: preferences were updated");
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);

            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */

    }


           @Override
           public void onClick(long date) {
               Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
//      COMPLETED (39) Refactor onClick to pass the URI for the clicked date with the Intent
               Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
               weatherDetailIntent.setData(uriForDateClicked);
               startActivity(weatherDetailIntent);
           }
       }