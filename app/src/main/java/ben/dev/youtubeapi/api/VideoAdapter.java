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
            // Bind data to the view
            if (video.getId() != null) {
                YoutubeVideoSnippet snippet = video.getSnippet();
                if (snippet != null) {
                    textViewTitle.setText(snippet.getTitle());
                    textViewDescription.setText(snippet.getDescription());
                    Thumbnails thumbnails = snippet.getThumbnails();
                    Thumbnail mediumThumbnail = thumbnails.getMediumThumbnail();
                    String thumbnailUrl = (mediumThumbnail != null) ? mediumThumbnail.getUrl() : null;
                    Picasso.get().load(thumbnailUrl).into(imageViewThumbnail);
                }
            }

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
