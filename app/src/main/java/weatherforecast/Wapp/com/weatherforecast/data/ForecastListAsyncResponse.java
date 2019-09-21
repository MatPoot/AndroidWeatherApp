package weatherforecast.Wapp.com.weatherforecast.data;

import java.util.ArrayList;

import weatherforecast.Wapp.com.weatherforecast.model.Forecast;



public interface ForecastListAsyncResponse {
    void processFinished(ArrayList<Forecast> forecastArrayList);
}
