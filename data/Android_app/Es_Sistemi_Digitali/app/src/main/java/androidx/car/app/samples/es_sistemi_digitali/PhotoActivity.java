package androidx.car.app.samples.es_sistemi_digitali;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.Date;
import java.util.concurrent.Executor;


public class PhotoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListenableFuture<ProcessCameraProvider> provider;

    private Button picture_bt, analysis_bt;
    private PreviewView pview;
    private ImageCapture imageCapt;
    private VideoCapture videoCapture;
    private String photoPath;//path of the photo



    /** onCreate Activity. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        if (!checkPermission())  requestPermission();
        /**-----------------photo processing------------------------**/
        analysis_bt = findViewById(R.id.elaboraFoto);
        analysis_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newActivity = new Intent(PhotoActivity.this, PhotoAnalysisActivity.class);
                newActivity.putExtra("photoPath", photoPath);
                startActivity(newActivity);
            }
        });
        /** To set invisible **/
        analysis_bt.setVisibility(View.INVISIBLE);
        /** take the picture **/
        picture_bt = findViewById(R.id.picture_bt);
        picture_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analysis_bt.setVisibility(View.VISIBLE); //To set visible
                capturePhoto();
            }
        });

        pview = findViewById(R.id.previewView);
        provider = ProcessCameraProvider.getInstance(this);
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
            Toast.makeText(PhotoActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PhotoActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    /** Permission control. */
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;  //Permission is not granted
        }
        return true; //Permission is granted
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


    /** return the Executor. */
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
        /** Video capture use case: */
        videoCapture = new VideoCapture.Builder()
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setCameraSelector(camSelector)
                .setVideoFrameRate(20)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, camSelector, preview, imageCapt, videoCapture);
    }

    /** taking the photo */
    private void capturePhoto() {
        File photoDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pedro");
        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";
        System.out.println("Pathname: " + photoFilePath);
        File photoFile = new File(photoFilePath);
        imageCapt.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(PhotoActivity.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();
                        photoPath=photoFilePath;
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(PhotoActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


}