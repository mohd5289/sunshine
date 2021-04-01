package com.example.sunshine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ForecastAdapter extends RecyclerView.Adapter <ForecastAdapter.ForecastAdapterViewHolder> {

    private String [] mWeatherData;
    private final forecastAdapterOnClickHandler mClickHandler;


    public interface forecastAdapterOnClickHandler{
        void onClick(String weatherForTheDay);

    }

    public ForecastAdapter(forecastAdapterOnClickHandler clickHandler){
        mClickHandler =clickHandler;
    }

    public  class ForecastAdapterViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        public  final TextView mWeatherTextView;

        public ForecastAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

        mWeatherTextView = (TextView)itemView.findViewById(R.id.tv_weather_data);
        itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
        int adapterPosition =getAdapterPosition();
        String weatherForDay = mWeatherData[adapterPosition];
        mClickHandler.onClick(weatherForDay);
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
    String weatherForThisDay = mWeatherData[position];
    holder.mWeatherTextView.setText(weatherForThisDay);
    }

    @Override
    public int getItemCount() {
       if(null==mWeatherData) return 0;

        return mWeatherData.length;
    }


 public void setWeatherData(String[] weatherData){
        mWeatherData=weatherData;
        notifyDataSetChanged();
  }
}
