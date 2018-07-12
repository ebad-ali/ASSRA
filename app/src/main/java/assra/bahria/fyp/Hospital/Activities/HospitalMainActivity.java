package assra.bahria.fyp.Hospital.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import assra.bahria.fyp.Common.Activities.MainActivity;
import assra.bahria.fyp.Hospital.Fragments.HospitalMapFragment;
import assra.bahria.fyp.Hospital.Fragments.HospitalUpdateBedsFragment;
import assra.bahria.fyp.Hospital.HospitalAppStateManager;
import assra.bahria.fyp.Patient.Activities.PatientMainActivity;
import assra.bahria.fyp.R;

public class HospitalMainActivity extends AppCompatActivity {

    private Drawer navDrawer = null;
    private Toolbar toolbar = null;
    private Boolean exit = false;
    private String lastShownFragmentTag = null;


    private static final String TAG = "HospitalMainActivity";

    private MaterialDialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hospital_main_activity);

        setUpToolBar();
        setUpLogoutDialog();
        setUpNavDrawer();

        DisplayFragment("HospitalMapFragment", false, false);
    }


    private void setUpToolBar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Hospital");
        setSupportActionBar(toolbar);
    }


    private void setUpNavDrawer() {


        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withSelectionListEnabledForSingleProfile(false)
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .withAlternativeProfileHeaderSwitching(false)
                .build();



        PrimaryDrawerItem hospitalMapDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Hospital Map").withIcon(GoogleMaterial.Icon.gmd_local_hospital).withIdentifier(0);
        PrimaryDrawerItem updateHospitalBedsDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Update Beds").withIcon(GoogleMaterial.Icon.gmd_edit).withIdentifier(1);
        PrimaryDrawerItem logoutDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName("Logout").withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withIdentifier(2);



        navDrawer = new DrawerBuilder().withActivity(this)
                .addDrawerItems(
                        hospitalMapDrawerItem,
                        updateHospitalBedsDrawerItem,
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
                                DisplayFragment("HospitalMapFragment", false, true);
                                break;


                            case 1:
                                Log.d(TAG, "onItemClick: " + "Log out");
                                DisplayFragment("HospitalUpdateBedsFragment", false, true);
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


    private void DisplayFragment(String fragmentTag, boolean shouldAddToBackStack, boolean isAnimated) {
        Fragment  fragment = null;

        switch (fragmentTag) {




            case "HospitalMapFragment":
                fragment =  new HospitalMapFragment();

                break;

            case  "HospitalUpdateBedsFragment":
                fragment =  new HospitalUpdateBedsFragment();
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

    private boolean isFragmentCurrentlyVisible(String fragmentTag) {
        Fragment fragmentToTest;
        fragmentToTest = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragmentToTest != null && fragmentToTest.isVisible()) {
            return true;
        } else {
            return false;
        }

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

                        HospitalAppStateManager.getInstance().setLoggedIn(false);
                        HospitalAppStateManager.getInstance().setUserID(-1);
                        Intent i = new Intent(HospitalMainActivity.this, MainActivity.class);
                        startActivity(i);
                        Toast.makeText(HospitalMainActivity.this, "You are now logged out.", Toast.LENGTH_SHORT).show();
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

        if (exit){
            HospitalAppStateManager.getInstance().setLoggedIn(false);
            HospitalAppStateManager.getInstance().setUserID(-1);
            finish();
        } else {
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
}
