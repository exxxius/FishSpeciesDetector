//////package com.rebirth.FishSpeciesDetector;
//////
//////import android.Manifest;
//////import android.app.AlertDialog;
//////import android.content.ContentValues;
//////import android.content.DialogInterface;
//////import android.content.pm.PackageManager;
//////import android.graphics.Bitmap;
//////import android.net.Uri;
//////import android.os.Bundle;
//////import android.os.Environment;
//////import android.provider.MediaStore;
//////
//////import androidx.appcompat.app.AppCompatActivity;
//////import androidx.core.app.ActivityCompat;
//////import androidx.core.content.ContextCompat;
//////
//////import java.io.IOException;
//////import java.io.OutputStream;
//////
//////import android.content.Intent;
//////import android.util.Log;
//////import android.view.View;
//////import android.widget.Button;
//////import android.widget.ImageView;
//////
//////import android.graphics.Canvas;
//////import android.graphics.Color;
//////import android.graphics.Paint;
//////import android.graphics.RectF;
//////import android.graphics.drawable.BitmapDrawable;
//////import android.widget.Toast;
//////
//////import org.tensorflow.lite.DataType;
//////import org.tensorflow.lite.Interpreter;
//////import org.tensorflow.lite.support.common.FileUtil;
//////import org.tensorflow.lite.support.common.TensorOperator;
//////import org.tensorflow.lite.support.common.ops.NormalizeOp;
//////import org.tensorflow.lite.support.image.ImageProcessor;
//////import org.tensorflow.lite.support.image.TensorImage;
//////import org.tensorflow.lite.support.image.ops.ResizeOp;
//////import org.tensorflow.lite.support.label.Category;
//////import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//////
//////import java.io.IOException;
//////import java.nio.ByteBuffer;
//////import java.util.ArrayList;
//////import java.util.List;
//////
//////
//////public class MainActivity extends AppCompatActivity {
//////    private Button btnCapture;
//////    private ImageView imageView;
//////    private static final int REQUEST_CAPTURE_IMAGE = 1;
//////    private static final int REQUEST_PICK_IMAGE = 2;
//////    private static final int PERMISSIONS_REQUEST = 1;
//////    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
//////    private static final int INPUT_SIZE = 640;
//////
//////    // request permission for camera and storage access
//////    private void requestPermissions() {
//////        ActivityCompat.requestPermissions(
//////                this,
//////                new String[]{
//////                        Manifest.permission.CAMERA,
//////                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//////                        Manifest.permission.READ_EXTERNAL_STORAGE
//////                },
//////                1
//////        );
//////    }
//////    // Listener for the image picker dialog
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        setContentView(R.layout.activity_main);
//////        if (hasPermission()) {
//////            setup();
//////        } else {
//////            requestPermission();
//////        }
//////        btnCapture = findViewById(R.id.btnCapture);
//////        imageView = findViewById(R.id.imageView);
//////
//////        btnCapture.setOnClickListener(new View.OnClickListener() {
//////            @Override
//////            public void onClick(View view) {
//////                // Show options to capture or select an image
//////                showImageOptions();
//////            }
//////        });
//////    }
//////    private boolean hasPermission() {
//////        return ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
//////    }
//////
//////    private void requestPermission() {
//////        ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
//////    }
//////
//////    @Override
//////    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//////        if (requestCode == PERMISSIONS_REQUEST) {
//////            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//////                setup();
//////            } else {
//////                Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_LONG).show();
//////            }
//////        } else {
//////            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//////        }
//////    }
//////
//////    private void setup() {
//////        detector = new Detector(this, new Size(INPUT_SIZE, INPUT_SIZE));
//////    }
//////
//////    // Method to display a dialog with options to capture or select an image
//////    private void showImageOptions() {
//////        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
//////        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//////        builder.setTitle("Capture or Select Image");
//////        builder.setItems(options, new DialogInterface.OnClickListener() {
//////            @Override
//////            public void onClick(DialogInterface dialog, int item) {
//////                if (options[item].equals("Take Photo")) {
//////                    captureImage();
//////                } else if (options[item].equals("Choose from Gallery")) {
//////                    selectImageFromGallery();
//////                } else if (options[item].equals("Cancel")) {
//////                    dialog.dismiss();
//////                }
//////            }
//////        });
//////        builder.show();
//////    }
//////
//////    // Method to capture an image
//////    private void captureImage() {
//////        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//////        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//////            startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE);
//////        }
//////    }
//////
//////    // Method to handle the captured image or selected image from gallery
//////    private void selectImageFromGallery() {
//////        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//////        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
//////    }
//////
//////
//////
//////    // Method to save the captured image or selected image from gallery
//////    private void saveImageToGallery(Bitmap bitmap) {
//////        ContentValues contentValues = new ContentValues();
//////        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image.jpg");
//////        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//////        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//////
//////        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//////
//////        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
//////            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//////            outputStream.flush();
//////        } catch (IOException e) {
//////            e.printStackTrace();
//////        }
//////    }
//////
//////    // Method to handle the result from the image picker dialog
//////    @Override
//////    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//////        super.onActivityResult(requestCode, resultCode, data);
//////        if (resultCode == RESULT_OK) {
//////            if (requestCode == REQUEST_CAPTURE_IMAGE) {
//////                Bundle extras = data.getExtras();
//////                Bitmap imageBitmap = (Bitmap) extras.get("data");
//////                imageView.setImageBitmap(imageBitmap);
//////                processImage(imageBitmap);
//////            } else if (requestCode == REQUEST_PICK_IMAGE) {
//////                Uri imageUri = data.getData();
//////                imageView.setImageURI(imageUri);
//////                try {
//////                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//////                    processImage(imageBitmap);
//////                } catch (IOException e) {
//////                    e.printStackTrace();
//////                }
//////            }
//////        }
//////    }
//////
////////    private void processImage(Bitmap bitmap) {
////////        // Resize the bitmap maintaining the aspect ratio
////////        float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
////////        int targetWidth = INPUT_SIZE;
////////        int targetHeight = Math.round(targetWidth / aspectRatio);
////////
////////        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
////////
////////        // Preprocess the resized bitmap to be used with the TFLite model
////////        ByteBuffer input = preprocessBitmap(resizedBitmap);
////////
////////        // Run the TFLite model with the preprocessed input
////////        runModel(input);
////////
////////        // Post-process the output from the TFLite model and obtain the bounding boxes
////////        List<Recognition> recognitions = postProcessModelOutput();
////////
////////        // Create a mutable copy of the original bitmap to draw bounding boxes on it
////////        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
////////        Canvas canvas = new Canvas(mutableBitmap);
////////        Paint paint = new Paint();
////////        paint.setColor(Color.RED);
////////        paint.setStyle(Paint.Style.STROKE);
////////        paint.setStrokeWidth(4.0f);
////////
////////        // Draw the bounding boxes on the mutableBitmap
////////        for (Recognition recognition : recognitions) {
////////            RectF box = recognition.getLocation();
////////            float xScale = (float) bitmap.getWidth() / targetWidth;
////////            float yScale = (float) bitmap.getHeight() / targetHeight;
////////
////////            // Scale the bounding box to match the dimensions of the original image
////////            RectF scaledBox = new RectF(
////////                    box.left * xScale,
////////                    box.top * yScale,
////////                    box.right * xScale,
////////                    box.bottom * yScale
////////            );
////////            canvas.drawRect(scaledBox, paint);
////////        }
////////
////////        // Display the mutableBitmap with the bounding boxes
////////        ImageView imageView = findViewById(R.id.imageView);
////////        imageView.setImageBitmap(mutableBitmap);
////////    }
//////
//////
////////    // Method to get the normalization operator to preprocess the image
////////    private void processImage(Bitmap imageBitmap) {
////////        // Load the TFLite model
////////        Interpreter tflite = null;
////////        try {
////////            tflite = new Interpreter(FileUtil.loadMappedFile(this, "best-fp16.tflite"));
////////        } catch (IOException e) {
////////            e.printStackTrace();
////////        }
////////
////////        // Preprocess the image
////////        ImageProcessor imageProcessor = new ImageProcessor.Builder()
////////                .add(new ResizeOp(640, 640, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
////////                .add(getPreprocessNormalizeOp())
////////                .build();
////////
////////        TensorImage inputImage = new TensorImage(DataType.FLOAT32);
////////        inputImage.load(imageBitmap);
////////        inputImage = imageProcessor.process(inputImage);
////////
////////        // Perform inference
////////        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 25200, 85}, DataType.FLOAT32);
////////        tflite.run(inputImage.getBuffer(), outputBuffer.getBuffer());
////////
////////        // Postprocess the results
////////        ByteBuffer output = outputBuffer.getBuffer();
////////        List<DetectedObject> detectedObjects = postProcessOutput(output);
////////
////////        // Draw bounding boxes on the image
////////        Bitmap resultBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
////////        Canvas canvas = new Canvas(resultBitmap);
////////        Paint paint = new Paint();
////////        paint.setStyle(Paint.Style.STROKE);
////////        paint.setStrokeWidth(5);
////////
////////        for (DetectedObject obj : detectedObjects) {
////////            paint.setColor(Color.RED);
////////            canvas.drawRect(obj.bbox, paint);
////////            paint.setColor(Color.WHITE);
////////            paint.setTextSize(40);
////////            canvas.drawText(obj.label + " " + String.format("%.2f", obj.confidence), obj.bbox.left, obj.bbox.top, paint);
////////        }
////////        // Display the processed image
////////        imageView.setImageBitmap(resultBitmap);
////////    }
//////
//////    private void processImage(Bitmap bitmap) {
//////        if (detector == null) {
//////            Log.e(TAG, "Detector not initialized.");
//////            return;
//////        }
//////        // Resize the bitmap maintaining the aspect ratio
//////        float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
//////        int targetWidth = INPUT_SIZE;
//////        int targetHeight = Math.round(targetWidth / aspectRatio);
//////
//////        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
//////
//////        // Preprocess the resized bitmap to be used with the TFLite model
//////        ByteBuffer input = detector.preprocessBitmap(resizedBitmap);
//////
//////        // Run the TFLite model with the preprocessed input
//////        detector.runModel(input);
//////
//////        // Post-process the output from the TFLite model and obtain the bounding boxes
//////        List<Recognition> recognitions = detector.postProcessModelOutput();
//////
//////        // Create a mutable copy of the original bitmap to draw bounding boxes on it
//////        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//////        Canvas canvas = new Canvas(mutableBitmap);
//////        Paint paint = new Paint();
//////        paint.setColor(Color.RED);
//////        paint.setStyle(Paint.Style.STROKE);
//////        paint.setStrokeWidth(4.0f);
//////        // Draw bounding boxes on the mutable bitmap
//////        for (Recognition recognition : recognitions) {
//////            RectF location = recognition.getLocation();
//////            canvas.drawRect(location, paint);
//////        }
//////
//////        // Update the ImageView with the new bitmap
//////        imageView.setImageBitmap(mutableBitmap);
//////
//////        // Set up an OnClickListener for the ImageView to dismiss the result
//////        imageView.setOnClickListener(new View.OnClickListener() {
//////            @Override
//////            public void onClick(View v) {
//////                imageView.setImageBitmap(null);
//////                imageView.setOnClickListener(null);
//////            }
//////        });
//////    }
//////
//////
//////        // Method to get the normalization operator to preprocess the image
//////    private TensorOperator getPreprocessNormalizeOp() {
//////        return new NormalizeOp(0.0f, 255.0f);
//////    }
//////
//////    // Method to postprocess the output
//////    private List<DetectedObject> postProcessOutput(ByteBuffer output) {
//////        ArrayList<DetectedObject> detectedObjects = new ArrayList<>();
//////        List<String> labels = null;
//////
//////        try {
//////            labels = FileUtil.loadLabels(this, "labels.txt");
//////        } catch (IOException e) {
//////            e.printStackTrace();
//////        }
//////
//////        // Iterate through the output and extract detected objects
//////        for (int i = 0; i < output.capacity() / 85; i++) {
//////            float confidence = output.getFloat(i * 85 + 4);
//////            if (confidence > 0.5) {
//////                int classId = -1;
//////                float maxProb = 0;
//////
//////                for (int j = 0; j < 6; j++) {
//////                    float prob = output.getFloat(i * 85 + 5 + j);
//////                    if (prob > maxProb) {
//////                        maxProb = prob;
//////                        classId = j;
//////                    }
//////                }
//////
//////                if (classId >= 0) {
//////                    float x = output.getFloat(i * 85);
//////                    float y = output.getFloat(i * 85 + 1);
//////                    float width = output.getFloat(i * 85 + 2);
//////                    float height = output.getFloat(i * 85 + 3);
//////                    RectF bbox = new RectF(
//////                            x - width / 2,
//////                            y - height / 2,
//////                            x + width / 2,
//////                            y + height / 2
//////                    );
//////
//////                    DetectedObject obj = new DetectedObject(labels.get(classId), confidence, bbox);
//////                    detectedObjects.add(obj);
//////                }
//////            }
//////        }
//////
//////        return detectedObjects;
//////    }
//////
//////    // Class to hold the detected objects
//////    private static class DetectedObject {
//////        String label;
//////        float confidence;
//////        RectF bbox;
//////
//////        // Constructor
//////        DetectedObject(String label, float confidence, RectF bbox) {
//////            this.label = label;
//////            this.confidence = confidence;
//////            this.bbox = bbox;
//////        }
//////    }
//////
//////}
////
////package com.rebirth.FishSpeciesDetector;
////
////import static android.content.ContentValues.TAG;
////
////import android.Manifest;
////import android.content.Intent;
////import android.content.pm.PackageManager;
////import android.graphics.Bitmap;
////import android.graphics.BitmapFactory;
////import android.graphics.Matrix;
////import android.graphics.RectF;
////import android.net.Uri;
////import android.os.Bundle;
////import android.provider.MediaStore;
////import android.util.Log;
////import android.util.Size;
////import android.view.View;
////import android.widget.Button;
////import android.widget.ImageView;
////import android.widget.TextView;
////import android.widget.Toast;
////
////import androidx.annotation.NonNull;
////import androidx.annotation.Nullable;
////import androidx.appcompat.app.AppCompatActivity;
////import androidx.core.app.ActivityCompat;
////import androidx.core.content.ContextCompat;
////import androidx.exifinterface.media.ExifInterface;
////
////import java.io.IOException;
////import java.io.InputStream;
////import java.util.List;
////import com.rebirth.FishSpeciesDetector.Recognition;
////
////public class MainActivity extends AppCompatActivity {
////    private static final String MODEL_PATH = "best-fp16.tflite";
////    private static final String LABELS_PATH = "labels.txt";
////
////    private static final int INPUT_SIZE = 640;
////
////    private static final int REQUEST_IMAGE_CAPTURE = 1;
////    private static final int REQUEST_PICK_IMAGE = 2;
////    private static final int REQUEST_PERMISSIONS = 3;
////
////    private ImageView imageView;
////    private TextView textView;
////    private Button newPredictionButton;
////
////    //private Detector detector;
////
////    ///////////////////////YoloV5Classifier detector ///////////////////////
////    private YoloV5Classifier detector;
////    /////////////////////////
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
////
////        imageView = findViewById(R.id.imageView);
////        textView = findViewById(R.id.textView);
////        newPredictionButton = findViewById(R.id.newPredictionButton);
////
////        String yoloModelPath = "best-fp16.tflite";
////        String labelsPath = "labels.txt";
////        boolean isQuantized = false; // Set to true if you are using a quantized model
////        int inputSize = 640; // Change this to match the input size of your YOLOv5 model
////
////        try {
////            detector = YoloV5Classifier.create(getAssets(), yoloModelPath, labelsPath, isQuantized, inputSize);
////        } catch (IOException e) {
////            Log.e(TAG, "Error initializing YoloV5Classifier", e);
////        }
////
////
////        findViewById(R.id.captureButton).setOnClickListener(v -> {
////            if (checkPermissions()) {
////                captureImage();
////            } else {
////                requestPermissions();
////            }
////        });
////
////        findViewById(R.id.selectButton).setOnClickListener(v -> {
////            if (checkPermissions()) {
////                selectImage();
////            } else {
////                requestPermissions();
////            }
////        });
////
////        newPredictionButton.setOnClickListener(v -> {
////            textView.setVisibility(View.GONE);
////            newPredictionButton.setVisibility(View.GONE);
////            imageView.setVisibility(View.GONE);
////        });
////    }
////
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////
////        if (resultCode == RESULT_OK) {
////            switch (requestCode) {
////                case REQUEST_IMAGE_CAPTURE:
////                    Bundle extras = data.getExtras();
////                    Bitmap imageBitmap = (Bitmap) extras.get("data");
////                    displayImage(imageBitmap);
////                    break;
////                case REQUEST_PICK_IMAGE:
////                    if (data != null && data.getData() != null) {
////                        Uri imageUri = data.getData();
////                        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
////                            Bitmap imageBitmapGallery = BitmapFactory.decodeStream(inputStream);
////                            imageBitmapGallery = handleImageRotation(imageUri, imageBitmapGallery);
////                            displayImage(imageBitmapGallery);
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                    break;
////            }
////        }
////    }
////
////    @Override
////    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
////
////        if (requestCode == REQUEST_PERMISSIONS) {
////            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
////            } else {
////                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show();
////            }
////        }
////    }
////
////    private void captureImage() {
////        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
////            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
////        }
////    }
////
////    private void selectImage() {
////        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////        startActivityForResult(pickImageIntent, REQUEST_PICK_IMAGE);
////    }
////
////    private void displayImage(Bitmap imageBitmap) {
////        Bitmap inputBitmap = Bitmap.createScaledBitmap(imageBitmap, 640, 640, true);
////        imageView.setImageBitmap(inputBitmap);
////        imageView.setVisibility(View.VISIBLE);
////        List<Recognition> predictions = detector.recognizeImage(inputBitmap);
////        StringBuilder resultText = new StringBuilder("A.I recognized this species as:\n");
////        for (int i = 0; i < predictions.size(); i++) {
////            Recognition prediction = predictions.get(i);
////            resultText.append("Fish ").append(i + 1).append(": ")
////                    .append(prediction.getTitle()).append(" ")
////                    .append(String.format("%.2f%%", prediction.getConfidence() * 100)).append("\n");
////        }
////        textView.setText(resultText.toString());
////        textView.setVisibility(View.VISIBLE);
////
////        newPredictionButton.setVisibility(View.VISIBLE);
////    }
////
////    private boolean checkPermissions() {
////        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
////                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
////    }
////
////    private void requestPermissions() {
////        ActivityCompat.requestPermissions(this, new String[]{
////                Manifest.permission.READ_EXTERNAL_STORAGE,
////                Manifest.permission.WRITE_EXTERNAL_STORAGE
////        }, REQUEST_PERMISSIONS);
////    }
////
////    private Bitmap handleImageRotation(Uri imageUri, Bitmap bitmap) throws IOException {
////        ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(imageUri));
////        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
////
////        Matrix matrix = new Matrix();
////        switch (orientation) {
////            case ExifInterface.ORIENTATION_ROTATE_90:
////                matrix.setRotate(90);
////                break;
////            case ExifInterface.ORIENTATION_ROTATE_180:
////                matrix.setRotate(180);
////                break;
////            case ExifInterface.ORIENTATION_ROTATE_270:
////                matrix.setRotate(270);
////                break;
////            default:
////                return bitmap;
////        }
////        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
////    }
////}
//
//package com.rebirth.FishSpeciesDetector;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.rebirth.FishSpeciesDetector.Classifier.Recognition;
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Environment;
//import android.provider.MediaStore;
//import androidx.core.content.FileProvider;
//import androidx.exifinterface.media.ExifInterface;
//
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//import android.content.res.Resources;
//
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final String TAG = "MainActivity";
//    private static final int PERMISSIONS_REQUEST = 1;
//    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
//
//    private Classifier detector;
//    //private ZoomableImageView imageView;
//    //private StretchableImageView imageView;
//    private CustomImageView imageView;
//    private TextView textView;
//    private boolean isFullScreen = false;
//
//    //for resizing the bounding boxes according to original image aspect ratio
//    private float scaleFactorX;
//    private float scaleFactorY;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        imageView = (CustomImageView) findViewById(R.id.zoomableImageView);
//        textView = findViewById(R.id.textView);
//
//        if (hasPermission()) {
//            initializeDetector();
//        } else {
//            requestPermission();
//        }
//        Button captureImageBtn = findViewById(R.id.captureButton);
//        captureImageBtn.setOnClickListener(view -> {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                } catch (IOException ex) {
//                    Log.e(TAG, "Error occurred while creating the File", ex);
//                }
//                if (photoFile != null) {
//                    Uri photoURI = FileProvider.getUriForFile(this,
//                            "com.rebirth.FishSpeciesDetector.fileprovider",
//                            photoFile);
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
//                }
//            }
//        });
//
//        Button selectImageBtn = findViewById(R.id.selectButton);
//        selectImageBtn.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE);
//        });
//        Button showResultButton = findViewById(R.id.showResultButton);
//        showResultButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                StringBuilder sb = new StringBuilder();
//                for (Classifier.Recognition result : results) {
//                    sb.append(result.getTitle())
//                            .append(" - ")
//                            .append(String.format("%.2f", result.getConfidence() * 100))
//                            .append("%")
//                            .append("\n");
//                }
//                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
//            }
//        });
//
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleFullScreen();
//            }
//        });
//    }
//
//    private void toggleFullScreen() {
//        if (isFullScreen) {
//            showSystemUI();
//        } else {
//            hideSystemUI();
//        }
//        isFullScreen = !isFullScreen;
//    }
//
//    private void hideSystemUI() {
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
//    }
//
//    private void showSystemUI() {
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }
//
//    private File createImageFile() throws IOException {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
//        return image;
//    }
//
//
//    private void initializeDetector() {
//        try {
//            String modelFilename = "best-fp16.tflite"; // Choose the appropriate model file
//            detector = DetectorFactory.getDetector(getAssets(), modelFilename);
//
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to initialize detector.", e);
//        }
//    }
//
//    private boolean hasPermission() {
//        return ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSIONS_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            initializeDetector();
//        } else {
//            requestPermission();
//        }
//    }
//
//    // Add other methods to handle camera and image processing, and use the `detector` to recognize objects in the images.
//    private List<Classifier.Recognition> results;
//
//
//    private void processImage(Bitmap bitmap, Bitmap originalBitmap) {
//        if (detector == null) {
//            Toast.makeText(this, "Detector not initialized", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Process the image using the detector
//        results = detector.recognizeImage(bitmap);
//
//        runOnUiThread(() -> {
//            // Calculate the scale factors
//            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
//            Bitmap resizedBitmapForDisplay = resizeMaintainAspect(originalBitmap, screenWidth);
//            scaleFactorX = (float) resizedBitmapForDisplay.getWidth() / bitmap.getWidth();
//            scaleFactorY = (float) resizedBitmapForDisplay.getHeight() / bitmap.getHeight();
//
//            // Draw bounding boxes on the bitmap
//            Bitmap bitmapWithBoundingBoxes = drawBoundingBoxesOnBitmap(resizedBitmapForDisplay, results);
//
//            // Update the image view with the bitmap with bounding boxes
//            imageView.setImageBitmap(bitmapWithBoundingBoxes);
//        });
//    }
//
//
////    private void processImage(Bitmap bitmap) {
////        if (detector == null) {
////            Toast.makeText(this, "Detector not initialized", Toast.LENGTH_SHORT).show();
////            return;
////        }
////
////        // Process the image using the detector
////        results = detector.recognizeImage(bitmap);
////
////        runOnUiThread(() -> {
////
////        // Display the processed image with bounding boxes and labels
////        Bitmap resultBitmap = drawBoundingBoxesOnBitmap(bitmap, results);
////        imageView.setImageBitmap(resultBitmap);
////
////        // Display the labels and confidence scores in text form
////        StringBuilder sb = new StringBuilder();
////        for (Classifier.Recognition result : results) {
////            sb.append(result.getTitle())
////                    .append(" - ")
////                    .append(String.format("%.2f", result.getConfidence() * 100))
////                    .append("%")
////                    .append("\n");
////        }
////        textView.setText(sb.toString());
////
////        });
////
////    }
//
//    private Bitmap drawBoundingBoxesOnBitmap(Bitmap originalBitmap, List<Classifier.Recognition> recognitions) {
//        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
//        android.graphics.Paint paint = new android.graphics.Paint();
//        paint.setStyle(android.graphics.Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        paint.setColor(android.graphics.Color.RED);
//
//        // Add a new Paint object for drawing text
//        android.graphics.Paint textPaint = new android.graphics.Paint();
//        textPaint.setColor(android.graphics.Color.RED);
//        textPaint.setTextSize(40f);
//
//        for (Classifier.Recognition result : recognitions) {
//            android.graphics.RectF location = result.getLocation();
//            location.left *= scaleFactorX;
//            location.top *= scaleFactorY;
//            location.right *= scaleFactorX;
//            location.bottom *= scaleFactorY;
//
//            canvas.drawRect(location, paint);
//
//            // Draw the label and confidence score
//            String label = result.getTitle();
//            float confidence = result.getConfidence();
//            canvas.drawText(label + " " + String.format("%.2f", confidence), location.left, location.top, textPaint);
//        }
//        return bitmap;
//    }
//
//    // Draw bounding boxes and labels on the image.
////    private Bitmap drawBoundingBoxesOnBitmap(Bitmap originalBitmap, List<Classifier.Recognition> recognitions) {
////        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
////        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
////        android.graphics.Paint paint = new android.graphics.Paint();
////        paint.setStyle(android.graphics.Paint.Style.STROKE);
////        paint.setStrokeWidth(5);
////        paint.setColor(android.graphics.Color.RED);
////
////        // Add a new Paint object for drawing text
////        android.graphics.Paint textPaint = new android.graphics.Paint();
////        textPaint.setColor(android.graphics.Color.RED);
////        textPaint.setTextSize(40f);
////
////        for (Classifier.Recognition result : recognitions) {
////            android.graphics.RectF location = result.getLocation();
////
////            // Adjust the coordinates of the bounding box
////            location.left *= scaleFactorX;
////            location.top *= scaleFactorY;
////            location.right *= scaleFactorX;
////            location.bottom *= scaleFactorY;
////
////            canvas.drawRect(location, paint);
////
////            // Draw the label and confidence score
////            String label = result.getTitle();
////            float confidence = result.getConfidence();
////            canvas.drawText(label + " " + String.format("%.2f", confidence), location.left, location.top, textPaint);
////        }
////        return bitmap;
////    }
//
//    // Draw bounding boxes and labels on the image.
////    private Bitmap drawBoundingBoxesOnBitmap(Bitmap originalBitmap, List<Classifier.Recognition> recognitions) {
////        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
////        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
////        android.graphics.Paint paint = new android.graphics.Paint();
////        paint.setStyle(android.graphics.Paint.Style.STROKE);
////        paint.setStrokeWidth(5);
////        paint.setColor(android.graphics.Color.RED);
////
////        // Add a new Paint object for drawing text
////        android.graphics.Paint textPaint = new android.graphics.Paint();
////        textPaint.setColor(android.graphics.Color.RED);
////        textPaint.setTextSize(40f);
////
////        for (Classifier.Recognition result : recognitions) {
////            android.graphics.RectF location = result.getLocation();
////            canvas.drawRect(location, paint);
////
////            // Draw the label and confidence score
////            String label = result.getTitle();
////            float confidence = result.getConfidence();
////            canvas.drawText(label + " " + String.format("%.2f", confidence), location.left, location.top, textPaint);
////        }
////        return bitmap;
////    }
//
//
//    private static final int SELECT_PHOTO_REQUEST_CODE = 1;
//    private static final int TAKE_PHOTO_REQUEST_CODE = 2;
//    private File photoFile;
//
//    // Resize the image.
//    private Bitmap resizeImage(Bitmap originalImage, int width, int height) {
//        Bitmap resizedImage = Bitmap.createScaledBitmap(originalImage, width, height, false);
//        return resizedImage;
//    }
//
//    // Resize the image to fit the screen size and maintain the aspect ratio.
//    private Bitmap resizeMaintainAspect(Bitmap originalImage, int targetWidth){
//        int originalWidth = originalImage.getWidth();
//        int originalHeight = originalImage.getHeight();
//
//        float aspectRatio = (float)originalHeight/(float)originalWidth;
//
//        int targetHeight = (int) (targetWidth*aspectRatio);
//
//        Bitmap result = Bitmap.createScaledBitmap(originalImage, targetWidth, targetHeight, false);
//        return result;
//    }
//
//
////    private Bitmap resizeMaintainAspectRatio(Bitmap originalImage, int targetWidth, int targetHeight) {
////        int originalWidth = originalImage.getWidth();
////        int originalHeight = originalImage.getHeight();
////
////        float widthRatio = (float) originalWidth / targetWidth;
////        float heightRatio = (float) originalHeight / targetHeight;
////
////        float resizeRatio = Math.min(widthRatio, heightRatio);
////
////        int newWidth = Math.round(originalWidth / resizeRatio);
////        int newHeight = Math.round(originalHeight / resizeRatio);
////
////        return Bitmap.createScaledBitmap(originalImage, newWidth, newHeight, false);
////    }
//
//
//    private Bitmap correctOrientation(Bitmap bitmap, Uri uri) {
//        try {
//            ExifInterface exif = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                exif = new ExifInterface(getContentResolver().openInputStream(uri));
//            } else {
//                exif = new ExifInterface(uri.getPath());
//            }
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//            Matrix matrix = new Matrix();
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    matrix.postRotate(90);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    matrix.postRotate(180);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    matrix.postRotate(270);
//                    break;
//                default:
//                    return bitmap;
//            }
//            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bitmap;
//    }
//
//
//    private class ProcessImageTask implements Runnable {
//        private final Bitmap bitmap;
//        private final Bitmap originalBitmap;
//
//        public ProcessImageTask(Bitmap bitmap, Bitmap originalBitmap) {
//            this.bitmap = bitmap;
//            this.originalBitmap = originalBitmap;
//        }
//
//        @Override
//        public void run() {
//            processImage(bitmap, originalBitmap);
//        }
//    }
//
//
//    //    private class ProcessImageTask implements Runnable {
////        private final Bitmap bitmap;
////
////        public ProcessImageTask(Bitmap bitmap) {
////            this.bitmap = bitmap;
////        }
////
////        @Override
////        public void run() {
////            processImage(bitmap);
////        }
////    }
//    private Executor executor = Executors.newSingleThreadExecutor();
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            Bitmap bitmap = null;
//            if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
//                if (data != null && data.getData() != null) {
//                    Uri uri = data.getData();
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                        bitmap = correctOrientation(bitmap, uri);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
//                if (photoFile != null) {
//                    Uri photoURI = Uri.fromFile(photoFile);
//                    bitmap = BitmapFactory.decodeFile(photoURI.getPath());
//                    bitmap = correctOrientation(bitmap, photoURI);
//                }
//            }
//            if (bitmap != null) {
//                int inputSize = 640; // This is the size for the model
//                Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
//                executor.execute(new ProcessImageTask(resizedBitmapForModel, bitmap));
//            }
//        }
//    }
//
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == RESULT_OK) {
////            if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
////                if (data != null && data.getData() != null) {
////                    Uri uri = data.getData();
////                    try {
////                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
////                        bitmap = correctOrientation(bitmap, uri);
////                        int inputSize = 640; // This is the size for the model
////                        Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
////                        executor.execute(new ProcessImageTask(resizedBitmapForModel));
////
////                        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
////                        Bitmap resizedBitmapForDisplay = resizeMaintainAspectRatio(bitmap, screenWidth, screenWidth);
////
////                        // Calculate the scale factors
////                        scaleFactorX = (float)resizedBitmapForDisplay.getWidth() / bitmap.getWidth();
////                        scaleFactorY = (float)resizedBitmapForDisplay.getHeight() / bitmap.getHeight();
////
////                        imageView.setImageBitmap(resizedBitmapForDisplay);
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
////                if (photoFile != null) {
////                    Uri photoURI = Uri.fromFile(photoFile);
////                    Bitmap bitmap = BitmapFactory.decodeFile(photoURI.getPath());
////                    bitmap = correctOrientation(bitmap, photoURI);
////                    int inputSize = 640; // This is the size for the model
////                    Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
////                    executor.execute(new ProcessImageTask(resizedBitmapForModel));
////
////                    int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
////                    Bitmap resizedBitmapForDisplay = resizeMaintainAspectRatio(bitmap, screenWidth, screenWidth);
////
////                    // Calculate the scale factors
////                    scaleFactorX = (float)resizedBitmapForDisplay.getWidth() / bitmap.getWidth();
////                    scaleFactorY = (float)resizedBitmapForDisplay.getHeight() / bitmap.getHeight();
////
////                    imageView.setImageBitmap(resizedBitmapForDisplay);
////                }
////            }
////        }
////    }
//
//
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == RESULT_OK) {
////            if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
////                if (data != null && data.getData() != null) {
////                    Uri uri = data.getData();
////                    try {
////                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
////                        bitmap = correctOrientation(bitmap, uri);
////                        int inputSize = 640; // This is the size for the model
////                        Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
////                        executor.execute(new ProcessImageTask(resizedBitmapForModel));
////
////                        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
////                        Bitmap resizedBitmapForDisplay = resizeMaintainAspectRatio(bitmap, screenWidth, screenWidth);
////                        imageView.setImageBitmap(resizedBitmapForDisplay);
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
////                if (photoFile != null) {
////                    Uri photoURI = Uri.fromFile(photoFile);
////                    Bitmap bitmap = BitmapFactory.decodeFile(photoURI.getPath());
////                    bitmap = correctOrientation(bitmap, photoURI);
////                    int inputSize = 640; // This is the size for the model
////                    Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
////                    executor.execute(new ProcessImageTask(resizedBitmapForModel));
////
////                    int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
////                    Bitmap resizedBitmapForDisplay = resizeMaintainAspectRatio(bitmap, screenWidth, screenWidth);
////                    imageView.setImageBitmap(resizedBitmapForDisplay);
////                }
////            }
////        }
////    }
//
////    @Override
////    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == RESULT_OK) {
////            if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
////                if (data != null && data.getData() != null) {
////                    Uri uri = data.getData();
////                    try {
////                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
////                        bitmap = correctOrientation(bitmap, uri);
////                        int inputSize = 640;
////                        Bitmap resizedBitmap = resizeImage(bitmap, inputSize, inputSize);
////                        executor.execute(new ProcessImageTask(resizedBitmap));
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                }
////            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
////                if (photoFile != null) {
////                    Uri photoURI = Uri.fromFile(photoFile);
////                    Bitmap bitmap = BitmapFactory.decodeFile(photoURI.getPath());
////                    bitmap = correctOrientation(bitmap, photoURI);
////                    int inputSize = 640;
////                    Bitmap resizedBitmap = resizeImage(bitmap, inputSize, inputSize);
////                    executor.execute(new ProcessImageTask(resizedBitmap));
////                }
////            }
////        }
////    }
//}


package com.rebirth.FishSpeciesDetector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rebirth.FishSpeciesDetector.Classifier.Recognition;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.res.Resources;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.bumptech.glide.Glide;
import android.view.DragEvent;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;

import android.view.MotionEvent;
import android.view.View;
import android.view.DragEvent;
import android.widget.RelativeLayout; // Or LinearLayout or whatever layout you are using
import android.widget.RelativeLayout.LayoutParams; // Be sure to import the correct LayoutParams class
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private Classifier detector;
    private CustomImageView imageView;
    private TextView textView;
    private boolean isFullScreen = false;

    //for resizing the bounding boxes according to original image aspect ratio
    private float scaleFactorX;
    private float scaleFactorY;

    private ConstraintLayout.LayoutParams initialParams; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (CustomImageView) findViewById(R.id.zoomableImageView);
        textView = findViewById(R.id.textView);

        if (hasPermission()) {
            initializeDetector();
        } else {
            requestPermission();
        }
        Button captureImageBtn = findViewById(R.id.captureButton);
        captureImageBtn.setOnClickListener(view -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, "Error occurred while creating the File", ex);
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.rebirth.FishSpeciesDetector.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
                }
            }
        });

        Button selectImageBtn = findViewById(R.id.selectButton);
        selectImageBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE);
        });
        Button showResultButton = findViewById(R.id.showResultButton);
        showResultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                for (Classifier.Recognition result : results) {
                    sb.append(result.getTitle())
                            .append(" - ")
                            .append(String.format("%.2f", result.getConfidence() * 100))
                            .append("%")
                            .append("\n");
                }
                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });

//        // Touch listener for the image view Reset Position
//        imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    // Store the initial layout parameters
//                    initialParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
//                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//                    v.startDrag(null, shadowBuilder, v, 0);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//
//        // Drag listener for the image view
//        imageView.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View v, DragEvent event) {
//                switch (event.getAction()) {
//                    case DragEvent.ACTION_DROP:
//                        // Reset the layout parameters to the initial ones
//                        v.setLayoutParams(initialParams);
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullScreen();
            }
        });
    }


    private void toggleFullScreen() {
        if (isFullScreen) {
            showSystemUI();
        } else {
            hideSystemUI();
        }
        isFullScreen = !isFullScreen;
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }


    private void initializeDetector() {
        try {
            String modelFilename = "best-fp16.tflite"; // Choose the appropriate model file
            detector = DetectorFactory.getDetector(getAssets(), modelFilename);

        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize detector.", e);
        }
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeDetector();
        } else {
            requestPermission();
        }
    }

    // Add other methods to handle camera and image processing, and use the `detector` to recognize objects in the images.
    private List<Classifier.Recognition> results;

    private void processImage(Bitmap bitmap, Bitmap originalBitmap) {
        if (detector == null) {
            Toast.makeText(this, "Detector not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // wait gif image
        ImageView gifImageView = findViewById(R.id.gifImageView);

        // Show ProgressDialog
        runOnUiThread(() -> {
            // To show the GIF
            Glide.with(MainActivity.this).load(R.drawable.wait).into(gifImageView);
            gifImageView.setVisibility(View.VISIBLE);
        });

        // Process the image using the detector
        results = detector.recognizeImage(bitmap);

        // Hide ProgressDialog
        runOnUiThread(() -> gifImageView.setVisibility(View.GONE));

        if (results.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "No fish detected. Please Try Again.", Toast.LENGTH_SHORT).show());
        } else {

            runOnUiThread(() -> {
                // Calculate the scale factors
                int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                Bitmap resizedBitmapForDisplay = resizeMaintainAspect(originalBitmap, screenWidth);
                scaleFactorX = (float) resizedBitmapForDisplay.getWidth() / bitmap.getWidth();
                scaleFactorY = (float) resizedBitmapForDisplay.getHeight() / bitmap.getHeight();

                // Draw bounding boxes on the bitmap
                Bitmap bitmapWithBoundingBoxes = drawBoundingBoxesOnBitmap(resizedBitmapForDisplay, results);

                // Update the image view with the bitmap with bounding boxes
                imageView.setImageBitmap(bitmapWithBoundingBoxes);

                // Construct the result string and set to textView
                StringBuilder sb = new StringBuilder();
                sb.append("A.I thinks that the species of fish is: \n");
                int i = 1;
                for (Classifier.Recognition result : results) {
                    sb.append("Fish " + i + ": ")
                            .append(result.getTitle())
                            .append(" - ")
                            .append(String.format("%.2f", result.getConfidence() * 100))
                            .append("%")
                            .append("\n");
                    i++;
                }
                textView.setText(sb.toString());
            });
        }
    }

    // draw bounding boxes on the result bitmap to show where the fish are detected in the image.
    private Bitmap drawBoundingBoxesOnBitmap(Bitmap originalBitmap, List<Classifier.Recognition> recognitions) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(android.graphics.Color.RED);

        // Add a new Paint object for drawing text
        android.graphics.Paint textPaint = new android.graphics.Paint();
        textPaint.setColor(android.graphics.Color.RED);
        textPaint.setTextSize(40f);

        for (Classifier.Recognition result : recognitions) {
            android.graphics.RectF location = result.getLocation();
            location.left *= scaleFactorX;
            location.top *= scaleFactorY;
            location.right *= scaleFactorX;
            location.bottom *= scaleFactorY;

            canvas.drawRect(location, paint);

            // Draw the label and confidence score
            String label = result.getTitle();
            float confidence = result.getConfidence();
            canvas.drawText(label + " " + String.format("%.2f", confidence), location.left, location.top, textPaint);
        }
        return bitmap;
    }

    private static final int SELECT_PHOTO_REQUEST_CODE = 1;
    private static final int TAKE_PHOTO_REQUEST_CODE = 2;
    private File photoFile;

    // Resize the image.
    private Bitmap resizeImage(Bitmap originalImage, int width, int height) {
        Bitmap resizedImage = Bitmap.createScaledBitmap(originalImage, width, height, false);
        return resizedImage;
    }

    // Resize the image to fit the screen size and maintain the aspect ratio.
    private Bitmap resizeMaintainAspect(Bitmap originalImage, int targetWidth){
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        float aspectRatio = (float)originalHeight/(float)originalWidth;

        int targetHeight = (int) (targetWidth*aspectRatio);

        Bitmap result = Bitmap.createScaledBitmap(originalImage, targetWidth, targetHeight, false);
        return result;
    }

    // Rotate the image to the correct orientation.
    private Bitmap correctOrientation(Bitmap bitmap, Uri uri) {
        try {
            ExifInterface exif = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exif = new ExifInterface(getContentResolver().openInputStream(uri));
            } else {
                exif = new ExifInterface(uri.getPath());
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    private class ProcessImageTask implements Runnable {
        private final Bitmap bitmap;
        private final Bitmap originalBitmap;

        public ProcessImageTask(Bitmap bitmap, Bitmap originalBitmap) {
            this.bitmap = bitmap;
            this.originalBitmap = originalBitmap;
        }

        @Override
        public void run() {
            processImage(bitmap, originalBitmap);
        }
    }

    private Executor executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Reset imageView and textView each time a new image is selected or photographed
        imageView.setImageBitmap(null);
        textView.setText("");

        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == SELECT_PHOTO_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        bitmap = correctOrientation(bitmap, uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                if (photoFile != null) {
                    Uri photoURI = Uri.fromFile(photoFile);
                    bitmap = BitmapFactory.decodeFile(photoURI.getPath());
                    bitmap = correctOrientation(bitmap, photoURI);
                }
            }
            if (bitmap != null) {
                int inputSize = 640; // This is the size for the model
                Bitmap resizedBitmapForModel = resizeImage(bitmap, inputSize, inputSize);
                executor.execute(new ProcessImageTask(resizedBitmapForModel, bitmap));
            }
        }
    }
}
