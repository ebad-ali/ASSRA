package assra.bahria.fyp.Patient.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


import assra.bahria.fyp.Common.Activities.MainActivity;
import assra.bahria.fyp.Common.Utils.OnFragmentInteractionListener;
import assra.bahria.fyp.Patient.Fragments.CallAmbulanceFragment;
import assra.bahria.fyp.Patient.Fragments.FirstAidVideosFragment;
import assra.bahria.fyp.R;

public class PatientMainActivity extends AppCompatActivity implements  OnFragmentInteractionListener<Object> {

    private Drawer navDrawer = null;
    private Toolbar toolbar = null;
    private Boolean exit = false;
    private String lastShownFragmentTag = null;



    private static final String TAG = "PatientMainActivity";

    private MaterialDialog logoutDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_main);


        setUpToolBar();
        setUpNavDrawer();
        setUpLogoutDialog();


//        if (isServicesOK()) {
//            DisplayFragment("MapFragment", false, false);
//            //initMap();
//        }


        DisplayFragment("CallAmbulanceFragment", false, false);
    }



    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Patient");
        setSupportActionBar(toolbar);
    }


    private void setUpNavDrawer() {


        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withSelectionListEnabledForSingleProfile(false)
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .withAlternativeProfileHeaderSwitching(false)
                .build();



        PrimaryDrawerItem callAmbulanceDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Call ambulance").withIcon(GoogleMaterial.Icon.gmd_local_hospital).withIdentifier(0);
        PrimaryDrawerItem firsAidDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Watch First Aid Videos").withIcon(GoogleMaterial.Icon.gmd_video_call).withIdentifier(1);
        PrimaryDrawerItem logoutDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Logout").withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withIdentifier(2);



        navDrawer = new DrawerBuilder().withActivity(this)
                .addDrawerItems(
                        callAmbulanceDrawerItem,
                        firsAidDrawerItem,
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
                                Log.d(TAG, "onItemClick: " + "CallAmbulance");
                                DisplayFragment("CallAmbulanceFragment", false, true);
                                break;


                            case 1:
                                Log.d(TAG, "onItemClick: " + "FirstAidVideo");
                                DisplayFragment("FirstAidVideoFragment", false, true);
                                break;

                            case 2:
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


    private void setUpLogoutDialog() {

        logoutDialog = new MaterialDialog.Builder(this)
                .title(R.string.logut)
                .content(R.string.logout_content)
                .negativeText("Cancel")
                .positiveText("Logout")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent i = new Intent(PatientMainActivity.this, MainActivity.class);
                        startActivity(i);
                        Toast.makeText(PatientMainActivity.this, "You are now logged out.", Toast.LENGTH_SHORT).show();
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


    private void DisplayFragment(String fragmentTag, boolean shouldAddToBackStack, boolean isAnimated) {
        Fragment  fragment = null;

        switch (fragmentTag) {




            case "MapFragment":
                fragment =  SupportMapFragment.newInstance();

                break;
            case  "FirstAidVideoFragment":
                fragment = new FirstAidVideosFragment();
                break;

            case "CallAmbulanceFragment":
                fragment = new CallAmbulanceFragment();
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


        if(fragmentTag == "MapFragment") {
            fragmentTransaction.commitNow();
            //getLocationPermission();
        } else {
            fragmentTransaction.commit();
        }







    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onFragmentInteraction(String tag, Object data) {
        if (tag.equals("OpenYoutube") && data instanceof String ){

            String youtubeVideoUrl = data.toString();

            Intent i = new Intent(this,YoutubePlayerActivity.class);
            i.putExtra("youtubeVideoUrl", youtubeVideoUrl);
            startActivity(i);
        }
    }



}
