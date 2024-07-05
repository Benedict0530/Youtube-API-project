package ben.dev.youtubeapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import ben.dev.youtubeapi.api.VideoAdapter;
import ben.dev.youtubeapi.api.YoutubeApiService;
import ben.dev.youtubeapi.api.YoutubeSearchResponse;
import ben.dev.youtubeapi.api.YoutubeVideo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLog";

    private EditText editTextSearch;
    private Button buttonSearch;
    private RecyclerView recyclerViewVideos;

    private YoutubeApiService apiService;
    private VideoAdapter adapter;
    private List<String> apiKeys;
    private int currentApiKeyIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonSearch = findViewById(R.id.buttonSearch);
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);

        // Read API keys from file
        apiKeys = readApiKeys();

        // Initialize Retrofit and YoutubeApiService
        apiService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YoutubeApiService.class);

        // Set up RecyclerView
        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this));

        loadPopularVideos();
        // Set onClickListener for Search button
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchVideos(query);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private List<String> readApiKeys() {
        List<String> keys = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("api_keys.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                keys.add(line.trim());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading API keys", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return keys;
    }

    private void loadPopularVideos() {
        loadPopularVideosWithRetry(currentApiKeyIndex);
    }

    private void searchVideos(String query) {
        searchVideosWithRetry(query, currentApiKeyIndex);
    }

    private void loadPopularVideosWithRetry(final int apiKeyIndex) {
        String apiKey = apiKeys.get(apiKeyIndex);
        String query = "popular videos"; // Example query for popular videos

        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", new Gson().toJson(response.body())); // Log the response
                    List<YoutubeVideo> videos = response.body().getItems();
                    if (videos != null && !videos.isEmpty()) {
                        showVideos(videos);
                    } else {
                        handleNoVideosFound();
                    }
                } else {
                    // Retry with the next API key
                    int nextApiKeyIndex = (apiKeyIndex + 1) % apiKeys.size();
                    loadPopularVideosWithRetry(nextApiKeyIndex);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                // Log the error
                Log.e(TAG, "API Request failed with API key: " + apiKey, t);

                // Retry with the next API key
                int nextApiKeyIndex = (apiKeyIndex + 1) % apiKeys.size();
                loadPopularVideosWithRetry(nextApiKeyIndex);
            }
        });
        Log.d(TAG, "Using API key: " + apiKey + " for request.");
    }

    private void searchVideosWithRetry(final String query, final int apiKeyIndex) {
        String apiKey = apiKeys.get(apiKeyIndex);

        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response", new Gson().toJson(response.body())); // Log the response
                    List<YoutubeVideo> videos = response.body().getItems();
                    if (videos != null && !videos.isEmpty()) {
                        showVideos(videos);
                    } else {
                        handleNoVideosFound();
                    }
                } else {
                    // Retry with the next API key
                    int nextApiKeyIndex = (apiKeyIndex + 1) % apiKeys.size();
                    searchVideosWithRetry(query, nextApiKeyIndex);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                // Log the error
                Log.e(TAG, "API Request failed with API key: " + apiKey, t);

                // Retry with the next API key
                int nextApiKeyIndex = (apiKeyIndex + 1) % apiKeys.size();
                searchVideosWithRetry(query, nextApiKeyIndex);
            }
        });
        Log.d(TAG, "Using API key: " + apiKey + " for request.");
    }

    // Method to handle successful response and display search results
    private void showVideos(List<YoutubeVideo> videos) {
        adapter = new VideoAdapter(MainActivity.this, videos, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeVideo video) {
                if (video.getId() != null) {
                    if ("youtube#video".equals(video.getId().getKind())) {
                        openVideoPlayer(video.getId().getVideoId(), videos);
                    } else if ("youtube#channel".equals(video.getId().getKind())) {
                        openChannelLink(video.getId().getChannelId(), video.getSnippet().getChannelTitle());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Video ID is null", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerViewVideos.setAdapter(adapter);
        recyclerViewVideos.setVisibility(View.VISIBLE);
    }

    // Method to handle case when no videos are found
    private void handleNoVideosFound() {
        Log.d(TAG, "No videos found in response");
        Toast.makeText(MainActivity.this, "No videos found", Toast.LENGTH_SHORT).show();
    }

    // Method to handle network errors
    private void handleNetworkError(Throwable t) {
        String errorMessage = "Failed to fetch videos";
        if (t instanceof java.net.UnknownHostException) {
            errorMessage += ": Please check your internet connection";
        } else {
            errorMessage += ": " + t.getMessage();
        }
        Log.e(TAG, errorMessage, t);
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Method to play a video using its ID
    private void openVideoPlayer(String videoId, List<YoutubeVideo> videos) {
        if (videoId != null) {
            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            intent.putExtra("videoId", videoId);
            // Pass all videos to VideoPlayerActivity
            intent.putExtra("relatedVideos", new Gson().toJson(videos));
            startActivity(intent);
        } else {
            loadPopularVideos();
        }
    }

    private void openChannelLink(String channelId, String channelName) {
        if (channelId != null) {
            Intent intent = new Intent(MainActivity.this, ChannelDetailsActivity.class);
            intent.putExtra("channelId", channelId);
            intent.putExtra("channelName", channelName);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Channel ID is null", Toast.LENGTH_SHORT).show();
        }
    }

}
