package ben.dev.youtubeapi;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private String channelName_extra;
    private TextView channel_Name;
    private boolean isFullscreen = false;
    private ImageView fullscreenButton;
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
        channel_Name = findViewById(R.id.text_channel_name);


        // Get video ID from intent
        videoId = getIntent().getStringExtra("videoId");
        channelName_extra = getIntent().getStringExtra("channelName");

        channel_Name.setText(channelName_extra);


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
        // Filter out channel items and the current video from the list
        List<YoutubeVideo> filteredVideos = new ArrayList<>();
        for (YoutubeVideo video : videos) {
            if (video.getId() != null && video.getId().getKind().equals("youtube#video") && !video.getId().getVideoId().equals(videoId)) {
                filteredVideos.add(video);
            }
        }

        adapter = new VideoAdapter(this, filteredVideos, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeVideo video) {
                if (video.getId() != null && video.getId().getVideoId() != null) {
                    openVideoPlayer(video.getId().getVideoId(), filteredVideos, video.getId().getChannelId(), video.getSnippet().getChannelTitle());
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
    private void openVideoPlayer(String videoId, List<YoutubeVideo> videos, String channelId, String channelName) {
        if(videoId != null) {
            // Create a new intent for VideoPlayerActivity
            Intent intent = new Intent(VideoPlayerActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoId", videoId);
            intent.putExtra("channelId", channelId);
            intent.putExtra("channelName", channelName);
            intent.putExtra("relatedVideos", new Gson().toJson(videos)); // Pass filtered videos here
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
            finish();
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

        setImmersiveMode();

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

        reverseSetImmersiveMode();
        // Set flag
        isFullscreen = false;
    }

    private void setImmersiveMode() {
        // For Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // For older versions
            final int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
    private void reverseSetImmersiveMode() {
        // For Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
            }
        } else {
            // For older versions
            final int flags = 0; // No flags needed to reverse immersive mode
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

}
