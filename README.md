# FishSpeciesDetector

FishSpeciesDetector is an Android application that uses a **custom-trained AI model** to detect and classify **fish species**, with a focus on Pacific Northwest **salmonids** (Chinook, Coho, Sockeye, Pink, Chum, Steelhead, Trout, and Kokanee).  
This project was part of my research and applied AI work to bring **computer vision into environmental conservation, fisheries management, and outdoor recreation**.

---

## 🌍 Motivation & Impact

Fishing is an important part of culture, food supply, and recreation. Correctly identifying fish species is critical for:

- **Conservation**: Many species have strict regulations (size limits, catch-and-release rules, endangered listings). Misidentification can harm populations.  
- **Recreational Anglers**: Ensures compliance with fishing laws and promotes sustainable practices.  
- **Fisheries Scientists**: Helps monitor population health and migrations.  
- **Education**: Teaches children, students, and citizen scientists about biodiversity.  

By deploying an **offline AI-powered app** that works directly on a smartphone, this project makes **real-time fish identification accessible to anyone in the field** — even without internet access.  

---

## 📱 Features

- **Capture Images**: Take a photo of a fish using the phone’s camera.  
- **Select Images**: Choose a fish photo from the device’s gallery.  
- **AI-powered Detection**: Uses a YOLOv5 → TensorFlow Lite model to classify species.  
- **Confidence Scores**: Displays prediction probabilities for transparency.  
- **Detailed Results**: Bounding box + species information.  
- **Offline Mode**: Works anywhere, even without network connectivity.  
- **Lightweight UI**: Simple, practical design for outdoor use.  

---

## 🖼️ Screenshots

### App Icon
<p align="center">
  <img src="https://github.com/exxxius/FishSpeciesDetector/raw/main/screenshots/Screenshot_20230519-091019_FishSpeciesDetector.jpg" width="50%" alt="App Icon">
</p>

### Main Screen
<p align="center">
  <img src="https://github.com/exxxius/FishSpeciesDetector/raw/main/screenshots/Screenshot_20230519-091026_FishSpeciesDetector.jpg" width="50%" alt="Main Screen">
</p>

### Loading Screen
<p align="center">
  <img src="https://github.com/exxxius/FishSpeciesDetector/raw/main/screenshots/Screenshot_20230519-091042_FishSpeciesDetector.jpg" width="50%" alt="Loading Screen">
</p>

### Results Screen
<p align="center">
  <img src="https://github.com/exxxius/FishSpeciesDetector/raw/main/screenshots/Screenshot_20230519-091052_FishSpeciesDetector.jpg" width="50%" alt="Results Screen">
</p>

### Results Detail Screen
<p align="center">
  <img src="https://github.com/exxxius/FishSpeciesDetector/raw/main/screenshots/Screenshot_20230519-091101_FishSpeciesDetector.jpg" width="50%" alt="Results Detail Screen">
</p>

---

## ⚙️ Technical Overview

- **Platform**: Android (Java/Kotlin).  
- **Model**: YOLOv5 distilled to **TensorFlow Lite**.  
- **Dataset**: Custom-built **Salmonidae dataset**, annotated with bounding boxes & species labels.  
- **Pipeline**: Image → Preprocessing → TFLite model → Classification + confidence → UI display.  
- **Performance**: Optimized to run under ~100ms on mid-range Android devices.  
- **Model Size**: 165 MB after quantization.

For dataset creation, training pipeline, and model distillation details, see the companion repo:  
👉 [YOLOv5-TFLite-FishDetector-Model-Training](https://github.com/exxxius/YOLOv5-TFLite-FishDetector-Model-Training)

---

## 🚀 Installation Instructions

### Requirements
- Android Studio (latest version).  
- Android SDK 33+ installed.  
- A physical Android device or emulator running Android 8.0 (API 26) or newer.  

### Steps
1. Clone the repository:
   ```sh
   git clone https://github.com/exxxius/FishSpeciesDetector.git
   ```
2. Open the project in **Android Studio**.  
3. Let Gradle sync and download all required dependencies.  
4. Connect an Android device (or start an emulator).  
5. Build & Run the app.  
6. The bundled TensorFlow Lite model (`model.tflite`) is already included in `/app/src/main/assets/`.  

---

## ▶️ Usage Instructions

1. Launch the **FishSpeciesDetector** app.  
2. From the **Main Menu**:  
   - Tap **Capture Image** to take a new photo.  
   - Tap **Select Image** to choose one from gallery.  
3. Tap **Show Result** to run AI detection.  
4. Review the results:  
   - The detected species name.  
   - A bounding box around the fish.  
   - Confidence percentage scores.  
5. Open **Detailed Results** to see additional species information.  

---

## 🧑‍💻 Skills Demonstrated

- Computer Vision (YOLOv5, TensorFlow, TensorFlow Lite).  
- Dataset curation & annotation (Salmonidae species).  
- Model optimization for mobile edge devices (quantization, pruning).  
- Android development (camera integration, storage access, UI).  
- End-to-end product design: **from dataset → training → mobile deployment**.  

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for details.

---

## 📬 Contact

For professional inquiries, please reach out via my GitHub profile:  
[https://github.com/exxxius](https://github.com/exxxius)  

---
