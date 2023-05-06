package com.rebirth.FishSpeciesDetector;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        Button captureButton = view.findViewById(R.id.captureButton);

        if (allPermissionsGranted()) {
            startCamera(view);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        captureButton.setOnClickListener(v -> {
            File file = new File(getActivity().getExternalFilesDir(null), "captured_fish.jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

            imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    // Handle the image processing and detection
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    exception.printStackTrace();
                }
            });
        });

        return view;
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera(View view) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                cameraProvider.unbindAll();
                androidx.camera.core.Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                preview.setSurfaceProvider(((androidx.camera.view.PreviewView) view.findViewById(R.id.previewView)).getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}
