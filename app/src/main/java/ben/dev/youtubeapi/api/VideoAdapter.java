package ben.dev.youtubeapi.api;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ben.dev.youtubeapi.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<YoutubeVideo> videos;
    private Context context;
    private OnItemClickListener listener;

    public VideoAdapter(Context context, List<YoutubeVideo> videos, OnItemClickListener listener) {
        this.context = context;
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(videos.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        ImageView imageViewThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageViewThumbnail = itemView.findViewById(R.id.imageViewThumbnail);
        }

        public void bind(final YoutubeVideo video, final OnItemClickListener listener) {
            if (video.getId() != null) {
                String videoIdString = video.getId().getVideoId();
                Log.d("VideoAdapter", "Binding video with ID: " + videoIdString);

                YoutubeVideoSnippet snippet = video.getSnippet();
                if (snippet != null) {
                    // Set title and description
                    textViewTitle.setText(snippet.getTitle());
                    textViewDescription.setText(snippet.getDescription());

                    // Load thumbnail if available
                    Thumbnails thumbnails = snippet.getThumbnails();
                    if (thumbnails != null) {
                        // Choose medium thumbnail if available
                        Thumbnail mediumThumbnail = thumbnails.getMediumThumbnail();
                        String thumbnailUrl = (mediumThumbnail != null) ? mediumThumbnail.getUrl() : null;

                        // Log the thumbnail URL
                        Log.d("VideoAdapter", "Thumbnail URL: " + thumbnailUrl);

                        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                            Picasso.get().load(thumbnailUrl)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(imageViewThumbnail);
                        } else {
                            imageViewThumbnail.setImageResource(R.drawable.ic_launcher_background);
                            Log.d("Picasso", "Thumbnail URL is null or empty for video ID: " + videoIdString);
                        }
                    } else {
                        imageViewThumbnail.setImageResource(R.drawable.ic_launcher_background);
                        Log.d("Picasso", "Thumbnails object is null for video ID: " + videoIdString);
                    }
                } else {
                    Log.d("Snippet", "Snippet object is null for video ID: " + videoIdString);
                }
            } else {
                Log.d("VideoAdapter", "Video ID is null for video, unable to load thumbnail.");
                imageViewThumbnail.setImageResource(R.drawable.ic_launcher_background);
            }

            // Handle item click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(video);
                    }
                }
            });
        }

    }

    public interface OnItemClickListener {
        void onItemClick(YoutubeVideo video);
    }
}
