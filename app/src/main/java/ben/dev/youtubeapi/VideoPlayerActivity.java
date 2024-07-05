package ben.dev.youtubeapi;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

import ben.dev.youtubeapi.api.VideoAdapter;
import ben.dev.youtubeapi.api.YoutubeVideo;

public class VideoPlayerActivity extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private String videoId;
    private boolean isFullscreen = false;
    private Button fullscreenButton;
    private RecyclerView recyclerViewRelatedVideos;
    private VideoAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Initialize views
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        fullscreenButton = findViewById(R.id.btn_fullscreen);
        recyclerViewRelatedVideos = findViewById(R.id.recyclerViewRelatedVideos);
        progressBar = findViewById(R.id.progress_bar);

        // Get video ID from intent
        videoId = getIntent().getStringExtra("videoId");

        // Initialize and play video
        initializePlayer();

        // Display related videos if available
        String relatedVideosJson = getIntent().getStringExtra("relatedVideos");
        if (relatedVideosJson != null) {
            List<YoutubeVideo> relatedVideos = new Gson().fromJson(relatedVideosJson, new TypeToken<List<YoutubeVideo>>(){}.getType());
            showRelatedVideos(relatedVideos);
        }
    }

    private void initializePlayer() {
        if (videoId != null) {
            getLifecycle().addObserver(youTubePlayerView);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0);
                    youTubePlayer.play();
                    fullscreenButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showRelatedVideos(List<YoutubeVideo> videos) {
        // Filter out channel items from the list
        List<YoutubeVideo> filteredVideos = new ArrayList<>();
        for (YoutubeVideo video : videos) {
            if (video.getId() != null && video.getId().getKind().equals("youtube#video")) {
                filteredVideos.add(video);
            }
        }

        adapter = new VideoAdapter(this, filteredVideos, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeVideo video) {
                if (video.getId() != null && video.getId().getVideoId() != null) {
                    openVideoPlayer(video.getId().getVideoId(), videos);
                } else {
                    Toast.makeText(VideoPlayerActivity.this, "Video ID is null", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerViewRelatedVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRelatedVideos.setAdapter(adapter);
        recyclerViewRelatedVideos.setVisibility(View.VISIBLE);
    }





    // Method to play a video using its ID
    private void openVideoPlayer(String videoId, List<YoutubeVideo> videos) {
        if(videoId != null) {
            // Pass all videos to the new instance of VideoPlayerActivity
            Gson gson = new Gson();
            String relatedVideosJson = gson.toJson(videos);

            // Create a new intent for VideoPlayerActivity
            Intent intent = new Intent(VideoPlayerActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoId", videoId);
            intent.putExtra("relatedVideos", relatedVideosJson);
            startActivity(intent);
        } else {
            // Handle the case where videoId is null
            Toast.makeText(VideoPlayerActivity.this, "Video ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        youTubePlayerView.release();
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreen();
            fullscreenButton.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    public void toggleFullscreen(View view) {
        if (!isFullscreen) {
            enterFullscreen();
        } else {
            exitFullscreen();
        }
    }

    private void enterFullscreen() {
        // Set landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // Set YouTubePlayerView to match parent
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) youTubePlayerView.getLayoutParams();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        youTubePlayerView.setLayoutParams(params);

        // Hide fullscreen button
        fullscreenButton.setVisibility(View.GONE);

        // Set flag
        isFullscreen = true;
    }

    private void exitFullscreen() {
        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Restore YouTubePlayerView size
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) youTubePlayerView.getLayoutParams();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        youTubePlayerView.setLayoutParams(params);

        // Show fullscreen button
        fullscreenButton.setVisibility(View.VISIBLE);

        // Set flag
        isFullscreen = false;
    }
}
