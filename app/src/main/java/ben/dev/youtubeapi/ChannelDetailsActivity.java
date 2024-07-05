package ben.dev.youtubeapi;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChannelDetailsActivity extends AppCompatActivity {

    private TextView channelNameTextView;
    private TextView channelIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_details);

        // Initialize views
        channelNameTextView = findViewById(R.id.channelNameTextView);
        channelIdTextView = findViewById(R.id.channelIdTextView);

        // Retrieve channel details from intent
        String channelId = getIntent().getStringExtra("channelId");
        String channelName = getIntent().getStringExtra("channelName");

        // Set channel details to views
        if (channelName != null) {
            channelNameTextView.setText(channelName);
        }
        if (channelId != null) {
            channelIdTextView.setText(channelId);
        }
    }
}
