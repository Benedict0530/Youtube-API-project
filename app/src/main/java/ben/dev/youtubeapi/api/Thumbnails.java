package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class Thumbnails {
    @SerializedName("default")
    private Thumbnail defaultThumbnail;

    @SerializedName("medium")
    private Thumbnail mediumThumbnail;

    @SerializedName("high")
    private Thumbnail highThumbnail;

    // Getters and setters
    public Thumbnail getDefaultThumbnail() {
        return defaultThumbnail;
    }

    public void setDefaultThumbnail(Thumbnail defaultThumbnail) {
        this.defaultThumbnail = defaultThumbnail;
    }

    public Thumbnail getMediumThumbnail() {
        return mediumThumbnail;
    }

    public void setMediumThumbnail(Thumbnail mediumThumbnail) {
        this.mediumThumbnail = mediumThumbnail;
    }

    public Thumbnail getHighThumbnail() {
        return highThumbnail;
    }

    public void setHighThumbnail(Thumbnail highThumbnail) {
        this.highThumbnail = highThumbnail;
    }
}
