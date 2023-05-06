//package com.rebirth.FishSpeciesDetector;
//
//import android.graphics.RectF;
//
//public class Recognition {
//    private final String id;
//    private final String title;
//    private final Float confidence;
//    private RectF location;
//
//    public Recognition(String id, String title, float confidence, RectF location) {
//        this.id = id;
//        this.title = title;
//        this.confidence = confidence;
//        this.location = location;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public Float getConfidence() {
//        return confidence;
//    }
//
//    public RectF getLocation() {
//        return new RectF(location);
//    }
//
//    public void setLocation(RectF location) {
//        this.location = location;
//    }
//
//    @Override
//    public String toString() {
//        return "Recognition{" +
//                "id='" + id + '\'' +
//                ", title='" + title + '\'' +
//                ", confidence=" + confidence +
//                ", location=" + location +
//                '}';
//    }
//}

package com.rebirth.FishSpeciesDetector;
public class Recognition {
    private final String id;
    private final String title;
    private final Float confidence;
    private final BoundingBox location;

    public Recognition(String id, String title, Float confidence, BoundingBox location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    public BoundingBox getLocation() {
        return location;
    }

        @Override
    public String toString() {
        return "Recognition{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", confidence=" + confidence +
                ", location=" + location +
                '}';
    }
}

