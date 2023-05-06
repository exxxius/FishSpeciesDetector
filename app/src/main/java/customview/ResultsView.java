package customview;

import com.rebirth.FishSpeciesDetector.Recognition;

//import org.tensorflow.lite.examples.detection.tflite.Classifier.Recognition;

import java.util.List;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
