package com.readyrecipe.android.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.DetectedItem;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.network.SessionManager;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraFragment extends Fragment {
    private static final int CAMERA_PERMISSION_CODE = 2001;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private ProgressBar progressBar;
    private TextView statusText;
    private View detectButton;

    private ApiService apiService;
    private SessionManager sessionManager;
    private CameraViewModel cameraViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewView = view.findViewById(R.id.cameraPreview);
        progressBar = view.findViewById(R.id.detectionProgress);
        statusText = view.findViewById(R.id.statusText);
        detectButton = view.findViewById(R.id.detectButton);

        View topBarTitle = view.findViewById(R.id.topBarTitle);
        if (topBarTitle instanceof TextView) {
            ((TextView) topBarTitle).setText("Camera");
        }

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        executorService = Executors.newSingleThreadExecutor();
        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        observeViewModel();
        detectButton.setOnClickListener(v -> captureAndDetect());
        ensureCameraPermission();
    }

    private void observeViewModel() {
        CameraStateFlowInterop.collect(
                getViewLifecycleOwner(),
                cameraViewModel.getUiState(),
                state -> {
                    if (state instanceof CameraUiState.Idle) {
                        setLoading(false);
                        statusText.setText("Camera ready - point at groceries and tap Detect Items");
                    } else if (state instanceof CameraUiState.Loading) {
                        setLoading(true);
                        statusText.setText("Running detection...");
                    } else if (state instanceof CameraUiState.Success success) {
                        setLoading(false);
                        statusText.setText("Detected " + success.getItems().size() + " items");
                        mergeDetectedItemsToPantry(success.getItems());
                    } else if (state instanceof CameraUiState.Error error) {
                        setLoading(false);
                        statusText.setText("Detection failed");
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                statusText.setText("Camera init failed");
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new android.util.Size(1920, 1080))
                .build();

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), selector, preview, imageCapture);
        } catch (Exception e) {
            statusText.setText("Camera bind failed");
        }
    }

    private void captureAndDetect() {
        if (imageCapture == null) {
            Bitmap fallbackBitmap = previewView.getBitmap();
            if (fallbackBitmap != null) {
                cameraViewModel.onDetectClicked(fallbackBitmap);
            } else {
                Toast.makeText(requireContext(), "Camera not ready", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        setLoading(true);
        statusText.setText("Capturing image...");

        File photoFile = new File(requireContext().getExternalCacheDir(), "camera_detect.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap == null) {
                    bitmap = previewView.getBitmap();
                }
                Bitmap finalBitmap = bitmap;
                requireActivity().runOnUiThread(() -> {
                    if (finalBitmap != null) {
                        cameraViewModel.onDetectClicked(finalBitmap);
                    } else {
                        setLoading(false);
                        statusText.setText("Capture failed");
                        Toast.makeText(requireContext(), "Unable to capture image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() -> {
                    Bitmap fallbackBitmap = previewView.getBitmap();
                    if (fallbackBitmap != null) {
                        cameraViewModel.onDetectClicked(fallbackBitmap);
                    } else {
                        setLoading(false);
                        statusText.setText("Capture failed");
                        Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void mergeDetectedItemsToPantry(List<DetectedItem> detectedItems) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            cameraViewModel.resetState();
            return;
        }

        Call<List<PantryItem>> getCall = apiService.getPantryItems(userId);
        getCall.enqueue(new Callback<List<PantryItem>>() {
            @Override
            public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                List<PantryItem> currentPantry = response.body() != null ? response.body() : new ArrayList<>();
                Map<String, PantryItem> mergedByName = new LinkedHashMap<>();

                for (PantryItem existing : currentPantry) {
                    String normalizedName = normalizeItemName(existing.getItemName());
                    if (!normalizedName.isEmpty()) {
                        mergedByName.put(normalizedName, existing);
                    }
                }

                for (DetectedItem detectedItem : detectedItems) {
                    String normalizedDetectedName = normalizeItemName(detectedItem.getName());
                    if (normalizedDetectedName.isEmpty()) {
                        continue;
                    }
                    PantryItem existingItem = mergedByName.get(normalizedDetectedName);
                    if (existingItem != null) {
                        BigDecimal currentQuantity = existingItem.getQuantity() != null ? existingItem.getQuantity() : BigDecimal.ZERO;
                        existingItem.setQuantity(currentQuantity.add(BigDecimal.ONE));
                    } else {
                        PantryItem newItem = createPantryItem(normalizedDetectedName, userId);
                        mergedByName.put(normalizedDetectedName, newItem);
                    }
                }

                List<PantryItem> mergedItems = new ArrayList<>(mergedByName.values());
                Call<List<PantryItem>> bulkCall = apiService.addPantryItems(mergedItems);
                bulkCall.enqueue(new Callback<List<PantryItem>>() {
                    @Override
                    public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                        if (response.isSuccessful()) {
                            showAddedSnackbar();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update pantry", Toast.LENGTH_SHORT).show();
                        }
                        cameraViewModel.resetState();
                    }

                    @Override
                    public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        cameraViewModel.resetState();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                cameraViewModel.resetState();
            }
        });
    }

    private PantryItem createPantryItem(String normalizedName, String userId) {
        PantryItem item = new PantryItem();
        item.setItemName(normalizedName);
        item.setQuantity(BigDecimal.ONE);
        item.setUnit("unit");
        item.setCategory("grocery");
        item.setExpiryDate(calculateExpiry(normalizedName));
        item.setApproved(true);
        try {
            item.setUserId(UUID.fromString(userId));
        } catch (Exception ignored) {
            // Keep null userId if format is not UUID
        }
        return item;
    }

    private String normalizeItemName(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.trim().toLowerCase(Locale.getDefault());
    }

    private void showAddedSnackbar() {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, "3 items added to pantry", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        detectButton.setEnabled(!loading);
    }

    private String calculateExpiry(String label) {
        String lower = label.toLowerCase(Locale.getDefault());
        int days;
        if (lower.contains("tomato")) {
            days = 7;
        } else if (lower.contains("milk")) {
            days = 5;
        } else {
            days = 5;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
