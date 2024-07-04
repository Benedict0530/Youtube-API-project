package ben.dev.youtubeapi;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

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

    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText editTextSearch;
    private Button buttonSearch;
    private RecyclerView recyclerViewVideos;

    private YoutubeApiService apiService;
    private VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextSearch = findViewById(R.id.editTextSearch);
        buttonSearch = findViewById(R.id.buttonSearch);
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);

        Picasso.setSingletonInstance(new Picasso.Builder(this).build());

        apiService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YoutubeApiService.class);

        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this));

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

    private void searchVideos(String query) {
        String apiKey = "AIzaSyBfenkvDY-TOMEJenUPt5YCvfZh7pmqtmU"; // Replace with your actual API key

        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YoutubeVideo> videos = response.body().getItems();
                    if (videos != null && !videos.isEmpty()) {
                        // Correct constructor usage for VideoAdapter
                        adapter = new VideoAdapter(MainActivity.this, videos, new VideoAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(YoutubeVideo video) {
                                if (video.getId() != null) {
                                    playVideo(video.getId().getVideoId());
                                } else {
                                    Toast.makeText(MainActivity.this, "Video ID is null", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        recyclerViewVideos.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "No videos found in response");
                        Toast.makeText(MainActivity.this, "No videos found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Failed to get videos";
                    if (response.message() != null) {
                        errorMessage += ": " + response.message();
                    }
                    Log.e(TAG, errorMessage);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch videos: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Failed to fetch videos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }


        });
    }

    private void playVideo(String videoId) {
        // Implement video playback logic here
    }
}
