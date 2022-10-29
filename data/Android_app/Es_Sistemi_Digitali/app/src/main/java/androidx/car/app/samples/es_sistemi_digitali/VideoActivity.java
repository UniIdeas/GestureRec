package androidx.car.app.samples.es_sistemi_digitali;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Environment;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.util.Date;
import java.util.concurrent.Executor;




public class VideoActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListenableFuture<ProcessCameraProvider> provider;

    private Button analysis_bt;
    private Button mostraVideo;
    private PreviewView pview;
    private ImageCapture imageCapt;
    private VideoCapture videoCapture;
    private ImageButton bRecord;
    private String videoPath; //path of the video
    private boolean cliccato;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_video);
            if (!checkPermission()) requestPermission();
            cliccato = false;
            videoPath = "";
            //----------- show the video ----------------------------------
            mostraVideo = findViewById(R.id.mostra);
            mostraVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(VideoActivity.this, PlayVideoActivity.class);
                    i.putExtra("uri", videoPath);
                    startActivity(i);
                }
            });
            //----------- translation ---------------------------------------
            analysis_bt = findViewById(R.id.analysis_bt);
            analysis_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ImageView imageForToast = new ImageView(VideoActivity.this);
                    imageForToast.setImageResource(R.drawable.clessidra);
                    Toast mioToast = new Toast(VideoActivity.this);
                    mioToast.setGravity(Gravity.CENTER, 0, 0);
                    mioToast.setDuration(Toast.LENGTH_LONG);
                    mioToast.setView(imageForToast);
                    mioToast.show();
                    //
                    Intent newActivity = new Intent(VideoActivity.this, AnalysisActivity.class);
                    newActivity.putExtra("uri", videoPath);
                    startActivity(newActivity);
                }
            });
            //----------- record video ----------------------------------
            pview = findViewById(R.id.previewView);
            bRecord = findViewById(R.id.bRecord);

            bRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                @SuppressLint("RestrictedApi")
                public void onClick(View v) {
                    if (cliccato == false) {
                        cliccato = true;
                        analysis_bt.setVisibility(View.INVISIBLE); //To set invisible
                        mostraVideo.setVisibility(View.INVISIBLE); //To set invisible
                        bRecord.setImageResource(R.drawable.stop);
                        recordVideo();
                    } else {
                        cliccato = false;
                        bRecord.setImageResource(R.drawable.start);
                        videoCapture.stopRecording();
                        analysis_bt.setVisibility(View.VISIBLE); //To set visible
                        mostraVideo.setVisibility(View.VISIBLE); //To set visible
                    }
                }
            });

            provider = ProcessCameraProvider.getInstance(this);
            //
            provider.addListener(() ->
            {
                try {
                    ProcessCameraProvider cameraProvider = provider.get();
                    startCamera(cameraProvider);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, getExecutor());
    } catch (Exception e) {
        Toast.makeText(VideoActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(VideoActivity.this, MainActivity.class);
        startActivity(i);
    }
    }

    /** Permission control. */
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false; //Permission is not granted
        }
        return true; //Permission is  granted
    }

    /** Request for permits. */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    /** Result of the required authorizations. */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /** Video recording.*/
    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {
            File movieDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pedro");
            if (!movieDir.exists())
                movieDir.mkdir();

            Date date = new Date();
            String timestamp = String.valueOf(date.getTime());
            String vidFilePath = movieDir.getAbsolutePath() + "/" + timestamp + ".mp4";
            videoPath = vidFilePath; //PATH DEL VIDEO
            File vidFile = new File(vidFilePath);

            try {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                videoCapture.startRecording(
                        new VideoCapture.OutputFileOptions.Builder(vidFile).build(),
                        getExecutor(),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                Toast.makeText(VideoActivity.this, "Video has been saved successfully.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                Toast.makeText(VideoActivity.this, "Error saving video: " + message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /** Get the Executor */
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    /** Camera initialization. */
    @SuppressLint("RestrictedApi")
    private void startCamera(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector camSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(pview.getSurfaceProvider());

        imageCapt = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setCameraSelector(camSelector)
                .setVideoFrameRate(30)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setMaxResolution(new Size(200,200))
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, camSelector, preview, imageCapt, videoCapture);
    }

}