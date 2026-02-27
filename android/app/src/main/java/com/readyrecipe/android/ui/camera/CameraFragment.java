package com.readyrecipe.android.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.readyrecipe.android.R;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.network.SessionManager;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class CameraFragment extends Fragment {
    private static final int CAMERA_PERMISSION_CODE = 2001;
    private static final String TFLITE_MODEL = "ssd_mobilenet_v2_fpnlite.tflite";
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int IMG_SIZE_X = 300;
    private static final int IMG_SIZE_Y = 300;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private Interpreter tfliteInterpreter;
    private Set<String> groceryLabels;
    private ProgressBar progressBar;
    private TextView statusText;
    private View detectButton;
    private ApiService apiService;
    private SessionManager sessionManager;

    // COCO class ID to label mapping (80 classes + a few foods)
    private static final String[] COCO_LABELS = {
            "person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck",
            "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench",
            "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
            "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis",
            "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard",
            "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife",
            "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot",
            "hot dog", "pizza", "donut", "cake", "chair", "couch", "potted plant", "bed",
            "dining table", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard",
            "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase",
            "scissors", "teddy bear", "hair drier", "toothbrush", "bread", "cucumber", "tomato",
            "pepper", "lemon", "onion", "strawberry", "plate", "dish"
    };

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

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        executorService = Executors.newSingleThreadExecutor();
        loadGroceryLabels();
        initModel();

        detectButton.setOnClickListener(v -> captureAndDetect());
        ensureCameraPermission();
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

    private void initModel() {
        try {
            tfliteInterpreter = new Interpreter(FileUtil.loadMappedFile(requireContext(), TFLITE_MODEL));
            statusText.setText("Camera ready - tap DETECT to scan");
        } catch (IOException e) {
            statusText.setText("Model load failed");
            Toast.makeText(requireContext(), "Failed to load model", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGroceryLabels() {
        groceryLabels = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requireContext().getAssets().open("grocery_labels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                groceryLabels.add(line.toLowerCase(Locale.getDefault()).trim());
            }
        } catch (IOException e) {
            statusText.setText("Label load failed");
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
            Toast.makeText(requireContext(), "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        statusText.setText("Capturing image...");
        progressBar.setVisibility(View.VISIBLE);
        detectButton.setEnabled(false);

        File photoFile = new File(requireContext().getExternalCacheDir(), "photo.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                statusText.post(() -> statusText.setText("Running detection..."));
                runDetection(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    detectButton.setEnabled(true);
                    statusText.setText("Capture failed");
                });
            }
        });
    }

    private void runDetection(File photoFile) {
        executorService.execute(() -> {
            try {
                Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE_X, IMG_SIZE_Y, true);

                ImageProcessor imageProcessor = new ImageProcessor.Builder()
                        .add(new ResizeOp(IMG_SIZE_X, IMG_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
                        .build();

                TensorImage tImage = new TensorImage(DataType.UINT8);
                tImage.load(resized);
                TensorImage processed = imageProcessor.process(tImage);

                Map<Integer, Object> outputMap = new HashMap<>();
                TensorBuffer detectionScores = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);
                TensorBuffer detectionClasses = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);

                outputMap.put(0, TensorBuffer.createFixedSize(new int[]{1, 10, 4}, DataType.FLOAT32).getBuffer());
                outputMap.put(1, detectionClasses.getBuffer());
                outputMap.put(2, detectionScores.getBuffer());
                outputMap.put(3, TensorBuffer.createFixedSize(new int[]{1}, DataType.FLOAT32).getBuffer());

                tfliteInterpreter.runForMultipleInputsOutputs(new Object[]{processed.getBuffer()}, outputMap);

                float[] scores = detectionScores.getFloatArray();
                float[] classes = detectionClasses.getFloatArray();
                List<DetectionResult> detected = new ArrayList<>();
                for (int i = 0; i < Math.min(10, scores.length); i++) {
                    float confidence = scores[i];
                    int classId = (int) classes[i];
                    if (confidence >= CONFIDENCE_THRESHOLD && classId < COCO_LABELS.length) {
                        String label = COCO_LABELS[classId].toLowerCase(Locale.getDefault());
                        if (groceryLabels.contains(label)) {
                            detected.add(new DetectionResult(label, confidence));
                        }
                    }
                }
                detected.sort(Comparator.comparingDouble(d -> -d.confidence));

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    detectButton.setEnabled(true);
                    if (detected.isEmpty()) {
                        statusText.setText("No grocery items detected");
                        Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT).show();
                    } else {
                        statusText.setText("Detected " + detected.size() + " items");
                        showApprovalDialog(detected);
                    }
                });

                bitmap.recycle();
                resized.recycle();
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    detectButton.setEnabled(true);
                    statusText.setText("Detection error");
                    Toast.makeText(requireContext(), "Detection error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showApprovalDialog(List<DetectionResult> results) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 0);

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout listLayout = new LinearLayout(requireContext());
        listLayout.setOrientation(LinearLayout.VERTICAL);

        List<RowSelection> selections = new ArrayList<>();

        for (DetectionResult result : results) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);

            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setChecked(true);
            checkBox.setText(capitalize(result.label) + " (" + Math.round(result.confidence * 100) + "%)");
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            EditText qtyInput = new EditText(requireContext());
            qtyInput.setText("1");
            qtyInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            qtyInput.setLayoutParams(new LinearLayout.LayoutParams(160, ViewGroup.LayoutParams.WRAP_CONTENT));

            row.addView(checkBox);
            row.addView(qtyInput);
            listLayout.addView(row);
            selections.add(new RowSelection(result.label, checkBox, qtyInput));
        }

        scrollView.addView(listLayout);
        container.addView(scrollView);

        new AlertDialog.Builder(requireContext())
                .setTitle("Approve items")
                .setView(container)
                .setPositiveButton("Add to Pantry", (d, which) -> handleSelections(selections))
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
    }

    private void handleSelections(List<RowSelection> selections) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PantryItem> items = new ArrayList<>();
        for (RowSelection selection : selections) {
            if (!selection.checkBox.isChecked()) continue;
            int qty = 1;
            try {
                qty = Integer.parseInt(selection.qtyInput.getText().toString());
            } catch (NumberFormatException ignored) {}

            PantryItem item = new PantryItem();
            item.setItemName(selection.label);
            item.setQuantity(BigDecimal.valueOf(qty));
            item.setUnit("unit");
            item.setCategory("grocery");
            item.setExpiryDate(calculateExpiry(selection.label));
            item.setApproved(true);
            try {
                item.setUserId(UUID.fromString(userId));
            } catch (Exception ignored) {}
            items.add(item);
        }

        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Nothing selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<PantryItem>> call = apiService.addPantryItems(items);
        call.enqueue(new Callback<List<PantryItem>>() {
            @Override
            public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Added " + items.size() + " items", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Add failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private static class DetectionResult {
        final String label;
        final float confidence;
        DetectionResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    private static class RowSelection {
        final String label;
        final CheckBox checkBox;
        final EditText qtyInput;
        RowSelection(String label, CheckBox checkBox, EditText qtyInput) {
            this.label = label;
            this.checkBox = checkBox;
            this.qtyInput = qtyInput;
        }
    }
}
