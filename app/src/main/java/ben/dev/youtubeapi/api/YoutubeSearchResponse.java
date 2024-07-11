package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YoutubeSearchResponse {

    @SerializedName("items")
    private List<YoutubeVideo> items;

    @SerializedName("nextPageToken")
    private String nextPageToken;

    public List<YoutubeVideo> getItems() {
        return items;
    }

    public void setItems(List<YoutubeVideo> items) {
        this.items = items;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
