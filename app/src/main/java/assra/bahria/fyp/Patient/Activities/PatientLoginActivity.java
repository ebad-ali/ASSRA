package assra.bahria.fyp.Patient.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;

import assra.bahria.fyp.Common.Activities.MainActivity;
import assra.bahria.fyp.Common.Models.Status;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.GPSTracker;
import assra.bahria.fyp.Common.Utils.UtilityEmailRegex;
import assra.bahria.fyp.Patient.Models.PatientLoginStatus;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
import assra.bahria.fyp.Patient.PatientAppStateManager;
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

public class PatientLoginActivity extends AppCompatActivity {


    private static final String TAG = "PatientLoginActivity";
    private AppCompatButton loginButton;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private AppCompatEditText emailInputEditTextView, passwordInputEditTextView;
    private AppCompatTextView accountSignUpActionTextView;


    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;
    private GPSTracker gpsTracker;
    Location location;



    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_login_activity);


        setUpGpsTracker();
        setUpInputs();
        setUpLoginButton();
        setUpSignUpTextView();
        setUpListeners();
        setUpOkHttpClient();
        buildErrorDialog();
        buildLoadingDialog();
    }





    private void setUpInputs(){

        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        emailInputEditTextView = findViewById(R.id.emailInputEditTextView);
        passwordInputEditTextView = findViewById(R.id.passwordInputEditTextView);
    }

    private void setUpLoginButton() {
        loginButton = findViewById(R.id.loginButton);
    }

    private void setUpSignUpTextView(){
        accountSignUpActionTextView = findViewById(R.id.accountSignUpActionTextView);
    }

    private void setUpListeners(){

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });


        accountSignUpActionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showSignUp();

            }
        });

    }



    private void setUpGpsTracker(){
        gpsTracker = new GPSTracker(this);
    }




    private void login(){

        // get input.
        String email = emailInputEditTextView.getText().toString();
        String password = passwordInputEditTextView.getText().toString();

        String tokenID = FirebaseInstanceId.getInstance().getToken();

        location = gpsTracker.getLocation();




        if (location == null){
            Toast.makeText(this, "Cannot Login without location data.", Toast.LENGTH_SHORT).show();

            return;
        }


        // will not throw because of return above checking for null

        String  lat = String.valueOf(location.getLatitude());
        String  lng = String.valueOf(location.getLongitude());

        if (tokenID == null){
            Toast.makeText(this, "Cannot Login without token data please try again.", Toast.LENGTH_SHORT).show();
            return;
        }


        // validate it
        boolean res = UtilityEmailRegex.validateEmail(email);

        // if email is invalid do nothing and show error
        if (email.length() == 0 || !res) {
            emailTextInputLayout.setError("Please enter valid email !");
            return;
        }

        // remove email error
        emailTextInputLayout.setError(null);

        // if password is invalid do nothing and show error
        if (password.length() == 0){
            passwordTextInputLayout.setError("Please enter valid password length must be greater than 5 !");
            return;
        }

        // remove password error
        passwordTextInputLayout.setError(null);







        // input is validated now fire off request and then wait for response


        try {
            showLoadingDialog();
            loginCall(email,password, tokenID,lat,lng);
        }catch (Exception x){
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }
    }


    private void loginCall(String email, String password, String token,String lat,String lng) throws  Exception{

        RequestBody formBody = new FormBody.Builder()
                .add("password",password)
                .add("email",email)
                .add("token",token)
                .add("lat",lat)
                .add("lng",lng)
                .build();

        Request request = new Request.Builder()
                .url(PatientRoutes.login)
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
                    final PatientLoginStatus status = gson.fromJson(result,PatientLoginStatus.class);

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

                    if (status.getStatus() < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Signup Error", "Invalid email or password !");
                            }
                        });

                    } else if  ( status.getStatus() > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(PatientLoginActivity.this, "Patient Login complete", Toast.LENGTH_SHORT).show();
                                //DriverAppStateManager.getInstance().setUserID(statusInt);
                                //openMainActivity();

                                // TODO: Ider patient ki id set horai
                                PatientAppStateManager.getInstance().setUserID(status.getUserId());
                                Intent i = new Intent(PatientLoginActivity.this, PatientMainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();


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



    private void showSignUp(){
        Intent i = new Intent(this, PatientSignupActivity.class);
        startActivity(i);
    }

    private void setUpOkHttpClient(){


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }



    private void buildLoadingDialog(){
        loadingDialog = new MaterialDialog.Builder(this)
                .title(R.string.logging_in)
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
        errorDialog = new MaterialDialog.Builder(this)
                .title(R.string.signup_error)
                .content(R.string.signup_error)
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



}
