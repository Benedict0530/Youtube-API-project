package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideoId {

    @SerializedName("videoId")
    private String videoId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
