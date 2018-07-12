package assra.bahria.fyp.Patient.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import assra.bahria.fyp.Common.Utils.CookieManager;
import assra.bahria.fyp.Common.Utils.ItemClickSupport;
import assra.bahria.fyp.Common.Utils.OnFragmentInteractionListener;
import assra.bahria.fyp.Patient.Models.FirstAidVideo;
import assra.bahria.fyp.Patient.Networking.PatientRoutes;
import assra.bahria.fyp.Patient.RecyclerViewAdapters.FirstAidAdapter;
import assra.bahria.fyp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class FirstAidVideosFragment extends Fragment {

    private static final String TAG = "FirstAidVideosFragment";


    private List<FirstAidVideo> firstAidVideoList;

    private OnFragmentInteractionListener<Object> onFragmentInteractionListener = null;
    // recycler view specifics
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager recyclerViewlayoutManager;
    private FirstAidAdapter firstAidRecyclerViewAdapter;

    private OkHttpClient client;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView errorText;
    private LinearLayout loadingGroupLinearLayout;
    private static boolean isAlreadyOn = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.patient_fragment_first_aid_videos, container, false);


    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener<Object>)context;

        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        onFragmentInteractionListener = null;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setUpOkHttpClient();
        setUpSwipeRefresh();
        setUpRecyclyerView();
        setUpRecyclerViewClicklListener();
        setUpLoadingGroup();
        setUpErrorText();

    }

    private void setUpRecyclyerView(){
        recyclerView = getView().findViewById(R.id.firstAidRecyclerView);
        recyclerViewlayoutManager = new LinearLayoutManager(getContext());
        firstAidRecyclerViewAdapter = new FirstAidAdapter(new ArrayList<FirstAidVideo>(), getContext());
        recyclerView.setLayoutManager(recyclerViewlayoutManager);
        recyclerView.setAdapter(firstAidRecyclerViewAdapter);
    }



    private void setUpOkHttpClient() {
        client = new OkHttpClient.Builder()
                .cookieJar(CookieManager.getInstance().getCookieJar())
                .build();
    }


    private  void setUpRecyclerViewClicklListener(){
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

               FirstAidVideo firstAidVideo =  firstAidVideoList.get(position);

                onFragmentInteractionListener.onFragmentInteraction("OpenYoutube",firstAidVideo.getLink());

            }
        });
    }


    private void setUpSwipeRefresh(){
        swipeRefresh = getView().findViewById(R.id.swipeRefresh);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFirstAidClientCall();
            }
        });
    }

    private void setUpErrorText(){
        errorText = getView().findViewById(R.id.errorText);
        errorText.setVisibility(View.GONE);
    }

    private void setUpLoadingGroup(){
        loadingGroupLinearLayout = getView().findViewById(R.id.loadingGroup);
        progressBar = getView().findViewById(R.id.progressBar);
        progressBar.animate();
        loadingGroupLinearLayout.setVisibility(View.VISIBLE);
    }




    private void initialFirstAidNetworkCall(){




        Request request = new Request.Builder()
                .url(PatientRoutes.videos)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {


                        swipeRefresh.setEnabled(true);
                        errorText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        loadingGroupLinearLayout.setVisibility(View.GONE);
                    }});
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                    {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                swipeRefresh.setEnabled(true);
                            }});
                        throw new IOException("Unexpected code " + response);
                    }
                    if (responseBody == null) {


                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                swipeRefresh.setEnabled(true);
                            }});
                        return;
                    }


                    String result =  responseBody.string();
                    Log.d(TAG, "onResponse: " + result );
                    Gson gson = new Gson();



                    FirstAidVideo[] firstAidVideos = gson.fromJson(result, FirstAidVideo[].class);


                    firstAidVideoList = new ArrayList<>();




                    firstAidVideoList.addAll(Arrays.asList(firstAidVideos));

                    if (firstAidVideoList.size() == 0) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                swipeRefresh.setEnabled(true);
                            }});
                        return;
                    }

                    firstAidRecyclerViewAdapter.setContext(getContext());
                    firstAidRecyclerViewAdapter.swap(firstAidVideoList);


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            loadingGroupLinearLayout.setVisibility(View.GONE);
                            errorText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            swipeRefresh.setEnabled(true);
                        }
                    });

                }
            }
        });
    }

    private void refreshFirstAidClientCall(){


        if (swipeRefresh.isEnabled()) {
            swipeRefresh.setRefreshing(true);
        }

        Request request = new Request.Builder()
                .url(PatientRoutes.videos)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefresh.isEnabled()) {
                            swipeRefresh.setRefreshing(false);
                        }

                        loadingGroupLinearLayout.setVisibility(View.GONE);
                        swipeRefresh.setEnabled(true);
                    }});
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                    {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (swipeRefresh.isEnabled()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                swipeRefresh.setEnabled(true);
                            }});
                        throw new IOException("Unexpected code " + response);
                    }
                    if (responseBody == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (swipeRefresh.isEnabled()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                swipeRefresh.setEnabled(true);
                            }});
                        return;
                    }
                    String result =  responseBody.string();
                    Log.d(TAG, "onResponse: " + result );


                    Gson gson = new Gson();



                    FirstAidVideo[] firstAidVideos = gson.fromJson(result, FirstAidVideo[].class);


                    firstAidVideoList = new ArrayList<>();



                    firstAidVideoList.addAll(Arrays.asList(firstAidVideos));

                    if (firstAidVideoList.size() == 0) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {

                                loadingGroupLinearLayout.setVisibility(View.GONE);
                                errorText.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                swipeRefresh.setEnabled(true);
                            }});
                        return;
                    }

                    firstAidRecyclerViewAdapter.setContext(getContext());
                    firstAidRecyclerViewAdapter.swap(firstAidVideoList);




                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            if (swipeRefresh.isEnabled()) {
                                swipeRefresh.setRefreshing(false);
                            }
                            loadingGroupLinearLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            errorText.setVisibility(View.GONE);
                            swipeRefresh.setEnabled(true);
                        }
                    });

                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();


        if (!isAlreadyOn) {
            try {

                initialFirstAidNetworkCall();

            } catch (Exception x) {

            }
            isAlreadyOn = true;
        }
    }

}
