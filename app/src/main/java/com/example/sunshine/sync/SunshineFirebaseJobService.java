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
package com.example.sunshine.sync;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;


// COMPLETED (2) Make sure you've imported the jobdispatcher.JobService, not job.JobService

// COMPLETED (3) Add a class called SunshineFirebaseJobService that extends jobdispatcher.JobService

public class SunshineFirebaseJobService extends ListenableWorker {

//  COMPLETED (4) Declare an ASyncTask field called mFetchWeatherTask
    private AsyncTask<Void, Void, Void> mFetchWeatherTask;
    private SettableFuture<Result> mFuture;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public SunshineFirebaseJobService(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }
//  COMPLETED (5) Override onStartJob and within it, spawn off a separate ASyncTask to sync weather data
    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */


//  COMPLETED (7) Override onStopJob, cancel the ASyncTask if it's not null and return true
    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
//    @Override
//    public boolean onStopJob(JobParameters jobParameters) {
//        if (mFetchWeatherTask != null) {
//            mFetchWeatherTask.cancel(true);
//        }
//        return true;
//    }


    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        mFuture = SettableFuture.create();

        mFetchWeatherTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                SunshineSyncTask.syncWeather(context);


                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void onPostExecute(Void aVoid) {
                //  COMPLETED (6) Once the weather data is sync'd, call jobFinished with the appropriate arguements
                mFuture.set(Result.success());
            }
        };

         mFetchWeatherTask.execute();
        return mFuture;
    }
}