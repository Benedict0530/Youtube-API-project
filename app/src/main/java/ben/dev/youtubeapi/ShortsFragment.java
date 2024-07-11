package ben.dev.youtubeapi;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import ben.dev.youtubeapi.api.YoutubeSearchResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShortsFragment extends Fragment {

    private static final String TAG = "ShortsFragment";
    private YouTubePlayerView youTubePlayerView;
    private String nextPageToken = ""; // Track next page token for pagination
    private ProgressBar loadingIndicator;
    private ImageView loadingBackground;

    public ShortsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shorts, container, false);
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        loadingBackground = view.findViewById(R.id.loadingBackground);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingBackground.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
        // Initialize YouTubePlayerView
        getLifecycle().addObserver(youTubePlayerView);

        // Load shorts videos
        loadShortsVideos();
    }

    private void loadShortsVideos() {
        ((MainActivity) requireActivity()).loadShortsVideosWithRetry(0, nextPageToken, new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                    // Retrieve the first video ID from the response
                    String videoId = response.body().getItems().get(0).getId().getVideoId(); // Assuming the first video in the list
                    if (videoId != null) {
                        playShortsVideo(videoId);
                    }else {
                        loadShortsVideos();
                    }
                    // Update nextPageToken for pagination
                    nextPageToken = response.body().getNextPageToken();
                } else {
                    Log.e(TAG, "Failed to fetch shorts videos or response body is empty");
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API request failed", t);
            }
        });
    }

    private void playShortsVideo(String videoId) {
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
                youTubePlayer.play();

                // Listen for video end
                youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                        super.onStateChange(youTubePlayer, state);
                        if (state == PlayerConstants.PlayerState.ENDED) {
                            // Video ended, replay
                            youTubePlayer.seekTo(0);
                            youTubePlayer.play();
                        }
                    }
                });

                loadingIndicator.setVisibility(View.GONE);
                loadingBackground.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (youTubePlayerView != null) {
            youTubePlayerView.release(); // Release YouTubePlayerView
        }
    }
}
