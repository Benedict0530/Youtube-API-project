package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideo {

    @SerializedName("id")
    private YoutubeVideoId id;

    @SerializedName("snippet")
    private YoutubeVideoSnippet snippet;

    // Example pagination metadata fields
    private String nextPageToken; // Store next page token if needed

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

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
