package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideoStatistics {

    @SerializedName("viewCount")
    private long viewCount;

    // Add other statistics if needed, e.g., likeCount, dislikeCount, etc.

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    // Add getters and setters for other statistics if needed
}
