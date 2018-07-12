package assra.bahria.fyp.Driver.Activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.security.Permission;

import assra.bahria.fyp.Common.Activities.MainActivity;
import assra.bahria.fyp.Common.Models.Status;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.GPSTracker;
import assra.bahria.fyp.Driver.DriverAppStateManager;
import assra.bahria.fyp.Driver.Fragments.DriverMapFragment;
import assra.bahria.fyp.Driver.Networking.DriverRoutes;
import assra.bahria.fyp.Patient.Activities.PatientMainActivity;
import assra.bahria.fyp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DriverMainActivity extends AppCompatActivity {

    private static final String TAG = "DriverMainActivity";

    private Drawer navDrawer = null;
    private Toolbar toolbar = null;
    private boolean exit = false;
    private String lastShownFragmentTag = null;
    private MaterialDialog logoutDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_main_activity);

        setUpToolBar();
        setUpLogoutDialog();
        setUpNavDrawer();

        DisplayFragment("CurrentEmergencyFragment", false, false);

    }


    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Driver");
        setSupportActionBar(toolbar);
    }

    private void setUpNavDrawer() {


        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withSelectionListEnabledForSingleProfile(false)
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .withAlternativeProfileHeaderSwitching(false)
                .build();



        PrimaryDrawerItem emergencyDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Current Ambulance Emergency Call").withIcon(GoogleMaterial.Icon.gmd_local_hospital).withIdentifier(0);
        PrimaryDrawerItem logoutDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Logout").withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withIdentifier(1);



        navDrawer = new DrawerBuilder().withActivity(this)
                .addDrawerItems(
                        emergencyDrawerItem,
                        new DividerDrawerItem(),
                        logoutDrawerItem
                )
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .withSelectedItem(-1).withDelayOnDrawerClose(-1)
                .withDelayDrawerClickEvent(400)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {


                        int drawerIdentitifier = ((int) drawerItem.getIdentifier()); // hmmmm :]

                        switch (drawerIdentitifier) {


                            case 0:
                                Log.d(TAG, "onItemClick: " + "Current Emergency Ambulance Call");
                                DisplayFragment("CurrentEmergencyFragment", false, true);
                                break;

                            case 1:
                                Log.d(TAG, "onItemClick: " + "Log out");
                                showLogoutDialog();
                                break;



                            default:
                                break;
                        }

                        return false;
                    }
                })
                .build();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        navDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);


    }


    private void DisplayFragment(String fragmentTag, boolean shouldAddToBackStack, boolean isAnimated) {
        Fragment  fragment = null;

        switch (fragmentTag) {




            case "CurrentEmergencyFragment":
                fragment =  new DriverMapFragment();

                break;


            default:
                break;

        }


        if (fragment == null) {
            return;
        }

        if (isFragmentCurrentlyVisible(fragmentTag)) {
            return;
        }


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();


        if (isAnimated) {
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }


        // if there is a visible fragment hide it
        if (lastShownFragmentTag != null) {
            // hide last shown frag
            fragmentTransaction.hide(getSupportFragmentManager().findFragmentByTag(lastShownFragmentTag));
        }

        // show new frag that was clicked if it exists in memory else we add and then show
        if (getSupportFragmentManager().findFragmentByTag(fragmentTag) != null) {
            fragmentTransaction.show(getSupportFragmentManager().findFragmentByTag(fragmentTag));
        } else {
            // add new frag
            fragmentTransaction.add(R.id.fragmentContainer, fragment, fragmentTag);
            fragmentTransaction.show(fragment);


        }

        if (shouldAddToBackStack) {
            fragmentTransaction.addToBackStack(fragmentTag);
        }

        lastShownFragmentTag = fragmentTag;



        fragmentTransaction.commit();







    }
    private void setUpLogoutDialog() {

        logoutDialog = new MaterialDialog.Builder(this)
                .title(R.string.logut)
                .content(R.string.logout_content)
                .negativeText("Cancel")
                .positiveText("Logout")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DriverAppStateManager.getInstance().setLoggedIn(false);
                        DriverAppStateManager.getInstance().setUserID(-1);
                        DriverAppStateManager.getInstance().setStatusID(0);
                        Intent i = new Intent(DriverMainActivity.this, MainActivity.class);
                        startActivity(i);
                        Toast.makeText(DriverMainActivity.this, "You are now logged out.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .build();

    }

    private void showLogoutDialog() {
        if (!logoutDialog.isShowing()) {
            logoutDialog.show();
        }
    }


    private boolean isFragmentCurrentlyVisible(String fragmentTag) {
        Fragment fragmentToTest;
        fragmentToTest = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragmentToTest != null && fragmentToTest.isVisible()) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onBackPressed() {

        if (exit) {
            DriverAppStateManager.getInstance().setLoggedIn(false);
            DriverAppStateManager.getInstance().setUserID(-1);
            DriverAppStateManager.getInstance().setStatusID(0);
            finish();
        }
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

//    private void gpsClientCall(String userID, Location location) throws  Exception{
//
//
//        RequestBody formBody = new FormBody.Builder()
//                .add("user_id",userID)
//                .add("lat", String.valueOf(location.getLatitude()))
//                .add("lng",String.valueOf(location.getLongitude()))
//                .build();
//
//        Request request = new Request.Builder()
//                .url(DriverRoutes.setDriverLocation)
//                .post(formBody)
//
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//
//            @Override public void onFailure(Call call, final IOException e) {
//                e.printStackTrace();
//                final String  message = e.getMessage();
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG,"Request Failed due to : " + e.getMessage());
//                    }
//                });
//            }
//
//            @Override public void onResponse(Call call, final Response response) throws IOException {
//                try (ResponseBody responseBody = response.body()) {
//                    if (!response.isSuccessful()) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Log.d(TAG,"Request Failed with error code  : " + response.code() );
//                            }
//                        });
//
//                        throw new IOException("Unexpected code " + response);
//                    }
//
//
//
//                    if (responseBody == null) {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Log.d(TAG,"Request body is null" );
//                            }
//                        });
//
//                        return;
//                    }
//
//                    String result =  responseBody.string();
//
//                    Gson gson = new Gson();
//                    final Status status = gson.fromJson(result,Status.class);
//
//                    final int statusInt;
//
//                    try {
//                        statusInt = Integer.parseInt(status.getStatus());
//                    }catch (Exception x) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(TAG,"Could not parse result int" );
//                            }
//                        });
//                        return;
//                    }
//
//                    if (statusInt < 1) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                // wrong credentials
//                                Log.d(TAG,"Request failed on server side" );
//                            }
//                        });
//
//                    } else if  ( statusInt > 0){
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Log.d(TAG,"Request succeeded" );
//
//
//                            }
//                        });
//
//
//                    } else {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Log.d(TAG,"Request error unknown" );
//                            }
//                        });
//                    }
//
//
//                }
//            }
//        });
//
//    }
//

}
