package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideoId {

    // This field will hold the videoId when available
    @SerializedName("videoId")
    private String videoId;

    // For other cases, it can be a generic id field
    @SerializedName("id")
    private String id;

    // Getters and setters
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
