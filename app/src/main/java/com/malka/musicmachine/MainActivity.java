package com.malka.musicmachine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_SONG = "song";
    private boolean mBound = false;
    private PlayerService mPlayerService;
    private Button mDownloadButton;
    private Button mPlayButton;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBound = true;
            PlayerService.LocalBinder localBinder = (PlayerService.LocalBinder) binder;
            mPlayerService = localBinder.getService();
            if (mPlayerService.isPlaying()) {
                mPlayButton.setText("Pause");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownloadButton = (Button) findViewById(R.id.downloadButton);
        mPlayButton = (Button) findViewById(R.id.playButton);

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Downloading", Toast.LENGTH_SHORT).show();

                // Send Messages to Handler for processing
                for (String song : Playlist.songs) {
                    Intent intent = new Intent(MainActivity.this, DownloadIntentService.class);
                    intent.putExtra(KEY_SONG, song);
                    startService(intent);
                }
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    if (mPlayerService.isPlaying()) {
                        mPlayerService.pause();
                        mPlayButton.setText("Play");
                    } else {
                        mPlayerService.play();
                        mPlayButton.setText("Pause");
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }
}