package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideo {

    @SerializedName("viewCount")
    private String viewCount;

    @SerializedName("id")
    private YoutubeVideoId id;

    @SerializedName("snippet")
    private YoutubeVideoSnippet snippet;
    private YoutubeVideoStatistics statistics;

    public YoutubeVideoStatistics getStatistics() {
        return statistics;
    }

    // Getters and setters
    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public YoutubeVideoId getId() {
        return id;
    }

    public void setId(YoutubeVideoId id) {
        this.id = id;
    }

    public YoutubeVideoSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(YoutubeVideoSnippet snippet) {
        this.snippet = snippet;
    }


    public void setStatistics(YoutubeVideoStatistics statistics) {
        this.statistics = statistics;
    }
}
