package ben.dev.youtubeapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
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

    private void loadPopularVideos() {
        String apiKey = "AIzaSyB0_ip-ZbKsd4BIgxQjVnHirUm_nHxRx8Q"; // Replace with your actual API key
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
                    handleFailedResponse(response);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }



    // Method to search videos based on query
    private void searchVideos(String query) {
        String apiKey = "AIzaSyB0_ip-ZbKsd4BIgxQjVnHirUm_nHxRx8Q"; // Replace with your actual API key

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
                    handleFailedResponse(response);
                }
            }


            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }



    // Method to handle successful response and display search results
    private void showVideos(List<YoutubeVideo> videos) {
        adapter = new VideoAdapter(MainActivity.this, videos, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YoutubeVideo video) {
                if (video.getId() != null) {
                    openVideoPlayer(video.getId().getVideoId());
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

    // Method to handle failed API response
    private void handleFailedResponse(Response<YoutubeSearchResponse> response) {
        String errorMessage = "Failed to get videos";
        if (response.message() != null) {
            errorMessage += ": " + response.message();
        }
        Log.e(TAG, errorMessage);
        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
    private void openVideoPlayer(String videoId) {
        Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        intent.putExtra("videoId", videoId);
        startActivity(intent);
    }
}
