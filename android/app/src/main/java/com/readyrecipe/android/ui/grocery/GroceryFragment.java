package com.readyrecipe.android.ui.grocery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.readyrecipe.android.R;
import com.readyrecipe.android.adapters.GroceryAdapter;
import com.readyrecipe.android.models.GroceryItem;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.network.SessionManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroceryFragment extends Fragment {
    private GroceryAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grocery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.groceryRecycler);
        View generateButton = view.findViewById(R.id.btnGenerateGrocery);
        View fabAdd = view.findViewById(R.id.groceryFabAdd);

        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        adapter = new GroceryAdapter(this::handleCheckedChange);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        generateButton.setOnClickListener(v -> generateGroceryList());
        fabAdd.setOnClickListener(v -> showAddDialog());

        loadGroceryItems();
    }

    private void loadGroceryItems() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<List<GroceryItem>> call = apiService.getGroceryItems(userId);
        call.enqueue(new Callback<List<GroceryItem>>() {
            @Override
            public void onResponse(Call<List<GroceryItem>> call, Response<List<GroceryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load grocery list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroceryItem>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_grocery, null, false);
        EditText nameInput = dialogView.findViewById(R.id.inputGroceryName);
        EditText qtyInput = dialogView.findViewById(R.id.inputGroceryQuantity);
        EditText unitInput = dialogView.findViewById(R.id.inputGroceryUnit);
        EditText priorityInput = dialogView.findViewById(R.id.inputGroceryPriority);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add grocery item")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String qtyText = qtyInput.getText().toString().trim();
                    String unit = unitInput.getText().toString().trim();
                    String priority = priorityInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BigDecimal qty = qtyText.isEmpty() ? BigDecimal.ONE : new BigDecimal(qtyText);
                    addGroceryItem(name, qty, unit.isEmpty() ? "unit" : unit, priority.isEmpty() ? "normal" : priority);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
    }

    private void addGroceryItem(String name, BigDecimal quantity, String unit, String priority) {
        String userId = sessionManager.getUserId();
        GroceryItem item = new GroceryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setPriority(priority);
        item.setChecked(false);
        try {
            if (userId != null) {
                item.setUserId(UUID.fromString(userId));
            }
        } catch (Exception ignored) {}

        Call<GroceryItem> call = apiService.addGroceryItem(item);
        call.enqueue(new Callback<GroceryItem>() {
            @Override
            public void onResponse(Call<GroceryItem> call, Response<GroceryItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.addItem(response.body());
                    Toast.makeText(requireContext(), "Added to grocery list", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroceryItem> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckedChange(GroceryItem item, boolean isChecked) {
        item.setChecked(isChecked);
        Call<GroceryItem> call = apiService.updateGroceryItem(item.getId() != null ? item.getId().toString() : "", item);
        call.enqueue(new Callback<GroceryItem>() {
            @Override
            public void onResponse(Call<GroceryItem> call, Response<GroceryItem> response) {
                if (response.isSuccessful()) {
                    if (isChecked) {
                        addToPantry(item);
                    }
                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroceryItem> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToPantry(GroceryItem groceryItem) {
        PantryItem pantryItem = new PantryItem();
        pantryItem.setItemName(groceryItem.getName());
        pantryItem.setQuantity(groceryItem.getQuantity());
        pantryItem.setUnit(groceryItem.getUnit());
        pantryItem.setCategory("grocery");
        pantryItem.setApproved(true);
        pantryItem.setExpiryDate(calculateExpiry(groceryItem.getName()));
        try {
            if (sessionManager.getUserId() != null) {
                pantryItem.setUserId(UUID.fromString(sessionManager.getUserId()));
            }
        } catch (Exception ignored) {}

        apiService.addPantryItem(pantryItem).enqueue(new Callback<PantryItem>() {
            @Override
            public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                // No UI needed beyond a quick acknowledgement
                Toast.makeText(requireContext(), "Moved to pantry", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<PantryItem> call, Throwable t) {
                Toast.makeText(requireContext(), "Pantry add failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateGroceryList() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<List<GroceryItem>> call = apiService.generateGrocery(userId);
        call.enqueue(new Callback<List<GroceryItem>>() {
            @Override
            public void onResponse(Call<List<GroceryItem>> call, Response<List<GroceryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                    Toast.makeText(requireContext(), "Generated list", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Generate failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<GroceryItem>> call, Throwable t) {
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
}
