package ben.dev.youtubeapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import ben.dev.youtubeapi.api.VideoAdapter;
import ben.dev.youtubeapi.api.YoutubeSearchResponse;
import ben.dev.youtubeapi.api.YoutubeVideo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private EditText editTextSearch;
    private ImageView buttonSearch;
    private ImageView buttonLogo;
    private RecyclerView recyclerViewVideos;
    private ProgressBar loadingIndicator;

    private VideoAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        buttonLogo = view.findViewById(R.id.logo);
        recyclerViewVideos = view.findViewById(R.id.recyclerViewVideos);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadPopularVideos();

        buttonLogo.setOnClickListener(v -> {
            loadPopularVideos();
            editTextSearch.setText("");
        });

        buttonSearch.setOnClickListener(v -> {
            String query = editTextSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchVideos(query);
            } else {
                Toast.makeText(getActivity(), "Search input cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchVideos(query);
                } else {
                    Toast.makeText(getActivity(), "Search input cannot be empty", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        return view;
    }

    private void loadPopularVideos() {
        loadingIndicator.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).loadPopularVideos(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YoutubeVideo> videos = response.body().getItems();
                    if (videos != null && !videos.isEmpty()) {
                        showVideos(videos);
                    } else {
                        handleNoVideosFound();
                    }
                } else {
                    handleNoVideosFound();
                }
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API Request failed", t);
                handleNetworkError(t);
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void searchVideos(String query) {
        loadingIndicator.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).searchVideos(query, new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YoutubeVideo> videos = response.body().getItems();
                    if (videos != null && !videos.isEmpty()) {
                        showVideos(videos);
                    } else {
                        handleNoVideosFound();
                    }
                } else {
                    handleNoVideosFound();
                }
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API Request failed", t);
                handleNetworkError(t);
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void showVideos(List<YoutubeVideo> videos) {
        adapter = new VideoAdapter(getActivity(), videos, video -> {
            if (video.getId() != null) {
                if ("youtube#video".equals(video.getId().getKind())) {
                    openVideoPlayer(video.getId().getVideoId(), videos, video.getId().getChannelId(), video.getSnippet().getChannelTitle());
                } else if ("youtube#channel".equals(video.getId().getKind())) {
                    openChannelLink(video.getId().getChannelId(), video.getSnippet().getChannelTitle());
                }
            } else {
                Toast.makeText(getActivity(), "Video ID is null", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewVideos.setAdapter(adapter);
        recyclerViewVideos.setVisibility(View.VISIBLE);
    }

    private void handleNoVideosFound() {
        Log.d(TAG, "No videos found in response");
        Toast.makeText(getActivity(), "No videos found", Toast.LENGTH_SHORT).show();
    }

    private void handleNetworkError(Throwable t) {
        String errorMessage = "Failed to fetch videos";
        if (t instanceof java.net.UnknownHostException) {
            errorMessage += ": Please check your internet connection";
        } else {
            errorMessage += ": " + t.getMessage();
        }
        Log.e(TAG, errorMessage, t);
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void openVideoPlayer(String videoId, List<YoutubeVideo> videos, String channelId, String channelName) {
        if (videoId != null) {
            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
            intent.putExtra("videoId", videoId);
            intent.putExtra("channelId", channelId);
            intent.putExtra("channelName", channelName);
            intent.putExtra("relatedVideos", new Gson().toJson(videos));
            startActivity(intent);
        } else {
            loadPopularVideos();
        }
    }

    private void openChannelLink(String channelId, String channelName) {
        if (channelId != null) {
            Intent intent = new Intent(getActivity(), ChannelDetailsActivity.class);
            intent.putExtra("channelId", channelId);
            intent.putExtra("channelName", channelName);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Channel ID is null", Toast.LENGTH_SHORT).show();
        }
    }


}