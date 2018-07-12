package assra.bahria.fyp.Admin.Activities;

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
import com.google.gson.Gson;

import java.io.IOException;

import assra.bahria.fyp.Admin.Networking.AdminRoutes;
import assra.bahria.fyp.Common.Models.Status;
import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.UtilityEmailRegex;
import assra.bahria.fyp.Patient.Activities.PatientSignupActivity;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
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

public class AdminSignUpActivity extends AppCompatActivity {


    private AppCompatButton signupButton;
    private TextInputLayout userNameTextInputLayout, passwordTextInputLayout , nameTextInputLayout;
    private AppCompatEditText usernameInputEditTextView, passwordInputEditTextView, nameInputEditTextView;

    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;

    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_signup_activity);


        setUpInputs();
        setUpSignUpButton();
        setUpListeners();
        setUpOkHttpClient();
        buildErrorDialog();
        buildLoadingDialog();
    }




    private void setUpInputs(){

        userNameTextInputLayout = findViewById(R.id.userNameTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        nameTextInputLayout = findViewById(R.id.nameTextInputLayout);



        passwordInputEditTextView = findViewById(R.id.passwordInputEditTextView);
        nameInputEditTextView = findViewById(R.id.nameInputEditTextView);
        usernameInputEditTextView = findViewById(R.id.userNameInputEditTextView);
    }

    private void setUpSignUpButton() {
        signupButton = findViewById(R.id.signupButton);
    }

    private void setUpListeners(){

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signUp();
            }
        });
    }

    private void setUpOkHttpClient(){


        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }



    private void signUp(){

        // get input.
        String username = usernameInputEditTextView.getText().toString();
        String password = passwordInputEditTextView.getText().toString();
        String name   = nameInputEditTextView.getText().toString();

        // if name is invalid do nothing and show error
        if (name.length() == 0){
            nameTextInputLayout.setError("Please enter valid name !");
            return;
        }

        // remove password error
        nameTextInputLayout.setError(null);


        if (username.length() == 0){
            userNameTextInputLayout.setError("Please enter valid name !");
            return;
        }


        userNameTextInputLayout.setError(null);

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
            signUpCall(name,username,password);
        }catch (Exception x){
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }





    }



    private void signUpCall(String name,String username, String password) throws  Exception{

        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("password",password)
                .add("username",username)
                .build();

        Request request = new Request.Builder()
                .url(AdminRoutes.signUp)
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

                    String result =  responseBody.string();

                    Gson gson = new Gson();
                    Status status = gson.fromJson(result,Status.class);

                    final int statusInt;

                    try {
                        statusInt = Integer.parseInt(status.getStatus());
                    }catch (Exception x) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                showErrorDialog("Server Error", "Could not parse status int!");
                            }
                        });
                        return;
                    }

                    if (statusInt < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Signup Error", "Invalid email or password !");
                            }
                        });

                    } else if  ( statusInt > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Toast.makeText(AdminSignUpActivity.this, "Admin SignUp complete you can now login.", Toast.LENGTH_LONG).show();
                                //DriverAppStateManager.getInstance().setUserID(statusInt);
                                //openMainActivity();
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
                .title(R.string.signing_up)
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
