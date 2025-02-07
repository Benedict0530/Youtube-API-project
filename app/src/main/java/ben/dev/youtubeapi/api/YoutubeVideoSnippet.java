package ben.dev.youtubeapi.api;

import com.google.gson.annotations.SerializedName;

public class YoutubeVideoSnippet {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("thumbnails")
    private Thumbnails thumbnails;

    @SerializedName("channelTitle")
    private String channelTitle;

    // Add statistics field if necessary
    @SerializedName("statistics")
    private YoutubeVideoStatistics statistics;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public YoutubeVideoStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(YoutubeVideoStatistics statistics) {
        this.statistics = statistics;
    }
}
