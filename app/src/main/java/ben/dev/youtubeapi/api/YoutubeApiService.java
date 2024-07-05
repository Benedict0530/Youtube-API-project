package ben.dev.youtubeapi.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApiService {
    // Add this method to your YoutubeApiService interface

    @GET("search")
    Call<YoutubeSearchResponse> searchVideos(
            @Query("part") String part,
            @Query("q") String query,
            @Query("key") String apiKey
    );


}
