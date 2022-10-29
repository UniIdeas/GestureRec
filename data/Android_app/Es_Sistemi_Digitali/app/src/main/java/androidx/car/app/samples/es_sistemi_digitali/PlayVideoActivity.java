package androidx.car.app.samples.es_sistemi_digitali;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class PlayVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback
{
    private MediaPlayer mediaPlayer;
    private SurfaceHolder holder;
    private SurfaceView surface;
    private Uri contentUri;
    private Button home,back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.video);

            //---------------- home ----------------------------------------
            home = findViewById(R.id.buttonA1);
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(PlayVideoActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
            //---------------- back ----------------------------------------
            back = findViewById(R.id.buttonA2);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(PlayVideoActivity.this, VideoActivity.class);
                    startActivity(i);
                }
            });


            surface = (SurfaceView) findViewById(R.id.surfView);
            holder = surface.getHolder();
            holder.addCallback(this);
            Intent intent = getIntent();
            String path = intent.getStringExtra("uri");
            File vidFile = new File(path);
            contentUri = Uri.fromFile(vidFile); //qua prendo video da path
        } catch (Exception e) {
            Toast.makeText(PlayVideoActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PlayVideoActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    /** Surface created. */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mediaPlayer= MediaPlayer.create(this,contentUri);
        mediaPlayer.setDisplay(holder);
        mediaPlayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                }
        );
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
    }

    /** surface changed.*/
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    /** surface destroyed .*/
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }
}