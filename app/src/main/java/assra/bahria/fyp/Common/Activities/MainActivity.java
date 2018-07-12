package assra.bahria.fyp.Common.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.concurrent.ExecutionException;

import assra.bahria.fyp.Admin.Activities.AdminLoginActivity;
import assra.bahria.fyp.Common.Models.GmapsDistanceMatrixModels.DistanceMatrixResult;
import assra.bahria.fyp.Common.Networking.CommonRoutes;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.RxOkhttpWrapper;
import assra.bahria.fyp.Driver.Activities.DriverLoginActivity;
import assra.bahria.fyp.Driver.Networking.DriverRoutes;
import assra.bahria.fyp.Hospital.Activities.HospitalLoginActivity;
import assra.bahria.fyp.Patient.Activities.PatientLoginActivity;
import assra.bahria.fyp.Patient.Activities.PatientSignupActivity;
import assra.bahria.fyp.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.reactivex.Observable.create;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private AppCompatButton patientStartButton;
    private AppCompatButton driverStartButton;
    private AppCompatButton adminStartButton;
    private AppCompatButton hospitalStartButton;
    private boolean exit = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean isRequiredPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;


    private CompositeDisposable compositeDisposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity_main);



        setUpButtons();
        setUpButtonListeners();
        if(isServicesOK()){
            getLocationPermission();
        }
    }




    private void setUpButtons() {
        patientStartButton = findViewById(R.id.patientStartButton);
        driverStartButton = findViewById(R.id.driverStartButton);
        adminStartButton = findViewById(R.id.adminStartButton);
        hospitalStartButton =  findViewById(R.id.hospitalStartButton);
    }

    private void setUpButtonListeners() {
        patientStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPatientFlow();

            }
        });



        driverStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDriverFlow();
            }
        });


        adminStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdminFlow();
            }
        });

        hospitalStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHospitalFlow();
            }
        });
    }


    private void startPatientFlow() {

        if (isRequiredPermissionGranted) {
            Intent i = new Intent(MainActivity.this, PatientLoginActivity.class);
            startActivity(i);

        } else {
            Toast.makeText(this, "ASSRA Needs location permission to work correctly please allow the permission.", Toast.LENGTH_SHORT).show();
        }

    }

    private void startDriverFlow() {
        if (isRequiredPermissionGranted) {
            Intent i = new Intent(MainActivity.this, DriverLoginActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "ASSRA Needs location permission to work correctly please allow the permission.", Toast.LENGTH_SHORT).show();
        }


    }

    private void startAdminFlow() {
        if (isRequiredPermissionGranted) {
            Intent i = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "ASSRA Needs location permission to work correctly please allow the permission.", Toast.LENGTH_SHORT).show();
        }


    }

    private void startHospitalFlow() {
        if (isRequiredPermissionGranted) {
            Intent i = new Intent(MainActivity.this, HospitalLoginActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(this, "ASSRA Needs location permission to work correctly please allow the permission.", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {

        if (exit)
            finish();
        else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                isRequiredPermissionGranted = true;



            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        isRequiredPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isRequiredPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            Toast.makeText(this, "ASSRA Needs location permission to work correctly please allow the permission.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    isRequiredPermissionGranted = true;
                    //initialize our map

                }
            }
        }
    }



    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make use of map requests because of playservice error.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();

    }






    }



