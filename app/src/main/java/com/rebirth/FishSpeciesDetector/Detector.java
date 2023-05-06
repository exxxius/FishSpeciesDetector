////package com.rebirth.FishSpeciesDetector;
////
////import android.content.Context;
////import android.graphics.Bitmap;
////import android.graphics.RectF;
////
////import org.tensorflow.lite.Interpreter;
////import org.tensorflow.lite.support.common.FileUtil;
////import org.tensorflow.lite.support.label.TensorLabel;
////import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
////
////import java.io.IOException;
////import java.util.ArrayList;
////import java.util.List;
////import java.util.Map;
////
////public class Detector {
////    private static final int NUM_THREADS = 4;
////
////    private final Interpreter interpreter;
////    private final int inputSize;
////    private final List<String> labels;
////
////    public Detector(Context context, String modelPath, String labelPath, int inputSize) throws IOException {
////        this.context = context;
////        this.labels = loadLabelList(context, labelPath);
////        this.inputSize = inputSize;
////        this.imgData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
////        this.imgData.order(ByteOrder.nativeOrder());
////
////        Interpreter.Options options = new Interpreter.Options();
////        options.setNumThreads(NUM_THREADS);
////
////        try {
////            this.interpreter = new Interpreter(loadModelFile(context, modelPath), options);
////        } catch (IOException e) {
////            Log.e(TAG, "Failed to create interpreter.", e);
////            throw e;
////        }
////    }
////
////
////    public List<Recognition> recognizeImage(Bitmap bitmap) {
////        // Preprocess the bitmap and run the model
////        float[][][] input = preprocessBitmap(bitmap);
////        float[][] output = new float[1][labels.size()];
////        interpreter.run(input, output);
////
////        // Post-process the model output
////        return postProcessModelOutput(output[0]);
////    }
////
////    private float[][][] preprocessBitmap(Bitmap bitmap) {
////        // Resize the bitmap and normalize the pixel values
////        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
////        float[][][] input = new float[1][inputSize][inputSize];
////        for (int i = 0; i < inputSize; ++i) {
////            for (int j = 0; j < inputSize; ++j) {
////                int pixel = resizedBitmap.getPixel(i, j);
////                input[0][i][j] = (((pixel >> 16) & 0xFF) - 127.5f) / 127.5f;
////            }
////        }
////        return input;
////    }
////
////    private List<Recognition> postProcessModelOutput(float[] output) {
////        List<Recognition> recognitions = new ArrayList<>();
////        for (int i = 0; i < output.length; ++i) {
////            if (output[i] > 0.6f) {
////                recognitions.add(new Recognition(String.valueOf(i), labels.get(i), output[i], null));
////            }
////        }
////        return recognitions;
////    }
////
////    public void close() {
////        if (interpreter != null) {
////            interpreter.close();
////        }
////    }
////}
//
//
//package com.rebirth.FishSpeciesDetector;
//
//import android.content.Context;
//import android.content.res.AssetFileDescriptor;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.RectF;
//import android.util.Log;
//
//import org.tensorflow.lite.Interpreter;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Detector {
//    private static final String TAG = "Detector";
//    private static final int BATCH_SIZE = 1;
//    private static final int PIXEL_SIZE = 3;
//    private static final int NUM_THREADS = 4;
//    private static final float THRESHOLD = 0.5f; // Adjust this value as needed
//
//    private final Context context;
//    private final List<String> labels;
//    private final int inputSize;
//    private final ByteBuffer imgData;
//    private final Interpreter interpreter;
//
//    public Detector(Context context, String modelPath, String labelPath, int inputSize) throws IOException {
//        this.context = context;
//        this.labels = loadLabelList(context, labelPath);
//        this.inputSize = inputSize;
//        this.imgData = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
//        this.imgData.order(ByteOrder.nativeOrder());
//
//        Interpreter.Options options = new Interpreter.Options();
//        options.setNumThreads(NUM_THREADS);
//
//        try {
//            this.interpreter = new Interpreter(loadModelFile(context, modelPath), options);
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to create interpreter.", e);
//            throw e;
//        }
//    }
//
//    // detects the fish species from the bitmap image and returns a list of recognitions.
//    public List<Recognition> recognizeImage(Bitmap bitmap) {
//        preprocessBitmap(bitmap, inputSize, imgData);
//        float[][][] labelProbArray = new float[1][25200][11];
//        interpreter.run(imgData, labelProbArray);
//        return postProcessModelOutput(labelProbArray);
//    }
//
//
//    private static void preprocessBitmap(Bitmap bitmap, int inputSize, ByteBuffer imgData) {
//        int[] intValues = new int[inputSize * inputSize];
//        imgData.rewind();
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//        for (int i = 0; i < inputSize; ++i) {
//            for (int j = 0; j < inputSize; ++j) {
//                int pixelValue = intValues[i * inputSize + j];
//                imgData.putFloat((((pixelValue >> 16) & 0xFF) - 128) / 128.0f);
//                imgData.putFloat((((pixelValue >> 8) & 0xFF) - 128) / 128.0f);
//                imgData.putFloat(((pixelValue & 0xFF) - 128) / 128.0f);
//            }
//        }
//    }
//
//    //    private List<Recognition> postProcessModelOutput(float[][] labelProbArray) {
////        List<Recognition> recognitions = new ArrayList<>();
////
////        for (int i = 0; i < labels.size(); i++) {
////            recognitions.add(new Recognition("" + i, labels.get(i), labelProbArray[0][i], null));
////        }
////        return recognitions;
////    }
////    private List<Recognition> postProcessModelOutput(float[][][] labelProbArray) {
////        List<Recognition> recognitions = new ArrayList<>();
////
////        for (int i = 0; i < labelProbArray[0].length; i++) {
////            float maxValue = -Float.MAX_VALUE;
////            int maxIndex = -1;
////
////            for (int j = 0; j < labels.size(); j++) {
////                if (labelProbArray[0][i][j] > maxValue) {
////                    maxValue = labelProbArray[0][i][j];
////                    maxIndex = j;
////                }
////            }
////            if (maxValue > THRESHOLD) {
////                String id = String.format("%s-%d", labels.get(maxIndex), i);
////                RectF location = new RectF(); // Add the correct location if it is available from the model output
////                recognitions.add(new Recognition(id, labels.get(maxIndex), maxValue, location));
////            }
////        }
////        return recognitions;
////    }
//    private List<Recognition> postProcessModelOutput(float[][][] labelProbArray) {
//        List<Recognition> recognitions = new ArrayList<>();
//        float maxValue = -Float.MAX_VALUE;
//        int maxIndex = -1;
//
//        for (int i = 0; i < labelProbArray[0].length; i++) {
//            for (int j = 0; j < labels.size(); j++) {
//                if (labelProbArray[0][i][j] > maxValue) {
//                    maxValue = labelProbArray[0][i][j];
//                    maxIndex = j;
//                }
//            }
//        }
//
//        if (maxValue > THRESHOLD) {
//            String id = String.format("%s-%d", labels.get(maxIndex), 0);
//            RectF location = new RectF(); // Add the correct location if it is available from the model output
//            recognitions.add(new Recognition(id, labels.get(maxIndex), maxValue, location));
//        }
//
//        return recognitions;
//    }
//
//
//    // loads the model file from the assets folder
//    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
//        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
//        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//
//    // loads the labels from the label txt file in the assets folder
//    private List<String> loadLabelList(Context context, String labelPath) throws IOException {
//        List<String> labelList = new ArrayList<>();
//        BufferedReader reader = null;
//        try {
//            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(labelPath)));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                labelList.add(line);
//            }
//        } catch (IOException e) {
//            throw new IOException("Failed to load label list.", e);
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error closing label file reader.", e);
//                }
//            }
//        }
//        return labelList;
//    }
//
//    public void close() {
//        if (interpreter != null) {
//            interpreter.close();
//        }
//    }
//}
