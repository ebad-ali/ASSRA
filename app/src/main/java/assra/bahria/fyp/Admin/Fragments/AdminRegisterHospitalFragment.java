package assra.bahria.fyp.Admin.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;

import assra.bahria.fyp.Admin.Models.AdminLoginStatus;
import assra.bahria.fyp.Admin.Models.RegisterHospitalStatus;
import assra.bahria.fyp.Admin.Networking.AdminRoutes;
import assra.bahria.fyp.Common.Models.Status;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.app.Activity.RESULT_OK;


public class AdminRegisterHospitalFragment extends Fragment {

    private TextView hospitalLocationTextView;
    private AppCompatButton registerHospitalButton;
    private AppCompatButton setHosiptalLocationButton;
    private TextInputLayout hospitalNameTextInputLayout,hospitalEmailTextInputLayout,hospitalPasswordTextInputLayout,hospitalBedsCountTextInputLayout;
    private EditText hospitalBedsCountInputEditTextView,hospitalPasswordInputEditTextView,hospitalEmailInputEditTextView,hospitalNameInputEditTextView;
    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;
    private static final String TAG = "AdminRegisterHospitalFr";
    private int PLACE_PICKER_REQUEST = 1;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private Boolean isLocationSelected = false;
    private LatLng latLng;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_register_hospital_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpSelectHospitalButton();
        setUpRegisterHospitalButton();
        setUpListeners();
        setUpInputs();
        setUpOkHttpClient();
        buildErrorDialog();
        buildLoadingDialog();
    }

    private void setUpRegisterHospitalButton(){
        registerHospitalButton = getView().findViewById(R.id.registerHospitalButton);
    }

    private void  setUpSelectHospitalButton(){
        setHosiptalLocationButton = getView().findViewById(R.id.setHosiptalLocationButton);
    }



    private void setUpInputs(){
        hospitalBedsCountTextInputLayout = getView().findViewById(R.id.hospitalBedsCountTextInputLayout);
        hospitalEmailTextInputLayout = getView().findViewById(R.id.hospitalEmailTextInputLayout);
        hospitalNameTextInputLayout =  getView().findViewById(R.id.hospitalNameTextInputLayout);
        hospitalPasswordTextInputLayout =  getView().findViewById(R.id.hospitalPasswordTextInputLayout);

        hospitalBedsCountInputEditTextView = getView().findViewById(R.id.hospitalBedsCountInputEditTextView);
        hospitalEmailInputEditTextView = getView().findViewById(R.id.hospitalEmailInputEditTextView);
        hospitalNameInputEditTextView =  getView().findViewById(R.id.hospitalNameInputEditTextView);
        hospitalPasswordInputEditTextView =  getView().findViewById(R.id.hospitalPasswordInputEditTextView);

        hospitalLocationTextView =  getView().findViewById(R.id.hospitalLocationTextView);

    }

    private void setUpListeners(){
        registerHospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              registerHospital();
            }
        });

        setHosiptalLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlacePickerActivity();
            }
        });
    }

    private void openPlacePickerActivity(){

        try {
            if (isServicesOK()) {


                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();


                startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            } else {
                Toast.makeText(getContext(), "Please install Google Play Services to continue", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception x){
            Toast.makeText(getContext(), x.getMessage(), Toast.LENGTH_SHORT).show();
        }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                isLocationSelected = true;
                Place place = PlacePicker.getPlace( getContext(),data);
                String toastMsg = String.format("Place: %s", place.getName());
                latLng = place.getLatLng();
                hospitalLocationTextView.setText(place.getName());
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        }


    }


    private void setUpOkHttpClient(){

        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }

    private void registerHospital(){

        // get input.
        String hospitalName = hospitalNameInputEditTextView.getText().toString();
        String password = hospitalPasswordInputEditTextView.getText().toString();
        String email = hospitalEmailInputEditTextView.getText().toString();
        String bedCount = hospitalBedsCountInputEditTextView.getText().toString();






        if (hospitalName.length() == 0){
            hospitalNameTextInputLayout.setError("Please enter valid Hospital Name !");
            return;
        }


        hospitalNameTextInputLayout.setError(null);


        if (email.length() == 0){
            hospitalEmailTextInputLayout.setError("Please enter valid Email !");
            return;
        }


        hospitalEmailTextInputLayout.setError(null);



        // if password is invalid do nothing and show error
        if (password.length() == 0){
            hospitalPasswordTextInputLayout.setError("Please enter valid password !");
            return;
        }

        // remove password error
        hospitalPasswordTextInputLayout.setError(null);


        if (bedCount.length() == 0){
            hospitalBedsCountTextInputLayout.setError("Please enter valid bed count greater than 0 !");
            return;
        }


        hospitalBedsCountTextInputLayout.setError(null);

        if (!isLocationSelected){
            Toast.makeText(getContext(), "Please select location of hospital before registering.", Toast.LENGTH_LONG).show();
            return;
        }




        // input is validated now fire off request and then wait for response


        String lat = String .valueOf(latLng.latitude);
        String lng = String .valueOf(latLng.longitude);

        try {
            showLoadingDialog();
            registerCall(hospitalName,email,password,bedCount,lat,lng);
        }catch (Exception x){
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }

    }



    private void registerCall(String hospitalName, String email ,String password, String bedsCount, String lat , String lng) throws  Exception{

        RequestBody formBody = new FormBody.Builder()
                .add("password",password)
                .add("email",email)
                .add("name",hospitalName)
                .add("numBeds",bedsCount)
                .add("lat",lat)
                .add("lng",lng)
                .build();

        Request request = new Request.Builder()
                .url(AdminRoutes.registerHospital)
                .post(formBody)

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



                    if (responseBody == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                showErrorDialog("Server Error", "An unrecoverable error occured");
                            }
                        });

                        return;
                    }

                    String result =  responseBody.string();

                    Gson gson = new Gson();
                    final RegisterHospitalStatus registerHospitalStatus = gson.fromJson(result,RegisterHospitalStatus.class);



                    if (registerHospitalStatus.getStatus() < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Signup Error", "Invalid email or password !");
                            }
                        });

                    } else if  ( registerHospitalStatus.getStatus() > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                clearInputs();
                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Hospital Registration complete", Toast.LENGTH_SHORT).show();



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


    private void clearInputs(){
        isLocationSelected = false;
        hospitalLocationTextView.setText("Address is empty set hospital location.");
        hospitalNameInputEditTextView.setText(null);
        hospitalEmailInputEditTextView.setText(null);
        hospitalPasswordInputEditTextView.setText(null);
        hospitalBedsCountInputEditTextView.setText(null);
    }


    private void buildLoadingDialog(){
        loadingDialog = new MaterialDialog.Builder(getContext())
                .title("Registering Hospital")
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

    private void  hideLoadingDialog(){
        if (loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }


    private void buildErrorDialog(){
        errorDialog = new MaterialDialog.Builder(getContext())
                .title(R.string.registration_error)
                .content(R.string.hosp_reg_error_reason)
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
