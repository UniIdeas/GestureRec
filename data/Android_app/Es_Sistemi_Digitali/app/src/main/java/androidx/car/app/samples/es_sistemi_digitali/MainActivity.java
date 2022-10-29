package androidx.car.app.samples.es_sistemi_digitali;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button goPhoto, goVideo,goAudio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //*************** CLICK HERE TO START PHOTO PROCESSING: *****************************

        goPhoto = findViewById(R.id.goPhoto);
        goPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PhotoActivity.class);
                startActivity(i);
            }
        });

        //*************** CLICK HERE TO START VIDEO PROCESSING: ***********************

        goVideo = findViewById(R.id.goVideo);
        goVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(i);
            }
        });
        //*************** CLICK HERE TO START AUDIO PROCESSING: *****************************

            goAudio = findViewById(R.id.goAudio);
            goAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, VoiceActivity.class);
                    startActivity(i);
                }
            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

}