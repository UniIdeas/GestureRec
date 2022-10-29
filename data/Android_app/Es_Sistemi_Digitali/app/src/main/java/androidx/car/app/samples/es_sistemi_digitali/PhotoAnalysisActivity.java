package androidx.car.app.samples.es_sistemi_digitali;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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


public class PhotoAnalysisActivity extends AppCompatActivity {
    private int imageHeight=200;
    private int imageWidth=200;
    private Button home;
    private Button back;
    TextView result2;
    ImageView imageView;

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    protected Interpreter tflite;
  /** An array to hold inference results, to be feed into Tensorflow Lite as outputs.*/
    private float[][] labelProbArray = null;
    /** Labels corresponding to the output of the vision model. */
    private List<String> labelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tflite);
        Intent intent=getIntent();
        String photoPath=intent.getStringExtra("photoPath");
        result2=findViewById(R.id.result);
        //
            //---------------- home -------------------------------------
            home = findViewById(R.id.buttonF1);
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(PhotoAnalysisActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
            //---------------- back -------------------------------------
            back = findViewById(R.id.buttonF2);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(PhotoAnalysisActivity.this, PhotoActivity.class);
                    startActivity(i);
                }
            });


        /** BITMAP: */
        Bitmap image = BitmapFactory.decodeFile(photoPath);
        int rotateImage = getCameraPhotoOrientation( photoPath);
        image = rotateBitmap(image, rotateImage);
        imageView = findViewById(R.id.imageView2);
        imageView.setImageBitmap(image);
        image = Bitmap.createScaledBitmap(image, imageHeight, imageWidth, false);

        initializeModel();
        labelList = loadLabelList();
        labelProbArray = new float[1][getNumLabels()];

        classifyFrame2(image);
        /**delete the photo*/
        File photofiles = new File(photoPath);
        photofiles.delete();

        } catch (Exception e) {
            Toast.makeText(PhotoAnalysisActivity.this, "Qualcosa è andato storto, riprova...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(PhotoAnalysisActivity.this, MainActivity.class);
            startActivity(i);
        }
}

    /** Reads label list from Assets. */
    private List<String> loadLabelList()  {
        List<String> labelList = new ArrayList<String>();
        String line="";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getAssets().open(getLabelPath())));
            line = reader.readLine();
            reader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            labelList.add(token);


        }
        return labelList;
    }

    /**Get the path of the label.*/
    protected String getLabelPath() {
        return "labels.txt";
    }


    /** Get the total number of labels. */
    protected int getNumLabels() {
        return labelList.size();
    }

    /** initialize the Model. */
    private void initializeModel() {

        try {
            if (tflite == null) {

                MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "model.tflite");
                tflite = new Interpreter(tfliteModel);

            }


        } catch(Exception e){
            e.printStackTrace();

        }
    }



    /** Classifies a frame from the preview stream. */
    void classifyFrame2(Bitmap bitmap) {

        if (tflite == null) {
            Log.e("ERRORE MODEL", "Image classifier has not been initialized; Skipped.");

        }
        int imageTensorIndex = 0;
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        TensorImage tfImage = new TensorImage(imageDataType);
        tfImage.load(bitmap);
        //
        // RUN:
        tflite.run(tfImage.getBuffer(), labelProbArray);
        //
        String maggiore="";
        String secondo="";
        String terzo="";

        float max=-1;
        float maxSecondo=-1;
        float maxTerzo=-1;
        for (int i =0; i< getNumLabels();i++ )
        {

            if(getProbability(i) > max) {
              maggiore=  String.format("%s: %.1f%%\n", labelList.get(i) ,getProbability(i)*100);
                max=getProbability(i);
            }
            // second:
            if(getProbability(i) > maxSecondo && getProbability(i)!=max) {
                secondo=  String.format("%s: %.1f%%\n", labelList.get(i) ,getProbability(i)*100);
                maxSecondo=getProbability(i);
            }
            //third:
            if(getProbability(i) > maxTerzo && getProbability(i)!=max && getProbability(i)!=maxSecondo) {
                terzo=  String.format("%s: %.1f%%\n", labelList.get(i) ,getProbability(i)*100);
                maxTerzo=getProbability(i);
            }

        }
        result2.setText(maggiore+"\n"+secondo+"\n"+terzo);


    }

    /**Get the probability.*/
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    /** Rotate the bitmap to the correct position.*/
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                                  source.getHeight(), matrix, true);}

    /** Get the orientation of the camera photo. */
    public int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION
                                                 ,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270; break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90; break;
            }
        } catch (Exception e) {  e.printStackTrace(); }
        return rotate; }


}