package weatherforecast.Wapp.com.weatherforecast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weatherforecast.Wapp.com.weatherforecast.data.ForecastData;
import weatherforecast.Wapp.com.weatherforecast.model.Forecast;
import weatherforecast.Wapp.com.weatherforecast.data.ForecastListAsyncResponse;
import weatherforecast.Wapp.com.weatherforecast.data.ForecastViewPagerAdapter;
import weatherforecast.Wapp.com.weatherforecast.util.Prefs;

public class MainActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private ForecastViewPagerAdapter forecastViewPagerAdapter;
    private ViewPager viewPager;
    private TextView locationText;
    private TextView currentTempText, currentDate;
    private EditText userLocationText;
    private String userEnteredString;
    private ImageView icon;
    private LocationManager locationmanager=null;
    private LocationListener locationListener=null;

    private static final long INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = findViewById(R.id.locationTextViewId);
        currentTempText = findViewById(R.id.currentTempId);
        currentDate = findViewById(R.id.currentDateId);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        icon = findViewById(R.id.weatherIcon);


        final Prefs prefs = new Prefs(this);
        String prefsLocation = prefs.getLocation();
        getWeather(prefsLocation);

        userLocationText = findViewById(R.id.locationNameId);

        userLocationText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {

                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                         && (keycode == KeyEvent.KEYCODE_ENTER)) {

                    userEnteredString = userLocationText.getText().toString();
                    prefs.setLocation(userEnteredString);
                    getWeather(userEnteredString);

                    return true;
                }

                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation=location;

    }



    void getCurrentLocation() {
        if (mCurrentLocation != null) {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());
            Toast.makeText(this, "Retrieving Location", Toast.LENGTH_LONG).show();
            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String addressa = addresses.get(0).getAddressLine(0); // If any additional mLocation line present than only, check with max available mLocation lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                userLocationText.setText(city);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            7);
                    try {
                        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, this);
                    } catch (SecurityException e) {
                        ;
                    }

                }
            }


        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    7);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
       // PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);
        try {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,mLocationRequest, this);


        } catch (SecurityException e) {
        }
        /*This will need to check for permission, may be red but will still work, put in code to check
          location permissions. */
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private String getImageUrl(String html) {

        String imgRegex = "(?i)<img[^>]+?src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
       // String htmlString = "<![CDATA[<img src=\"http://l.yimg.com/a/i/us/we/52/27.gif\"/> <BR /> <b>Current Conditions:</b> <BR />Mostly Cloudy <BR /> <BR /> <b>Forecast:</b> <BR /> Sat - Rain. High: 78Low: 74 <BR /> Sun - Mostly Cloudy. High: 78Low: 77 <BR /> Mon - Partly Cloudy. High: 78Low: 76 <BR /> Tue - Sunny. High: 78Low: 74 <BR /> Wed - Sunny. High: 80Low: 72 <BR /> <BR /> <a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Country__Country/*https://weather.yahoo.com/country/state/city-1545228/\">Full Forecast at Yahoo! Weather</a> <BR /> <BR /> <BR /> ]]>";
        String imgSrc = null;

        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(html);

        while(m.find()) {
             imgSrc = m.group(1);

        }
        return imgSrc;
    }

    private void getWeather(String currentLocation) {



        forecastViewPagerAdapter = new ForecastViewPagerAdapter(getSupportFragmentManager(),
                getFragments(currentLocation));

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(forecastViewPagerAdapter);

    }

    private List<Fragment> getFragments(String locationString) {
        final List<Fragment> fragmentList = new ArrayList<>();

        new ForecastData().getForecast(new ForecastListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Forecast> forecastArrayList) {

                fragmentList.clear();
                String html = forecastArrayList.get(0).getDescriptionHTML();

                Picasso.with(getApplicationContext())
                        .load(getImageUrl(html))
                        .into(icon);

                locationText.setText(String.format("%s,\n%s", forecastArrayList.get(0).getCity(),
                        forecastArrayList.get(0).getRegion()));
                currentTempText.setText(String.format("%sF", forecastArrayList.get(0).getCurrentTemperature()));
                String[] date = forecastArrayList.get(0).getDate().split(" ");
                //Fri, 17 Nov 2017 03:00 PM PST
                String splitDate = "Today " + date[0] + " " + date[1] + " " +date[2] + " " + date[3];


                currentDate.setText(splitDate);


                for (int i = 0; i < forecastArrayList.size(); i++) {
                    Forecast forecast = forecastArrayList.get(i);
                    ForecastFragment forecastFragment =
                            ForecastFragment.newInstance(forecast);

                    fragmentList.add(forecastFragment);

                }
                forecastViewPagerAdapter.notifyDataSetChanged();


            }
        }, locationString);
        return fragmentList;
    }

    public void showlocation(View view) {
        getCurrentLocation();
    }
}
