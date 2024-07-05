package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YoutubeSearchResponse {

    @SerializedName("items")
    private List<YoutubeVideo> items;

    public List<YoutubeVideo> getItems() {
        return items;
    }

    public void setItems(List<YoutubeVideo> items) {
        this.items = items;
    }

}
