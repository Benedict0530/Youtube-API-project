package ben.dev.youtubeapi;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ben.dev.youtubeapi.api.ApiService;
import ben.dev.youtubeapi.api.YoutubeApiService;
import ben.dev.youtubeapi.api.YoutubeSearchResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long API_KEY_COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private BottomNavigationView bottomNavigationView;
    private YoutubeApiService apiService;
    private ApiService backendService;
    private List<String> apiKeys;
    private Map<String, Long> apiKeyFailureTimestamps;
    private int currentApiKeyIndex = 0;
    private String nextPageToken = "";
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setImmersiveMode();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Initialize API service and fetch API key statuses from backend
        initApiService();
        initBackendService();
    }

    private void initApiService() {
        apiService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YoutubeApiService.class);
    }

    private void initBackendService() {
        backendService = new Retrofit.Builder()
                .baseUrl("https://api-yt-backend.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        // Fetch API key statuses from backend
        fetchApiKeyStatuses();
    }

    private void fetchApiKeyStatuses() {
        backendService.getApiKeyStatuses().enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call, Response<Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    apiKeyFailureTimestamps = response.body();
                    // Now initialize API keys and start using them
                    initApiKeys();
                    // Load default fragment after initializing keys
                    loadDefaultFragment();
                } else {
                    fetchApiKeyStatuses();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Long>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch API key statuses", t);
                // Handle failure to fetch API key statuses
                apiKeyFailureTimestamps = new HashMap<>();
                initApiKeys(); // Initialize with default keys or handle the error condition
                // Load default fragment after initializing keys
                loadDefaultFragment();
            }
        });
    }

    private void initApiKeys() {
        if (apiKeys == null) {
            apiKeys = new ArrayList<>();
        } else {
            apiKeys.clear(); // Clear the list before re-initializing
        }

        for (int i = 1; i <= 24; i++) {
            try {
                String apiKey = BuildConfig.class.getField("YOUTUBE_API_KEY" + i).get(null).toString();
                Long failureTimestamp = apiKeyFailureTimestamps.get(apiKey);
                if (failureTimestamp == null || (System.currentTimeMillis() - failureTimestamp) > API_KEY_COOLDOWN_MS) {
                    apiKeys.add(apiKey);
                }
            } catch (IllegalAccessException | NoSuchFieldException | NullPointerException e) {
                Log.e(TAG, "Failed to retrieve API key " + i, e);
                // Handle exception (e.g., fallback to default keys)
                apiKeys.add("default_api_key_" + i); // Example: Add a default key
            }
        }
    }

    private void loadDefaultFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        loadingIndicator.setVisibility(View.GONE);
    }

    // Method to load popular videos in the HomeFragment
    public void loadPopularVideos(Callback<YoutubeSearchResponse> callback) {
        loadPopularVideosWithRetry(currentApiKeyIndex, callback);
    }

    // Method to search videos based on query
    public void searchVideos(String query, Callback<YoutubeSearchResponse> callback) {
        searchVideosWithRetry(query, currentApiKeyIndex, callback);
    }

    // Retry mechanism for loading popular videos with API key rotation
    private void loadPopularVideosWithRetry(final int apiKeyIndex, final Callback<YoutubeSearchResponse> callback) {
        String apiKey = getNextAvailableApiKey(apiKeyIndex);
        if (apiKey == null) {
            Toast.makeText(MainActivity.this, "All API keys exhausted. Please try again later.", Toast.LENGTH_SHORT).show();
            callback.onFailure(null, new Throwable("All API keys exhausted."));
            return;
        }

        String query = "Trending Videos Today";
        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey, 20);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    markApiKeyAsFailed(apiKey);
                    retryLoadPopularVideos(apiKeyIndex, callback);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API Request failed with API key: " + apiKey, t);
                markApiKeyAsFailed(apiKey);
                retryLoadPopularVideos(apiKeyIndex, callback);
            }
        });
        Log.d(TAG, "Using API key: " + apiKey + " for request.");
    }

    // Retry mechanism for loading popular videos
    private void retryLoadPopularVideos(int apiKeyIndex, Callback<YoutubeSearchResponse> callback) {
        loadPopularVideosWithRetry((apiKeyIndex + 1) % apiKeys.size(), callback);
    }
    // Method to load shorts videos with API key rotation and retry
    public void loadShortsVideosWithRetry(final int apiKeyIndex, String nextPageToken, final Callback<YoutubeSearchResponse> callback) {
        String apiKey = getNextAvailableApiKey(apiKeyIndex);
        if (apiKey == null) {
            Toast.makeText(MainActivity.this, "All API keys exhausted. Please try again later.", Toast.LENGTH_SHORT).show();
            callback.onFailure(null, new Throwable("All API keys exhausted."));
            return;
        }

        String query = "Youtube Trending Shorts";
        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey, 20);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    markApiKeyAsFailed(apiKey);
                    retryLoadShortsVideos(apiKeyIndex, callback);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API Request failed with API key: " + apiKey, t);
                markApiKeyAsFailed(apiKey);
                retryLoadShortsVideos(apiKeyIndex, callback);
            }
        });
        Log.d(TAG, "Using API key: " + apiKey + " for request.");
    }

    // Retry mechanism for loading shorts videos
    private void retryLoadShortsVideos(int apiKeyIndex, Callback<YoutubeSearchResponse> callback) {
        loadShortsVideosWithRetry((apiKeyIndex + 1) % apiKeys.size(), nextPageToken, callback);
    }



    // Retry mechanism for searching videos based on query
    private void searchVideosWithRetry(final String query, final int apiKeyIndex, final Callback<YoutubeSearchResponse> callback) {
        String apiKey = getNextAvailableApiKey(apiKeyIndex);
        if (apiKey == null) {
            Toast.makeText(MainActivity.this, "All API keys exhausted. Please try again later.", Toast.LENGTH_SHORT).show();
            callback.onFailure(null, new Throwable("All API keys exhausted."));
            return;
        }

        Call<YoutubeSearchResponse> call = apiService.searchVideos("snippet", query, apiKey, 20);
        call.enqueue(new Callback<YoutubeSearchResponse>() {
            @Override
            public void onResponse(Call<YoutubeSearchResponse> call, Response<YoutubeSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    markApiKeyAsFailed(apiKey);
                    retrySearchVideos(query, apiKeyIndex, callback);
                }
            }

            @Override
            public void onFailure(Call<YoutubeSearchResponse> call, Throwable t) {
                Log.e(TAG, "API Request failed with API key: " + apiKey, t);
                markApiKeyAsFailed(apiKey);
                retrySearchVideos(query, apiKeyIndex, callback);
            }
        });
        Log.d(TAG, "Using API key: " + apiKey + " for request.");
    }

    // Retry mechanism for searching videos
    private void retrySearchVideos(String query, int apiKeyIndex, Callback<YoutubeSearchResponse> callback) {
        searchVideosWithRetry(query, (apiKeyIndex + 1) % apiKeys.size(), callback);
    }

    // Method to get the next available API key for use
    private String getNextAvailableApiKey(int startIndex) {
        if (apiKeys == null || apiKeys.isEmpty()) {
            Log.e(TAG, "API keys list is null or empty.");
            return null; // Or handle this condition based on your logic
        }

        if (apiKeyFailureTimestamps == null) {
            Log.e(TAG, "apiKeyFailureTimestamps is null. Initializing with an empty map.");
            apiKeyFailureTimestamps = new HashMap<>(); // Initialize with an empty map
        }

        for (int i = 0; i < apiKeys.size(); i++) {
            int index = (startIndex + i) % apiKeys.size();
            String apiKey = apiKeys.get(index);
            Long failureTimestamp = apiKeyFailureTimestamps.get(apiKey);
            if (failureTimestamp == null || (System.currentTimeMillis() - failureTimestamp) > API_KEY_COOLDOWN_MS) {
                return apiKey;
            }
        }
        return null; // Handle case when no API key is available
    }

    // Method to mark an API key as failed and update status
    private void markApiKeyAsFailed(String apiKey) {
        // Store the failure timestamp locally
        apiKeyFailureTimestamps.put(apiKey, System.currentTimeMillis());

        // Send the failure timestamp to backend
        Map<String, String> body = new HashMap<>();
        body.put("apiKey", apiKey);
        body.put("timestamp", String.valueOf(System.currentTimeMillis()));

        backendService.updateApiKeyStatus(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "API key status updated on server.");
                } else {
                    Log.e(TAG, "Failed to update API key status on server: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to update API key status on server", t);
            }
        });
    }

    // Method to set immersive mode for full screen experience
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

    // Listener for bottom navigation view item selection
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    // Check if the current fragment is already HomeFragment or ShortsFragment
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (item.getItemId() == R.id.nav_home && !(currentFragment instanceof HomeFragment)) {
                        selectedFragment = new HomeFragment();
                    } else if (item.getItemId() == R.id.nav_shorts && !(currentFragment instanceof ShortsFragment)) {
                        selectedFragment = new ShortsFragment();
                    }

                    // Replace fragment only if a different fragment is selected
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                        return true;
                    }

                    return false;
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        setImmersiveMode();
    }
}
