package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideoStatistics {
    @SerializedName("viewCount")
    private int viewCount;

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
