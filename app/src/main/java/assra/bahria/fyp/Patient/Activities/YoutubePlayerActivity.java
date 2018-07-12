package assra.bahria.fyp.Patient.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import assra.bahria.fyp.Common.Utils.YoutubeIDExtractor;
import assra.bahria.fyp.R;

public class YoutubePlayerActivity extends YouTubeBaseActivity {

    private YouTubePlayerView youtubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    private String videoUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_youtube_player);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
             videoUrl = extras.getString("youtubeVideoUrl");

        }



        setUpYoutubePlayer();
    }


    private void setUpYoutubePlayer(){
        youtubePlayerView = findViewById(R.id.youtubePlayer);
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if (videoUrl != null) {
                    youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);

                    youTubePlayer.loadVideo(YoutubeIDExtractor.getVideoId(videoUrl));
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(YoutubePlayerActivity.this, "Could not load video !", Toast.LENGTH_SHORT).show();
            }
        };


        youtubePlayerView.initialize( getString(R.string.youtube_api_key), onInitializedListener);


    }


}
