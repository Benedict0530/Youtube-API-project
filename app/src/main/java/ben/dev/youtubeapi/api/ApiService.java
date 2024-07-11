package ben.dev.youtubeapi.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.Map;

public interface ApiService {

    // Endpoint to update API key status
    @POST("/api/updateKeyStatus")
    Call<Void> updateApiKeyStatus(@Body Map<String, String> apiKeyStatusMap);

    // Endpoint to fetch API key statuses
    @GET("/api/getKeyStatuses")
    Call<Map<String, Long>> getApiKeyStatuses();
}
