package assra.bahria.fyp.Hospital.Activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputLayout;
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
import assra.bahria.fyp.Common.Utils.UtilityEmailRegex;
import assra.bahria.fyp.Hospital.HospitalAppStateManager;
import assra.bahria.fyp.Hospital.Models.HospitalLoginStatus;
import assra.bahria.fyp.Hospital.Networking.HospitalRoutes;
import assra.bahria.fyp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HospitalLoginActivity extends AppCompatActivity {


    private AppCompatButton loginButton;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private AppCompatEditText emailInputEditTextView, passwordInputEditTextView;
    private AppCompatTextView accountSignUpActionTextView;

    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;

    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hospital_login_activity);
        setUpInputs();
        setUpLoginButton();
        setUpListeners();
        setUpOkHttpClient();
        buildErrorDialog();
        buildLoadingDialog();
    }


    private void setUpInputs() {

        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        emailInputEditTextView = findViewById(R.id.emailInputEditTextView);
        passwordInputEditTextView = findViewById(R.id.passwordInputEditTextView);
    }

    private void setUpLoginButton() {
        loginButton = findViewById(R.id.loginButton);
    }

    private void setUpListeners() {

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });

    }

    private void setUpOkHttpClient(){


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }


    private void login(){

        // get input.
        String email = emailInputEditTextView.getText().toString();
        String password = passwordInputEditTextView.getText().toString();



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
            passwordTextInputLayout.setError("Please enter valid password !");
            return;
        }

        // remove password error
        passwordTextInputLayout.setError(null);







        // input is validated now fire off request and then wait for response


        try {
            showLoadingDialog();
            loginCall(email,password, FirebaseInstanceId.getInstance().getToken());
        }catch (Exception x){
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }
    }


    private void loginCall(String email, String password,String token) throws  Exception{

        RequestBody formBody = new FormBody.Builder()
                .add("password",password)
                .add("email",email)
                .add("token",token)
                .build();

        Request request = new Request.Builder()
                .url(HospitalRoutes.login)
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
                        showErrorDialog("Server Exception", message);
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
                    final HospitalLoginStatus hospitalLoginStatus = gson.fromJson(result,HospitalLoginStatus.class);

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

                    if (hospitalLoginStatus.getStatus() < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Login  Error", "Invalid email or password !");
                            }
                        });

                    } else if  ( hospitalLoginStatus.getStatus() > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(HospitalLoginActivity.this, "Hospital Login complete", Toast.LENGTH_SHORT).show();
                                //DriverAppStateManager.getInstance().setUserID(statusInt);
                                //openMainActivity();

                                // TODO: Ider patient ki id set horai
                                HospitalAppStateManager.getInstance().setUserID(hospitalLoginStatus.getId());
                                HospitalAppStateManager.getInstance().setLoggedIn(true);
                                Intent i = new Intent(HospitalLoginActivity.this, HospitalMainActivity.class);
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
                .title(R.string.login_error)
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
