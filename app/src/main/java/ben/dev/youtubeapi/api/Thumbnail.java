package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class Thumbnail {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
