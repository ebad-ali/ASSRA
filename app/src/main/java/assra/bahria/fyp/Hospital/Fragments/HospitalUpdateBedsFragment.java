package assra.bahria.fyp.Hospital.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.io.IOException;

import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Driver.Models.Hospital;
import assra.bahria.fyp.Hospital.HospitalAppStateManager;
import assra.bahria.fyp.Hospital.Models.HospitalUpdateBedStatus;
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


public class HospitalUpdateBedsFragment extends Fragment {

    private TextInputLayout hospitalBedsCountTextInputLayout;
    private AppCompatEditText hospitalBedsCountInputEditTextView;
    private AppCompatButton updateHospitalBedsCountButton;
    private OkHttpClient client;
    private MaterialDialog loadingDialog;
    private MaterialDialog errorDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hospital_update_beds_fragment, container, false);



    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInputs();
        setUpdateHospitalBedsCountButton();
        setupListeners();
        setUpOkHttpClient();
        buildErrorDialog();
        buildLoadingDialog();
    }

    private void setUpOkHttpClient(){

        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }

    private void setupInputs(){
        hospitalBedsCountTextInputLayout = getView().findViewById(R.id.hospitalBedsCountTextInputLayout);
        hospitalBedsCountInputEditTextView = getView().findViewById(R.id.hospitalBedsCountInputEditTextView);

    }

    private void setUpdateHospitalBedsCountButton(){
        updateHospitalBedsCountButton = getView().findViewById(R.id.updateHospitalBedsCountButton);
    }
    private void setupListeners(){
        updateHospitalBedsCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBeds();
            }
        });
    }


    private void updateBeds(){
        String bedCount = hospitalBedsCountInputEditTextView.getText().toString();



        if (bedCount.length() == 0){
            hospitalBedsCountTextInputLayout.setError("Please enter valid bed count !");
            return;
        }


        hospitalBedsCountTextInputLayout.setError(null);





        try {
            showLoadingDialog("Updating beds count","please wait");
            updateBedsCall(String.valueOf(HospitalAppStateManager.getInstance().getUserID()),bedCount);
        }catch (Exception x){
            hideLoadingDialog();
            showErrorDialog("Exception", x.getMessage());
        }



    }




    private void updateBedsCall(String hospitalID, String bedCount) throws  Exception{

        RequestBody formBody = new FormBody.Builder()
                .add("id",hospitalID)
                .add("numBeds",bedCount)
                .build();

        Request request = new Request.Builder()
                .url(HospitalRoutes.updateBeds)
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
                                showErrorDialog("Server Error", "An unrecoverable error occured because body is null");
                            }
                        });

                        return;
                    }

                    String result =  responseBody.string();

                    Gson gson = new Gson();
                    final HospitalUpdateBedStatus hospitalUpdateBedStatus = gson.fromJson(result,HospitalUpdateBedStatus.class);



                    if (hospitalUpdateBedStatus.getStatus() < 1) {

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // wrong credentials
                                hideLoadingDialog();
                                showErrorDialog("Update Error", "Could not update number of beds.");
                            }
                        });

                    } else if  ( hospitalUpdateBedStatus.getStatus() > 0){

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                clearInputs();
                                hideLoadingDialog();
                                Toast.makeText(getContext(), "Updated number of beds" , Toast.LENGTH_SHORT).show();



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


    private void showLoadingDialog(String title, String content){
        if (!loadingDialog.isShowing()){
            loadingDialog.setTitle(title);
            loadingDialog.setContent(content);
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
