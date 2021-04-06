package com.example.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.data.WeatherContract;
import com.example.sunshine.utilities.*;

public class ForecastAdapter extends RecyclerView.Adapter <ForecastAdapter.ForecastAdapterViewHolder> {


    private final forecastAdapterOnClickHandler mClickHandler;
  private final Context mContext;
    private Cursor mCursor;
    public interface forecastAdapterOnClickHandler{
        void onClick(long date);

    }

    public ForecastAdapter(forecastAdapterOnClickHandler clickHandler, Context mContext){
        mClickHandler =clickHandler;
        this.mContext = mContext;
    }

    public  class ForecastAdapterViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        //      COMPLETED (5) Add an ImageView for the weather icon
        final ImageView iconView;

        public ForecastAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            iconView = (ImageView) itemView.findViewById(R.id.weather_icon);
            dateView = (TextView) itemView.findViewById(R.id.date);
            descriptionView = (TextView) itemView.findViewById(R.id.weather_description);
            highTempView = (TextView) itemView.findViewById(R.id.high_temperature);
            lowTempView = (TextView) itemView.findViewById(R.id.low_temperature);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
//          COMPLETED (37) Instead of passing the String for the clicked item, pass the date from the cursor
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }



    }

    @NonNull
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.forecast_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem,parent,shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;

        weatherImageId = SunshineWeatherUtils
                .getSmallArtResourceIdForWeatherCondition(weatherId);

        holder.iconView.setImageResource(weatherImageId);


        /*******************
         * Weather Summary *
         *******************/
//      COMPLETED (7) Generate a weather summary with the date, description, high and low
        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

        /* Display friendly date string */
        holder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        holder.highTempView.setText(highString);
        holder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();

    }
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
//      COMPLETED (12) After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }


}
