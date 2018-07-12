package assra.bahria.fyp.Driver.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import assra.bahria.fyp.Common.Constants;
import assra.bahria.fyp.Common.Models.CombinedDirectionAndDuration;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.DistanceMatrixResult;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.Element;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.Row;
import assra.bahria.fyp.Common.Networking.CommonRoutes;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.PolyLineParser;
import assra.bahria.fyp.Common.Utils.RxOkhttpWrapper;
import assra.bahria.fyp.Driver.DriverAppStateManager;
import assra.bahria.fyp.Driver.Models.Hospital;
import assra.bahria.fyp.Driver.Models.HospitalNotifStatus;
import assra.bahria.fyp.Driver.Models.UnbookDriverStatus;
import assra.bahria.fyp.Driver.Networking.DriverRoutes;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Directions;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Leg;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Route;
import assra.bahria.fyp.Common.Models.GmapsDirectionModels.Step;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
import assra.bahria.fyp.R;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class DriverMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DriverMapFragment";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private BroadcastReceiver fireBaseMessageBroadcastReciever;
    private AppCompatButton driverArrivedButton,driverUnbookButton;

    Location origin;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;
    private OkHttpClient client;
    private Boolean isAlreadyLoaded = false;
    private MaterialDialog hospitalSelectionDialog;

    private TextView etaTextView;

    private List<Hospital> hospitalArrayList = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.driver_map_fragment, container, false);


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupButtons();
        setupButtonListeners();
        setUpBroadcastReciever();
        setUpOkHttpClient();
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


    private void setupButtons(){
        driverArrivedButton = getView().findViewById(R.id.driverArrivedButton);
        driverUnbookButton = getView().findViewById(R.id.driverUnbookButton);
        etaTextView = getView().findViewById(R.id.etaTextView);
        etaTextView.setVisibility(View.GONE);
    }


    private void getDirectionsAndDuration(HttpUrl directionsUrl, HttpUrl distanceMatrixUrl, final String startMarker, final String endMarker) {



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
                renderRoute(routes,startMarker,endMarker);

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

    private void setupButtonListeners(){


        // Clicked when driver arrives at patient location
        driverArrivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    showLoadingDialog("Loading Hospitals","Loading nearest hospitals");
                    getHospitalsCall(String.valueOf(DriverAppStateManager.getInstance().getUserID()), String.valueOf(origin.getLatitude()), String.valueOf(origin.getLatitude()));
                } catch (Exception x){
                    hideLoadingDialog();
                    showErrorDialog("Exception", x.getMessage());
                }
            }
        });


        driverUnbookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              String statusID =  String.valueOf(DriverAppStateManager.getInstance().getStatusID());
              String driverID = String.valueOf(DriverAppStateManager.getInstance().getUserID());
              UnbookDriverCall(driverID, statusID);


            }
        });
    }

    private void showHospitalSelectionDialog(String[] items){
        hospitalSelectionDialog = null;
        hospitalSelectionDialog =  new MaterialDialog.Builder(getActivity())
                .title("Select a Hospital")
                .items(items)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        Hospital hospital = hospitalArrayList.get(which);
                        Toast.makeText(getContext(), hospital.getName() , Toast.LENGTH_SHORT).show();

                        try {
                            showLoadingDialog("Sending Notification","sending notification to hospital");
                            notifyHospitalNetworkCall(String.valueOf(DriverAppStateManager.getInstance().getUserID()),hospital.getHId(),String.valueOf(origin.getLatitude()),String.valueOf(origin.getLongitude()));
                        }
                        catch (Exception x){
                            hideLoadingDialog();
                            showErrorDialog("Exception", x.getMessage());
                        }

                        return true;
                    }
                })
                .positiveText("Select one")
                .show();




    }


    private void UnbookDriverCall(String driverId,String statusID){

        RequestBody formBody = new FormBody.Builder()
                .add("d_id",driverId)
                .add("status_id",statusID)
                .build();


        // RXjava
        RxOkhttpWrapper.getResponse(DriverRoutes.unbookDriver,formBody).map(getResponseUnbookDriverStatusMap()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribeOn(Schedulers.io()).
                subscribe(new Observer<UnbookDriverStatus>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoadingDialog("Unbooking","Unbooking driver please wait ...");
                    }

                    @Override
                    public void onNext(UnbookDriverStatus unbookDriverStatus) {

                        hideLoadingDialog();
                        if (unbookDriverStatus.getStatus() > 0)
                        {
                            Toast.makeText(getContext(), "Unbooked Driver", Toast.LENGTH_LONG).show();
                        } else {
                            showErrorDialog("Server Error", "Could not Unbook driver");

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadingDialog();
                        showErrorDialog("Server Error",e.toString());
                    }

                    @Override
                    public void onComplete() {
                        hideLoadingDialog();
                    }
                });

    }


    private Function<Response, UnbookDriverStatus> getResponseUnbookDriverStatusMap(){
        return new Function<Response, UnbookDriverStatus>() {
            @Override
            public UnbookDriverStatus apply(Response response) throws Exception {

                UnbookDriverStatus unbookDriverStatus = new Gson().fromJson(response.body().string(),UnbookDriverStatus.class);
                return  unbookDriverStatus;

            }
        };



    }

    private void hideHospitalSelectionDialog(){

        if (hospitalSelectionDialog.isShowing()){
            hospitalSelectionDialog.dismiss();
        }
    }

    private void notifyHospitalNetworkCall(String driverID,String hospitalID,String lat , String lng){
        RequestBody formBody = new FormBody.Builder()
                .add("d_id",driverID)
                .add("lat",lat)
                .add("lng",lng)
                .add("h_id",hospitalID)
                .build();

        final Request request = new Request.Builder()
                .post(formBody)
                .url(DriverRoutes.hospitalNotif)
                .build();



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

                    final HospitalNotifStatus status = gson.fromJson(result,HospitalNotifStatus.class);


                    if (status.getStatus() < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Notification Send Error", "Server could not send notification");
                            }
                        });

                    } else if  (status.getStatus() > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Notification sent to hospital", Toast.LENGTH_SHORT).show();

                                String originString = origin.getLatitude() + "," + origin.getLongitude();
                                String destinationString = status.getHospLat() + "," + status.getHospLng();


                                // For Location route
                                HttpUrl directionsURL = HttpUrl.parse(PatientRoutes.mapDirections).newBuilder()
                                        .addQueryParameter("origin",originString)
                                        .addQueryParameter("destination",destinationString)
                                        .addQueryParameter("mode","driving")
                                        .addQueryParameter("key",getResources().getString(R.string.google_maps_directions_api_key)).build();

                                // For eta time
                                HttpUrl distanceMatrixURL = HttpUrl.parse(CommonRoutes.distanceMatrixRoute).newBuilder()
                                        .addQueryParameter("origins",originString)
                                        .addQueryParameter("destinations",destinationString)
                                        .addQueryParameter("key",getResources().getString(R.string.google_maps_distance_matrix_api_key)).build();

                                try {
                                    getDirectionsAndDuration(directionsURL,distanceMatrixURL,"Your location","Hospital Location");
                                }catch (Exception x){
                                    hideLoadingDialog();
                                    showErrorDialog("Exception", x.getMessage());
                                }


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



    private void setUpBroadcastReciever(){
        fireBaseMessageBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent == null){

                    Toast.makeText(context, "intent is null", Toast.LENGTH_SHORT).show();
                    return;

                }


                    String name = intent.getStringExtra("name");
                    String lat = intent.getStringExtra("lat");
                    String lng = intent.getStringExtra("lng");
                    String statusID = intent.getStringExtra("statusID");
                    if (name != null && lat != null && lng != null && statusID != null){


                        Toast.makeText(getContext(), "Recieved data from service via localbroadcast manager !", Toast.LENGTH_SHORT).show();



                        String originString = origin.getLatitude() + "," + origin.getLongitude();
                        String destinationString = lat + "," + lng;


                        // For Location route
                        HttpUrl directionsURL = HttpUrl.parse(PatientRoutes.mapDirections).newBuilder()
                                .addQueryParameter("origin",originString)
                                .addQueryParameter("destination",destinationString)
                                .addQueryParameter("mode","driving")
                                .addQueryParameter("key",getResources().getString(R.string.google_maps_directions_api_key)).build();

                        // For eta time
                        HttpUrl distanceMatrixURL = HttpUrl.parse(CommonRoutes.distanceMatrixRoute).newBuilder()
                                .addQueryParameter("origins",originString)
                                .addQueryParameter("destinations",destinationString)
                                .addQueryParameter("key",getResources().getString(R.string.google_maps_distance_matrix_api_key)).build();

                        try {
                            DriverAppStateManager.getInstance().setStatusID(Integer.parseInt(statusID));
                            getDirectionsAndDuration(directionsURL,distanceMatrixURL,"Your location","Patient Location");
                        }catch (Exception x){
                            hideLoadingDialog();
                            showErrorDialog("Exception", x.getMessage());
                        }
                    }





            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(fireBaseMessageBroadcastReciever);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(fireBaseMessageBroadcastReciever,
                new IntentFilter(Constants.fireBaseBroadcasteEvent));
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
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


    private void clearMarkersAndRoute(){

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

    private void setUpOkHttpClient(){


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }



    // Used to show path in map with start and end markers
    private  void renderRoute(List<List<HashMap<String, String>>> result, String startMarkerTitle, String endMarkerTitle){
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
            startMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            startMarker.title(startMarkerTitle);


            MarkerOptions endMarker = new MarkerOptions();

            endMarker.position(new LatLng(points.get(points.size() - 1 ).latitude,points.get(points.size() - 1).longitude));
            endMarker.draggable(false);
            endMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            endMarker.title(endMarkerTitle);

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


    private void showLoadingDialog(){
        if (!loadingDialog.isShowing()){
            loadingDialog.show();
        }

    }

    private void showLoadingDialog(String title, String content){
        if (!loadingDialog.isShowing()){
            loadingDialog.setContent(content);
            loadingDialog.setTitle(title);
            loadingDialog.show();
        }

    }

    private void  hideLoadingDialog(){
        if (loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    private  void getHospitalsCall(String driverID, String lat, String lng){


        RequestBody formBody = new FormBody.Builder()
                .add("d_id",driverID)
                .add("lat",lat)
                .add("lng",lng)
                .build();

        final Request request = new Request.Builder()
                .post(formBody)
                .url(DriverRoutes.shortListHospitals)
                .build();



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

                    final Hospital[] hospitals = gson.fromJson(result,Hospital[].class);
                    hospitalArrayList = null;
                    hospitalArrayList = Arrays.asList(hospitals);

                    if (hospitals.length == 0) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Directions Error", "Could not obtain gps directions from Google API");
                            }
                        });

                    } else if  (hospitals.length > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {


                                // convert to a strng array for fucks sake
                                String[] strings = new String[hospitals.length];
                                for(int j =0;j<hospitals.length;j++){
                                    strings[j] = hospitals[j].getName();
                                }

                                hideLoadingDialog();
                                showHospitalSelectionDialog(strings);
                                Toast.makeText(getContext(), "Obtained Hospitals", Toast.LENGTH_LONG).show();

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




}

