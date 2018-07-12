package assra.bahria.fyp.Patient.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import assra.bahria.fyp.Common.Models.CombinedDirectionAndDuration;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Duration;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.DistanceMatrixResult;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.Element;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.Row;
import assra.bahria.fyp.Common.Networking.CommonRoutes;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.PolyLineParser;
import assra.bahria.fyp.Common.Utils.RxOkhttpWrapper;
import assra.bahria.fyp.Patient.Activities.AmbulanceTimerActivity;
import assra.bahria.fyp.Patient.Models.CallAmbulanceStatus;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Directions;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Leg;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Route;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Step;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
import assra.bahria.fyp.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class CallAmbulanceFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "CallAmbulanceFragment";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;
    private MaterialDialog driverInfoDialog;

    private AppCompatButton callAmbulanceButton,viewDriverInfoButton;
    private TextView etaTextView;

    private boolean isAlreadyLoaded = false;
private OkHttpClient client;

    double lat = 0.0, lng = 0.0;

    Location origin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.patient_fragment_call_ambulance, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setUpTextViews();
        setUpButtons();
        setUpListeners();
        setUpOkHttpClient();
        builddriverInfoDialog();
        buildErrorDialog();
        buildLoadingDialog();
        if (!isAlreadyLoaded) {

            if (isServicesOK()) {
                getLocationPermission();
            }

            isAlreadyLoaded = true;
        } else {
            // already loaded
        }

    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

   private void setUpTextViews(){
        etaTextView = getView().findViewById(R.id.etaTextView);
        etaTextView.setVisibility(View.GONE);
    }

    private void setUpButtons() {
        callAmbulanceButton = getView().findViewById(R.id.callAmbulanceButton);
        viewDriverInfoButton = getView().findViewById(R.id.viewDriverInfoButton);
        callAmbulanceButton.setClickable(false);
        viewDriverInfoButton.setVisibility(View.GONE);

    }

    private void setUpListeners() {



        viewDriverInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showdriverInfoDialog();
            }
        });




        callAmbulanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Toast.makeText(getContext(), "Call Ambulance", Toast.LENGTH_SHORT).show();


                Intent timerActivityIntent = new Intent(getActivity(), AmbulanceTimerActivity.class);

                Bundle bundle = new Bundle();
                bundle.putDouble("lat", lat);
                bundle.putDouble("lng", lng);

                timerActivityIntent.putExtras(bundle);
                startActivityForResult(timerActivityIntent,1);

                getActivity().overridePendingTransition(R.anim.fade_in_slideup,R.anim.no_animation);
                /*  Todo :  Ider Modal khulega on top of this fragment implementation jese marzi ho but UI layout/patient_activity_call_ambulance.xml jese honi chaiyay without the
                logo aur image just the button aur text ho cancel button hogi aur uske click krne pe timer cancel aur modal band
                Other wise network request bhejega wo request ki jaga filhal toast krdi siki api banara hai

                 Farz kar request bhejdi ab kahen na kahen se notification aegi (FIREBASE) jo ke baategi ke driver ara hai us notification
                 me wo data hoga jo ke map ke andar aik ROUTE dikha sake wo ROUTE driver ki initial location dega lat long me  usko use krte we
                 driver ke lat long tak route dikhana jese hum normally maps use krte

                 Yeh scene hai jo bhi kam hoga koshish kar isi fragment ke andar ho acitivty already bharawa hai user ki id
                 ka scene ye hoga

                 // gps lat long ka scene ye krsakta ke jab bhi app kholta gps location lele instead jab koi activity khulti teri marzi
                 // uske according shit change krni pare
                 // Uska code ider para  assra.bahria.fyp.Common.Utils.GPSTracker

                 isko ya to app start pe chala aur pateint app state manager me lat long dalde ye phir is fragment ke start pe chala de

                 // chalane ki example DriverMainActivity me hai


                 // user id ider se milegi
                  PatientAppStateManager.getInstance().getUserID();

                */


            }
        });
    }


    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getActivity(), "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true);

        } else {
            //
        }
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                             moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                            origin  = new Location(currentLocation);





                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        callAmbulanceButton.setClickable(true);

        lat = latLng.latitude;
        lng = latLng.longitude;



    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, getActivity() == null ? "Activity is null" : "not null");

//        if (!isAlreadyLoaded) {
//
//            if ( isServicesOK() ) {
//                getLocationPermission();
//            }
//
//            isAlreadyLoaded = true;
//        } else {
//            // already loaded
//        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, getActivity() == null ? "on attach Activity is null" : "on attach not null");


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);






        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                // with result


               String callAbulanceStatusString =  data.getStringExtra("CallAmbulanceStatus");

               // convert to directions object

                Gson gson =  new Gson();

                final CallAmbulanceStatus callAbulanceStatus = gson.fromJson(callAbulanceStatusString,CallAmbulanceStatus.class);

               if (callAbulanceStatusString != null && callAbulanceStatus != null){


                   Toast.makeText(getContext(), callAbulanceStatusString, Toast.LENGTH_LONG).show();
                   if (callAbulanceStatus.getStatus() != 1){
                       showErrorDialog("No Driver Available","No driver avaliable at current time please try again later.");
                       return;
                   }

                   setShowDriverInfoDialogItems(callAbulanceStatus);
                   viewDriverInfoButton.setVisibility(View.VISIBLE);
                   // render path

                   // origin obtained destination obtained now make client call

                   String originString = origin.getLatitude() + "," + origin.getLongitude();
                   String destinationString = callAbulanceStatus.getLat() + "," + callAbulanceStatus.getLng();


                   HttpUrl directionsURL = HttpUrl.parse(PatientRoutes.mapDirections).newBuilder()
                           .addQueryParameter("origin",originString)
                           .addQueryParameter("destination",destinationString)
                           .addQueryParameter("mode","driving")
                           .addQueryParameter("key",getResources().getString(R.string.google_maps_directions_api_key)).build();

                   HttpUrl distanceMatrixURL = HttpUrl.parse(CommonRoutes.distanceMatrixRoute).newBuilder()
                           .addQueryParameter("origins",originString)
                           .addQueryParameter("destinations",destinationString)
                           .addQueryParameter("key",getResources().getString(R.string.google_maps_distance_matrix_api_key)).build();

//                   try {b
//                       showLoadingDialog();
//                       getDirectionsCall(originString, destinationString, "driving", getResources().getString(R.string.google_maps_directions_api_key));
//                   }catch (Exception x){
//                       hideLoadingDialog();
//                       showErrorDialog("Exception", x.getMessage());
//                   }

                   getDirectionsAndDuration(directionsURL,distanceMatrixURL);


               }


                // render path on map in this fragment !!!!
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //no result
                Toast.makeText(getContext(), "Ambulance call canceled by user !", Toast.LENGTH_LONG).show();
            }
        }







    }


    private void createRoute(){




    }

    private void removePolyLine(){
    }



    private void setUpOkHttpClient(){


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }

    private void getDirectionsCall(final String origin, final String destination, String mode, final String key  ) throws  Exception{




        HttpUrl url = HttpUrl.parse(PatientRoutes.mapDirections).newBuilder()
                .addQueryParameter("origin",origin)
                .addQueryParameter("destination",destination)
                .addQueryParameter("mode",mode)
                .addQueryParameter("key",key).build();

        //new HttpUrl.Builder().





        Log.d(TAG,"lksdjaslkjdklsajdlasjasj" + url.toString());

        final Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d(TAG,"lksdjaslkjdklsajdlasjasj" + request.url().toString());

        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final String  message = e.getMessage();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        hideLoadingDialog();
                        showErrorDialog("Exception", message);
                    }
                });
            }

            @Override public void onResponse(Call call, final Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                showErrorDialog("Server Error", "Server error code : " + response.code());
                            }
                        });

                        throw new IOException("Unexpected code " + response);
                    }




                    Log.d(TAG,"lksdjaslkjdklsajdlasjasj" + request.url().toString());


                    if (responseBody == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                showErrorDialog("Server Error", "Response body is null error occured");
                            }
                        });
                        return;
                    }

                    String result =  responseBody.string();

                    Gson gson = new Gson();
                    final Directions directions = gson.fromJson(result,Directions.class);

//                    final int statusInt;
//
//                    try {
//                        statusInt = Integer.parseInt(status.getStatus());
//                    }catch (Exception x) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                hideLoadingDialog();
//                                showErrorDialog("Server Error", "Could not parse status int!");
//                            }
//                        });
//                        return;
//                    }


                    if (!directions.getStatus().equals("OK")) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Directions Error", "Could not obtain gps directions from Google API");
                            }
                        });

                    } else if  (directions.getStatus().equals("OK")){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Obtained Directions", Toast.LENGTH_LONG).show();
                                //DriverAppStateManager.getInstance().setUserID(statusInt);
                                //openMainActivity();


                                Log.d(TAG,String.valueOf(directions) + " :END:");



                                List<List<HashMap<String, String>>> routes =   getRoutez(directions);
                                renderRoute(routes);

                                try
                                {
                                    showLoadingDialog();
                                    getDurationCall(origin,destination,getResources().getString(R.string.google_maps_distance_matrix_api_key));
                                }catch (Exception x){
                                    hideLoadingDialog();
                                    showErrorDialog("Server Error","Server Error Could not get ETA");

                                }





//                                ArrayList<LatLng> points = new ArrayList<>();
//                                PolylineOptions lineOptions = null;
//
//                                for (int i = 0; i < directions.getRoutes().size(); i++) {
//                                    points = new ArrayList<>();
//                                    lineOptions = new PolylineOptions();
//
//                                    Route route = directions.getRoutes().get(i);
//
//
//
//
//                                }
//
//                                lineOptions.addAll(points);
//                                lineOptions.width(10);
//                                lineOptions.color(Color.RED);
//                                mMap.addPolyline(lineOptions);



                            }
                        });


                    } else {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                showErrorDialog("Server Error", "An Unknown error occurred ");
                            }
                        });
                    }


                }
            }
        });

    }

    private void getDurationCall(String origins, String destinations, String key) throws  Exception {
        HttpUrl url = HttpUrl.parse(CommonRoutes.distanceMatrixRoute).newBuilder()
                .addQueryParameter("origins",origins)
                .addQueryParameter("destinations",destinations)
                .addQueryParameter("key",key).build();

        Log.d(TAG,"lksdjaslkjdklsajdlasjasj" + url.toString());

        final Request request = new Request.Builder()
                .url(url)
                .build();

        Log.d(TAG,"lksdjaslkjdklsajdlasjasj" + request.url().toString());


        client.newCall(request).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final String  message = e.getMessage();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        hideLoadingDialog();
                        showErrorDialog("Exception", message);
                    }
                });
            }

            @Override public void onResponse(Call call, final Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                etaTextView.setVisibility(View.GONE);
                                hideLoadingDialog();
                                showErrorDialog("Server Error", "Server error code : " + response.code());
                            }
                        });

                        throw new IOException("Unexpected code " + response);
                    }







                    if (responseBody == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                etaTextView.setVisibility(View.GONE);
                                hideLoadingDialog();
                                showErrorDialog("Server Error", "Response body is null error occured");
                            }
                        });
                        return;
                    }

                    String result =  responseBody.string();

                    Gson gson = new Gson();
                    final DistanceMatrixResult distanceMatrixResult = gson.fromJson(result,DistanceMatrixResult.class);

//                    final int statusInt;
//
//                    try {
//                        statusInt = Integer.parseInt(status.getStatus());
//                    }catch (Exception x) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                hideLoadingDialog();
//                                showErrorDialog("Server Error", "Could not parse status int!");
//                            }
//                        });
//                        return;
//                    }


                    if (!distanceMatrixResult.getStatus().equals("OK")) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                etaTextView.setVisibility(View.GONE);
                                showErrorDialog("Distance Matrix Error", "Could not obtain Distance Matrix from Google API");
                            }
                        });

                    } else if  (distanceMatrixResult.getStatus().equals("OK")){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Obtained Directions", Toast.LENGTH_LONG).show();

                                // make etaTextViewVisible and set its text

                                etaTextView.setVisibility(View.VISIBLE);
                                etaTextView.setText(distanceMatrixResult.getRows().get(0).getElements().get(0).getDuration().getText());
                            }
                        });


                    } else {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                etaTextView.setVisibility(View.GONE);
                                showErrorDialog("Server Error", "An Unknown error occurred ");
                            }
                        });
                    }


                }
            }
        });

    }



    private void getDirectionsAndDuration(HttpUrl directionsUrl,HttpUrl distanceMatrixUrl) {



        Observable.zip(RxOkhttpWrapper.getResponse(distanceMatrixUrl).map(getResponseDistanceMatrixResultMappingFunction()),
                RxOkhttpWrapper.getResponse(directionsUrl).map(getResponseDirectionMappingFunction()),
        getCombinedDirectionAndDurationResultBiFunction()).observeOn(AndroidSchedulers.mainThread()).
                        subscribeOn(Schedulers.io()).subscribe(new Observer<CombinedDirectionAndDuration>() {
            @Override
            public void onSubscribe(Disposable d) {

                showLoadingDialog();
            }

            @Override
            public void onNext(CombinedDirectionAndDuration combinedDirectionAndDuration) {

                // have both things !!! in the wrapper

                // render route
                List<List<HashMap<String, String>>> routes =   getRoutez(combinedDirectionAndDuration.getDirections());
                renderRoute(routes);

                // set eta time
                Row row = combinedDirectionAndDuration.getDistanceMatrixResult().getRows().get(0);
                Element element = row.getElements().get(0);
                assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.Duration duration = element.getDuration();
                etaTextView.setText("ETA : " + duration.getText() );
                etaTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                hideLoadingDialog();
                showErrorDialog("Server Error",e.toString());


                etaTextView.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                hideLoadingDialog();
            }
        });



    }

    private Function<Response, DistanceMatrixResult>  getResponseDistanceMatrixResultMappingFunction(){
     return new Function<Response, DistanceMatrixResult>() {
            @Override
            public DistanceMatrixResult apply(Response response) throws Exception {

                    DistanceMatrixResult distanceMatrixResult = new Gson().fromJson(response.body().string(),DistanceMatrixResult.class);
                    return  distanceMatrixResult;

            }
        };



    }

    private Function<Response, Directions>  getResponseDirectionMappingFunction(){
        return new Function<Response, Directions>() {
            @Override
            public Directions apply(Response response) throws Exception {

                Directions directions = new Gson().fromJson(response.body().string(),Directions.class);
                return  directions;

            }
        };



    }

    private BiFunction<DistanceMatrixResult, Directions, CombinedDirectionAndDuration> getCombinedDirectionAndDurationResultBiFunction(){
       return new BiFunction<DistanceMatrixResult, Directions, CombinedDirectionAndDuration>() {
           @Override
           public CombinedDirectionAndDuration apply(DistanceMatrixResult distanceMatrixResult, Directions directions) throws Exception {

               if (!distanceMatrixResult.getStatus().equals("OK")){
                   throw new IllegalStateException("Distance Matrix Result is not OK");
               }

               if (!directions.getStatus().equals("OK")){
                   throw new IllegalStateException("Directions Result is not OK");
               }

               return  new CombinedDirectionAndDuration(directions,distanceMatrixResult);
           }
       };
    }

    private  List<List<HashMap<String, String>>> getRoutez(Directions directions){

        List<List<HashMap<String, String>>> routesMain = new ArrayList<>() ;

        List<Route> routes;
        List<Step> steps;
        List<Leg> legs;

        routes = directions.getRoutes();

        for(int i=0;i<routes.size();i++){
            legs =  routes.get(i).getLegs();
            List path = new ArrayList<HashMap<String, String>>();

            for(int j=0;j<legs.size();j++){

                steps = legs.get(j).getSteps();

                for(int k = 0; k <steps.size();k++){
                    String polyline = "";
                    polyline = steps.get(k).getPolyline().getPoints();
                    List list = PolyLineParser.decodePoly(polyline);

                    for(int l=0;l <list.size();l++){

                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                        hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                        path.add(hm);

                    }
                }

                routesMain.add(path);


            }

        }

        return  routesMain;
    }


    private  void renderRoute(List<List<HashMap<String, String>>> result){
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);

            lineOptions.startCap(new RoundCap());
            lineOptions.endCap(new RoundCap());


            MarkerOptions startMarker = new MarkerOptions();

            startMarker.position(new LatLng(points.get(0).latitude,points.get(0).longitude));
            startMarker.draggable(false);
            startMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            startMarker.title("Your location");


            MarkerOptions endMarker = new MarkerOptions();

            endMarker.position(new LatLng(points.get(points.size() - 1 ).latitude,points.get(points.size() - 1).longitude));
            endMarker.draggable(false);
            endMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            endMarker.title("Ambulance");

            mMap.addMarker(startMarker);
            mMap.addMarker(endMarker);

            lineOptions.width(10);
            lineOptions.jointType(JointType.ROUND);
            lineOptions.color(Color.RED);

        }

        mMap.addPolyline(lineOptions);
    }

    private void buildLoadingDialog(){
        loadingDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.fetching_directions)
                .content(R.string.please_wait)
                .cancelable(false)
                .progress(true, 0)
                .build();
    }

    private void builddriverInfoDialog(){
        driverInfoDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.driver_info)
                .titleColorRes(R.color.primary_dark)
                .customView(R.layout.driver_info_view, true)
                .cancelable(true)
                .positiveText("OK")
                .build();






    }

    private void showdriverInfoDialog(){
        if (!driverInfoDialog.isShowing()){

            driverInfoDialog.show();
        }

    }

    private void setShowDriverInfoDialogItems(CallAmbulanceStatus callAmbulanceStatus){
        TextView driverNameTextView = driverInfoDialog.getCustomView().findViewById(R.id.driverNameTextView);
        TextView userNameTextView = driverInfoDialog.getCustomView().findViewById(R.id.userNameTextView);
        TextView cnicTextView = driverInfoDialog.getCustomView().findViewById(R.id.cnicTextView);
        TextView licenseNumberTextView = driverInfoDialog.getCustomView().findViewById(R.id.licenseNumberTextView);
        TextView contactNumberTextView = driverInfoDialog.getCustomView().findViewById(R.id.contactNumberTextView);
        TextView gpsCoordTextView = driverInfoDialog.getCustomView().findViewById(R.id.gpsCoordTextView);


         driverNameTextView.setText(callAmbulanceStatus.getName());
         userNameTextView.setText(callAmbulanceStatus.getUsername());
         cnicTextView.setText(callAmbulanceStatus.getCnic());
         licenseNumberTextView.setText(callAmbulanceStatus.getLicenseNum());
         contactNumberTextView.setText(callAmbulanceStatus.getContact());
         gpsCoordTextView.setText(callAmbulanceStatus.getLat() + "/" + callAmbulanceStatus.getLng());

    }

    private void  hidedriverInfoDialog(){
        if (driverInfoDialog.isShowing()){
            driverInfoDialog.dismiss();
        }
    }





    private void showLoadingDialog(){
        if (!loadingDialog.isShowing()){
            loadingDialog.show();
        }

    }

    private void  hideLoadingDialog(){
        if (loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }


    private void buildErrorDialog(){
        errorDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.error_fetch_googleapi)
                .content(R.string.error_from_server)
                .negativeText("OK")
                .build();


    }

    private void showErrorDialog(String title, String content){
        if (!errorDialog.isShowing()) {
            errorDialog.setTitle(title);
            errorDialog.setContent(content);
            errorDialog.show();
        }
    }

    private void  hideErrorDialog(){
        if (errorDialog.isShowing()){
            errorDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
