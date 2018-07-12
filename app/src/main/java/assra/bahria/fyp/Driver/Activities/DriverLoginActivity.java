package assra.bahria.fyp.Driver.Activities;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;

import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.GPSTracker;
import assra.bahria.fyp.Driver.DriverAppStateManager;
import assra.bahria.fyp.Driver.Models.DriverLoginStatus;
import assra.bahria.fyp.Driver.Networking.DriverRoutes;
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

public class DriverLoginActivity extends AppCompatActivity {


    private AppCompatButton loginButton;
    private TextInputLayout userNameTextInputLayout, passwordTextInputLayout;
    private AppCompatEditText userNameInputEditTextView, passwordInputEditTextView;
    private AppCompatTextView accountSignUpActionTextView;

    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;

    private boolean exit = false;

    private GPSTracker gpsTracker;


    Double latitude = 0.0;
    Double longitude = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_login_activity);

        setUpInputs();
        setUpSignUpButton();
        setUpSignUpTextView();
        setUpListeners();
        setUpOkHttpClient();
        buildErrorDialog();
        setGpsTracker();
        buildLoadingDialog();

    }


    private void setUpInputs() {

        userNameInputEditTextView = findViewById(R.id.userNameInputEditTextView);
        passwordInputEditTextView = findViewById(R.id.passwordInputEditTextView);

        userNameTextInputLayout = findViewById(R.id.userNameTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);

    }

    private void setUpSignUpButton() {
        loginButton = findViewById(R.id.loginButton);
    }

    private void setGpsTracker() {
        gpsTracker = new GPSTracker(this);
    }

    private void setUpSignUpTextView() {
        //accountSignUpActionTextView = findViewById(R.id.accountSignUpActionTextView);
    }

    private void setUpListeners() {

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });


//        accountSignUpActionTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                showSignUp();
//
//            }
//        });

    }


    private void getGpsLocation() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        Location location = gpsTracker.getLocation();

        if (location != null) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            Toast.makeText(this, "hmmmm", Toast.LENGTH_SHORT).show();
        }

    }


    private void login() {

        getGpsLocation();

        String tokenID = "";
        tokenID = FirebaseInstanceId.getInstance().getToken();

        // get input.
        String username = userNameInputEditTextView.getText().toString();
        String password = passwordInputEditTextView.getText().toString();


        // if email is invalid do nothing and show error
        if (username.length() == 0) {
            userNameTextInputLayout.setError("Please enter valid username !");
            return;
        }

        // remove email error
        userNameTextInputLayout.setError(null);

        // if password is invalid do nothing and show error
        if (password.length() == 0) {
            passwordTextInputLayout.setError("Please enter valid password !");
            return;
        }

        // remove password error
        passwordTextInputLayout.setError(null);


        if (latitude == 0.0 || longitude == 0.0) {
            showToast("Location error!");
            return;
        }

        if (tokenID.equals("")) {
            showToast("Token error!");
            return;
        }


        // input is validated now fire off request and then wait for response


        try {
            showLoadingDialog();
            loginCall(username, password, tokenID, "" + longitude, "" + latitude);
        }
        catch (Exception x) {
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }


    }


    private void loginCall(String username, String password, String token, String lng, String lat) throws Exception {

        RequestBody formBody = new FormBody.Builder()
                .add("password", password)
                .add("username", username)
                .add("token", token)
                .add("lat", lat)
                .add("lng", lng)
                .build();

        Request request = new Request.Builder()
                .url(DriverRoutes.login)
                .post(formBody)

                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final String message = e.getMessage();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        hideLoadingDialog();
                        showErrorDialog("Exception", message);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
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

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
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

                    String result = responseBody.string();

                    Gson gson = new Gson();
                    final DriverLoginStatus status = gson.fromJson(result, DriverLoginStatus.class);

                    //final int statusInt;

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

                    } else if (status.getStatus() > 0) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(DriverLoginActivity.this, "Patient Login complete", Toast.LENGTH_SHORT).show();
                                DriverAppStateManager.getInstance().setUserID(status.getDriverId());
                                DriverAppStateManager.getInstance().setLoggedIn(true);

                                Intent i = new Intent(DriverLoginActivity.this, DriverMainActivity.class);
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

    private void showSignUp() {
        Intent i = new Intent(this, DriverSignupActivity.class);
        startActivity(i);
    }

    private void setUpOkHttpClient() {


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }


    private void buildLoadingDialog() {
        loadingDialog = new MaterialDialog.Builder(this)
                .title(R.string.logging_in)
                .content(R.string.please_wait)
                .cancelable(false)
                .progress(true, 0)
                .build();
    }


    private void showLoadingDialog() {
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }

    }

    private void hideLoadingDialog() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    private void buildErrorDialog() {
        errorDialog = new MaterialDialog.Builder(this)
                .title(R.string.signup_error)
                .content(R.string.signup_error)
                .negativeText("OK")
                .build();


    }

    private void showErrorDialog(String title, String content) {
        if (!errorDialog.isShowing()) {
            errorDialog.setTitle(title);
            errorDialog.setContent(content);
            errorDialog.show();
        }
    }

    private void hideErrorDialog() {
        if (errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
    }


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
