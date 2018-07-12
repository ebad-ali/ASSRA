package assra.bahria.fyp.Patient.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Patient.Models.CallAmbulanceStatus;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
import assra.bahria.fyp.Patient.PatientAppStateManager;
import assra.bahria.fyp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AmbulanceTimerActivity extends AppCompatActivity {

    Toolbar timerToolbar;
    AppCompatTextView timerActionTextView;
    AppCompatButton cancelAmbulanceButton;

    private CountDownTimer timer;
    private OkHttpClient client;

    Double lat = 0.0, lng = 0.0;

    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_timer);

        setUpToolbar();
        getLatLng();
        setUpOkHttpClient();
        setUpUI();
        setUpTimer();

        buildErrorDialog();
        buildLoadingDialog();
    }

    private void setUpToolbar() {
        timerToolbar = findViewById(R.id.timerToolbar);
        setSupportActionBar(timerToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Cancel Ambulance");
    }

    private void getLatLng() {

        Bundle getTokenBundle = getIntent().getExtras();
        lat = getTokenBundle.getDouble("lat");
        lng = getTokenBundle.getDouble("lng");
    }


    private void setUpUI() {

        timerActionTextView = findViewById(R.id.timerActionTextView);
        cancelAmbulanceButton = findViewById(R.id.cancelAmbulanceButton);

        cancelAmbulanceButton.setOnClickListener(cancelAmbulanceButtonListener);
    }

    private void setUpOkHttpClient() {

        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }

    private void setUpTimer() {

        timer =  new CountDownTimer(10000, 1000) {

            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            public void onTick(long millisUntilFinished) {
                timerActionTextView.setText("" + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {

                timerActionTextView.setText("00:00");
                showLoadingDialog();
                callAmbulance();
            }
        }.start();

    }

    private void callAmbulance() {


        RequestBody formBody = new FormBody.Builder()
                .add("user_id", "" + PatientAppStateManager.getInstance().getUserID())
                .add("lat", "" + lat)
                .add("lng", "" + lng)
                .build();

        Request request = new Request.Builder()
                .url(PatientRoutes.callAmbulance)
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

                    final CallAmbulanceStatus status = gson.fromJson(result, CallAmbulanceStatus.class);

//                    if (status.getStatus() < 1) {
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                // wrong credentials
//                                hideLoadingDialog();
//                                showErrorDialog("Error", "An error has been occured!");
//                            }
//                        });
//
//                    } else if (status.getStatus() > 0) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                hideLoadingDialog();
                                Intent returnIntent = new Intent();
                                Gson gson = new Gson();
                                String objString = gson.toJson(status);
                                returnIntent.putExtra("CallAmbulanceStatus",objString);
                                closeActivity(returnIntent,Activity.RESULT_OK);

                            }
                        });

//                    }
//
//                    else {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                hideLoadingDialog();
//                                showErrorDialog("Server Error", "An Unknown error occurred ");
//                            }
//                        });
//                    }

                }
            }
        });


    }


    View.OnClickListener cancelAmbulanceButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO add the logic to the button
            Intent returnIntent = new Intent();
            closeActivity(returnIntent,Activity.RESULT_CANCELED);
        }
    };

    private void closeActivity(Intent intent, int activityResult) {


        timer.cancel();
        setResult(activityResult,intent);
        this.finish();
        overridePendingTransition(R.anim.no_animation,R.anim.fade_out_slidedown);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent returnIntent = new Intent();
                closeActivity(returnIntent,Activity.RESULT_CANCELED);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        closeActivity(returnIntent,Activity.RESULT_CANCELED);


    }


    private void buildLoadingDialog() {
        loadingDialog = new MaterialDialog.Builder(this)
                .title("Finding Ambulance")
                .content("Finding you a nearby ambulance")
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
                .title("")
                .content("")
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

}
