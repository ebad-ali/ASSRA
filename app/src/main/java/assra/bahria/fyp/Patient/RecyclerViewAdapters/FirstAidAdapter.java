package assra.bahria.fyp.Patient.RecyclerViewAdapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import assra.bahria.fyp.Common.GlideApp.GlideApp;
import assra.bahria.fyp.Patient.Models.FirstAidVideo;
import assra.bahria.fyp.R;

public class FirstAidAdapter extends RecyclerView.Adapter<FirstAidAdapter.FirstAidViewHolder> {


    private List<FirstAidVideo> firstAidVideoList;

    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public FirstAidAdapter(  List<FirstAidVideo> firstAidVideoList  ,Context context ){
        this.context = context;
        this.firstAidVideoList = firstAidVideoList;

    }

    public void swap( List<FirstAidVideo> firstAidVideoList){
        this.firstAidVideoList = firstAidVideoList;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }});
    }


    @NonNull
    @Override
    public FirstAidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View firstAidView = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_firstaidvideo_list_item,parent,false);
        return new FirstAidViewHolder(firstAidView);
    }

    @Override
    public void onBindViewHolder(@NonNull FirstAidViewHolder holder, int position) {
        FirstAidVideo firstAidVideo = firstAidVideoList.get(position);
        FirstAidViewHolder firstAidViewHolder = holder;
        firstAidViewHolder.thumbnailDescriptionTextView.setText(firstAidVideo.getDesc());
        GlideApp.with(getContext()).load(firstAidVideo.getYoutubeThumbnailUrl()).placeholder(R.color.primary_light).error(R.color.primary_light).into(firstAidViewHolder.videoThumbnailImageView);

    }

    @Override
    public int getItemCount() {
        return firstAidVideoList.size();
    }

    // view holders
    public static  class FirstAidViewHolder extends RecyclerView.ViewHolder {

        public ImageView videoThumbnailImageView;
        public TextView  thumbnailDescriptionTextView;

        public FirstAidViewHolder(View itemView) {
            super(itemView);
            videoThumbnailImageView = itemView.findViewById(R.id.videoThumbnailImageView);
            thumbnailDescriptionTextView = itemView.findViewById(R.id.thumbnailDescriptionTextView);
        }
    }
}
