package androidx.car.app.samples.es_sistemi_digitali;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class AnalysisActivity extends AppCompatActivity {
    static{
        if(!OpenCVLoader.initDebug())
            Log.d("ERROR","Unable to load OPENCV");
        else
            Log.d("SUCCESS","OpenCV loaded");
    }

    private int imageHeight=200;
    private int imageWidth=200;

    private Button home;
    private Button back;
    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    protected Interpreter tflite;
    /** An array to hold inference results*/
    private float[][] labelProbArray = null;
    /** Labels corresponding to the output of the vision model. */
    private List<String> labelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            /** alternative to OpenCVLoader.initDebug():
             System.loadLibrary("opencv_java4");
             */
            setContentView(R.layout.activity_analysis);
            initializeModel();
            labelList = loadLabelList();
            labelProbArray = new float[1][getNumLabels()];

            //------------------------ home ------------------------
            home = findViewById(R.id.buttonA1);
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AnalysisActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
            //------------------------ back -------------------------
            back = findViewById(R.id.buttonA2);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AnalysisActivity.this, VideoActivity.class);
                    startActivity(i);
                }
            });

            Intent intent = getIntent();
            String video_path = intent.getStringExtra("uri");
            VideoCapture cap = new org.opencv.videoio.VideoCapture();
            //
            cap.set(Videoio.CAP_PROP_FRAME_WIDTH, 200);
            cap.set(Videoio.CAP_PROP_FRAME_HEIGHT, 200);

            cap.open(video_path);
            double fps = cap.get(Videoio.CAP_PROP_FPS);
            System.out.println("fps: " + fps);


            Mat frame = new Mat();
            int i = 0;

                if (cap.isOpened()) {

                String frase = "";
                int frames_per_second = 2;
                int video_length = (int) cap.get(org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT);
                Mat img;
                Bitmap bitmap;
                Bitmap rotatedBitmap;

                Matrix matrix = new Matrix();
                MediaMetadataRetriever m = new MediaMetadataRetriever();
                m.setDataSource(video_path);
                String s = "";
                if (Build.VERSION.SDK_INT >= 17) {
                    s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                }
                int rotazione = Integer.valueOf(s);
                matrix.postRotate(rotazione);

                //----------------------------------------------------------------------------

                while (cap.grab()) {
                    if (i % 6 == 0) { //30= 1 frame per second
                        cap.read(frame);
                        img = preprocessImage(frame, video_length, frames_per_second);
                        if (img.cols() > 0 && img.rows() > 0) {
                            bitmap = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                            /**rotate bitmap:*/
                            Utils.matToBitmap(img, bitmap);
                            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, imageHeight, imageWidth, false);
                            frase += classifyFrame2(rotatedBitmap);
                        }
                    }
                    i++;
                }


                String finale = "";
                int n;
                for (int k = 0; k < frase.length() - 2; k++) {
                    if (frase.charAt(k) == 's') {
                        k += 4;
                        if (k + 1 < frase.length() && k + 6 < frase.length() && frase.charAt(k + 1) == 's' && frase.charAt(k + 6) == 's') {
                            finale += " ";
                            k += 10;
                        }

                        while (k < frase.length() - 5 && frase.charAt(k+1) == 's'  ) k+=5;    //to skip all subsequent spaces

                    } else if (frase.charAt(k) == 'n') {
                        k += 6; //to skip the letters nothing
                    } else if (frase.charAt(k) == 'd') {
                        k += 2;
                        if (k + 1 < frase.length() && k + 4 < frase.length() && frase.charAt(k + 1) == 'd' && frase.charAt(k + 4) == 'd') {
                            if (!finale.isEmpty())
                                finale = finale.substring(0, finale.length() - 1);
                            k += 6;
                        }
                        while (k < frase.length() - 3 && frase.charAt(k+1) == 'd'  ) k+=3;
                    } else if (frase.charAt(k) == frase.charAt(k + 1) && frase.charAt(k) == frase.charAt(k + 2)) {
                        finale += frase.charAt(k);
                        n = k; //actual letters
                        k += 2;
                        while (frase.charAt(n) == frase.charAt(k) && k < frase.length() - 1) k++;
                        k--;
                    }
                }
                TextView v = findViewById(R.id.textView1);
                cap.release();
                v.setText("frase:\n " + finale);

            } else {
                Log.d("VideoCapture", "VideoCapture failed");
                Toast.makeText(this, "VideoCapture failed", Toast.LENGTH_SHORT).show();
            }

            File videofiles = new File(video_path);
            videofiles.delete();

        } catch (Exception e) {
            Toast.makeText(AnalysisActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(AnalysisActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    /** Preprocess Image. */
    private Mat preprocessImage(Mat frame, int video_length, int frames_per_second) {
        return frame;
    }


    /** Reads label list from Assets. */
    private List<String> loadLabelList()  {
        List<String> labelList = new ArrayList<String>();
        String line="";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getAssets().open(getLabelPath())));
            line = reader.readLine();
            reader.close();
        } catch (IOException e) { e.printStackTrace(); }

        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            labelList.add(token);
        }
        return labelList;
    }

    protected String getLabelPath() {
        return "labels.txt";
    }


    /** Returns the total number of labels. */
    protected int getNumLabels() {
        return labelList.size();
    }

    /** Initializing the Model. */
    private void initializeModel() {

        try {
            if (tflite == null) {
                MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "model.tflite");
                tflite = new Interpreter(tfliteModel);
            }
        } catch(Exception e){ e.printStackTrace();  }
    }

    /** Classifies a frame from the preview stream. */
    String classifyFrame2(Bitmap bitmap) {

        if (tflite == null) {
            Log.e("ERRORE MODEL", "Image classifier has not been initialized; Skipped.");

        }

        int imageTensorIndex = 0;
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        TensorImage tfImage = new TensorImage(imageDataType);
        tfImage.load(bitmap);
        tflite.run(tfImage.getBuffer(), labelProbArray);
        String maggiore="";

        float max=-1;
        for (int i =0; i< getNumLabels();i++ )
        {
            if(getProbability(i) > max) {
                maggiore= labelList.get(i);
                max=getProbability(i);
            }
        }

        return maggiore;

    }

    /** Returns the probability of a given label */
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];

    }



}
