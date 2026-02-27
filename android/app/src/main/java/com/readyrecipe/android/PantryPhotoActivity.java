package com.readyrecipe.android;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;

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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantryPhotoActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private Interpreter tfliteInterpreter;
    private Set<String> groceryLabels;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button detectButton;
    private static final String TFLITE_MODEL = "ssd_mobilenet_v2_fpnlite.tflite";
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int IMG_SIZE_X = 300;
    private static final int IMG_SIZE_Y = 300;
    private static final int CAMERA_PERMISSION_CODE = 100;

    // COCO class ID to label mapping (80 classes)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_photo);

        sharedPreferences = getSharedPreferences("ReadyRecipePrefs", MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();

        previewView = findViewById(R.id.cameraPreview);
        progressBar = findViewById(R.id.detectionProgress);
        statusText = findViewById(R.id.statusText);
        detectButton = findViewById(R.id.detectButton);
        Button backButton = findViewById(R.id.backButton);

        progressBar.setVisibility(android.view.View.GONE);

        // Load grocery labels from asset
        loadGroceryLabels();

        // Initialize TFLite model
        try {
            tfliteInterpreter = new Interpreter(FileUtil.loadMappedFile(this, TFLITE_MODEL));
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load TFLite model: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            statusText.setText("Model load failed: " + e.getMessage());
        }

        // Check and request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }

        detectButton.setOnClickListener(v -> captureAndDetect());
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                statusText.setText("Camera permission required");
                finish();
            }
        }
    }

    private void loadGroceryLabels() {
        groceryLabels = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("grocery_labels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                groceryLabels.add(line.toLowerCase().trim());
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load grocery labels", Toast.LENGTH_SHORT).show();
            statusText.setText("Failed to load labels");
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
                statusText.setText("Camera ready - tap DETECT to scan");
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Camera initialization failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                statusText.setText("Camera error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        // Preview use case
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new android.util.Size(1920, 1080))
                .build();

        // Select back camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to bind camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            statusText.setText("Camera bind failed");
        }
    }

    private void captureAndDetect() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Capturing image...");
        progressBar.setVisibility(android.view.View.VISIBLE);
        detectButton.setEnabled(false);

        File photoFile = new File(getExternalCacheDir(), "photo.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                statusText.setText("Running detection...");
                runDetection(photoFile);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    detectButton.setEnabled(true);
                    statusText.setText("Capture failed: " + exception.getMessage());
                    Toast.makeText(PantryPhotoActivity.this,
                            "Failed to capture image: " + exception.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void runDetection(File photoFile) {
        executorService.execute(() -> {
            try {
                // Load and resize bitmap
                Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE_X, IMG_SIZE_Y, true);

                // Prepare TensorFlow Lite input
                ImageProcessor imageProcessor = new ImageProcessor.Builder()
                        .add(new ResizeOp(IMG_SIZE_X, IMG_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
                        .build();

                TensorImage tImage = new TensorImage(DataType.UINT8);
                tImage.load(resizedBitmap);
                TensorImage processedImage = imageProcessor.process(tImage);

                // Run inference
                Map<Integer, Object> outputMap = new HashMap<>();
                TensorBuffer detectionScores = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);
                TensorBuffer detectionClasses = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);

                outputMap.put(0, TensorBuffer.createFixedSize(new int[]{1, 10, 4}, DataType.FLOAT32).getBuffer());
                outputMap.put(1, detectionClasses.getBuffer());
                outputMap.put(2, detectionScores.getBuffer());
                outputMap.put(3, TensorBuffer.createFixedSize(new int[]{1}, DataType.FLOAT32).getBuffer());

                tfliteInterpreter.runForMultipleInputsOutputs(
                        new Object[]{processedImage.getBuffer()}, outputMap);

                // Extract results
                float[] scores = detectionScores.getFloatArray();
                float[] classes = detectionClasses.getFloatArray();

                List<DetectionResult> detectedItems = new ArrayList<>();

                for (int i = 0; i < Math.min(10, scores.length); i++) {
                    float confidence = scores[i];
                    int classId = (int) classes[i];

                    if (confidence >= CONFIDENCE_THRESHOLD && classId < COCO_LABELS.length) {
                        String label = COCO_LABELS[classId].toLowerCase();
                        if (groceryLabels.contains(label)) {
                            detectedItems.add(new DetectionResult(label, confidence));
                        }
                    }
                }

                // Sort by confidence descending
                detectedItems.sort(Comparator.comparingDouble(d -> -d.confidence));

                // Show results dialog on UI thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    detectButton.setEnabled(true);
                    if (detectedItems.isEmpty()) {
                        statusText.setText("No grocery items detected");
                        Toast.makeText(PantryPhotoActivity.this, "No grocery items detected", Toast.LENGTH_SHORT).show();
                    } else {
                        statusText.setText("Detected " + detectedItems.size() + " items");
                        showDetectionResults(detectedItems);
                    }
                });

                // Cleanup
                bitmap.recycle();
                resizedBitmap.recycle();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    detectButton.setEnabled(true);
                    statusText.setText("Detection error: " + e.getMessage());
                    Toast.makeText(PantryPhotoActivity.this,
                            "Detection error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showDetectionResults(List<DetectionResult> results) {
        // Build results string with confidence scores
        StringBuilder resultsText = new StringBuilder("Detected Groceries:\n\n");
        for (DetectionResult result : results) {
            resultsText.append(String.format("• %s (%.0f%%)\n",
                    formatLabel(result.label),
                    result.confidence * 100));
        }

        // Show dialog with bulk add option
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detection Results")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(resultsText.toString())
                .setPositiveButton("Add to Pantry", (dialog, which) -> {
                    showQuantityInputDialog(results);
                })
                .setNegativeButton("Discard", (dialog, which) -> {
                    statusText.setText("Results discarded");
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showQuantityInputDialog(List<DetectionResult> results) {
        // Create a list of items with quantity inputs
        List<ItemWithQuantity> itemsToAdd = new ArrayList<>();
        for (DetectionResult result : results) {
            itemsToAdd.add(new ItemWithQuantity(result.label, 1));
        }

        // Build custom dialog with quantity inputs
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.widget.LinearLayout containerLayout = new android.widget.LinearLayout(this);
        containerLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        containerLayout.setPadding(20, 20, 20, 20);

        TextView instructionText = new TextView(this);
        instructionText.setText("Set quantities for each item:");
        instructionText.setTextSize(16);
        instructionText.setTextColor(getColor(R.color.heading_primary));
        instructionText.setPadding(0, 0, 0, 20);
        containerLayout.addView(instructionText);

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        android.widget.LinearLayout itemsLayout = new android.widget.LinearLayout(this);
        itemsLayout.setOrientation(android.widget.LinearLayout.VERTICAL);

        List<EditText> quantityInputs = new ArrayList<>();

        for (ItemWithQuantity item : itemsToAdd) {
            android.widget.LinearLayout itemRow = new android.widget.LinearLayout(this);
            itemRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            itemRow.setPadding(0, 10, 0, 10);

            TextView labelView = new TextView(this);
            labelView.setText(formatLabel(item.itemName));
            labelView.setTextSize(14);
            labelView.setTextColor(getColor(R.color.text_body));
            labelView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            itemRow.addView(labelView);

            EditText quantityInput = new EditText(this);
            quantityInput.setText(String.valueOf(item.quantity));
            quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            quantityInput.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    100, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            quantityInput.setBackgroundColor(getColor(R.color.card_background));
            itemRow.addView(quantityInput);
            quantityInputs.add(quantityInput);

            itemsLayout.addView(itemRow);
        }

        scrollView.addView(itemsLayout);
        containerLayout.addView(scrollView);

        builder.setTitle("Quantities")
                .setView(containerLayout)
                .setPositiveButton("Add All", (dialog, which) -> {
                    // Collect quantities and add items
                    String userId = sharedPreferences.getString("userId", "");
                    if (userId.isEmpty()) {
                        Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (int i = 0; i < itemsToAdd.size(); i++) {
                        int quantity = 1;
                        try {
                            String qtyText = quantityInputs.get(i).getText().toString();
                            if (!qtyText.isEmpty()) {
                                quantity = Integer.parseInt(qtyText);
                            }
                        } catch (NumberFormatException e) {
                            quantity = 1;
                        }
                        addPantryItem(userId, itemsToAdd.get(i).itemName, quantity);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    statusText.setText("Add cancelled");
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void addPantryItem(String userId, String itemName, int quantity) {
        PantryItem item = new PantryItem();
        item.setItemName(itemName);
        item.setQuantity(BigDecimal.valueOf(quantity));
        item.setUserId(UUID.fromString(userId));
        item.setUnit("unit");
        item.setCategory("grocery");

        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        Call<PantryItem> call = apiService.addPantryItem(item);
        call.enqueue(new Callback<PantryItem>() {
            @Override
            public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PantryPhotoActivity.this,
                            "✓ Added " + formatLabel(itemName), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PantryPhotoActivity.this,
                            "Failed to add " + formatLabel(itemName), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PantryItem> call, Throwable t) {
                Toast.makeText(PantryPhotoActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatLabel(String label) {
        if (label == null || label.isEmpty()) return "";
        return label.substring(0, 1).toUpperCase() + label.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
        executorService.shutdown();
    }

    // Helper classes
    private static class DetectionResult {
        String label;
        float confidence;

        DetectionResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    private static class ItemWithQuantity {
        String itemName;
        int quantity;

        ItemWithQuantity(String itemName, int quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }
}
