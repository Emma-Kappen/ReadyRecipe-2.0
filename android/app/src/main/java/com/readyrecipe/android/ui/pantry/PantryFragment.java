package com.readyrecipe.android.ui.pantry;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.readyrecipe.android.BottomNavigationActivity;
import com.readyrecipe.android.R;
import com.readyrecipe.android.adapters.PantryAdapter;
import com.readyrecipe.android.models.PantryItem;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.network.SessionManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PantryFragment extends Fragment {
    private PantryAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String userId;
    private SwipeRefreshLayout refreshLayout;
    private View progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pantry, container, false);

        sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getUserId();
        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);

        RecyclerView recyclerView = root.findViewById(R.id.pantryRecycler);
        refreshLayout = root.findViewById(R.id.pantryRefresh);
        progress = root.findViewById(R.id.pantryLoading);
        FloatingActionButton fabAdd = root.findViewById(R.id.pantryFabAdd);
        View btnOpenCamera = root.findViewById(R.id.btnOpenCamera);
        View topBarTitle = root.findViewById(R.id.topBarTitle);

        if (topBarTitle instanceof android.widget.TextView) {
            ((android.widget.TextView) topBarTitle).setText("Pantry");
        }

        adapter = new PantryAdapter(new PantryAdapter.OnPantryItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                if (item != null) {
                    showEditDialog(item);
                }
            }

            @Override
            public void onLongPress(PantryItem item, int position) {
                if (item != null) {
                    showItemActionsDialog(item, position);
                }
            }
        });
        int spacing = getResources().getDimensionPixelSize(R.dimen.space_8);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(this::loadPantryItems);
        fabAdd.setOnClickListener(v -> showAddDialog());
        btnOpenCamera.setOnClickListener(v -> {
            if (getActivity() instanceof BottomNavigationActivity) {
                ((BottomNavigationActivity) getActivity()).openCameraTab();
            }
        });

        loadPantryItems();
        return root;
    }

    private void loadPantryItems() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Login required", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            return;
        }
        setLoading(true);
        Call<List<PantryItem>> call = apiService.getPantryItems(userId);
        call.enqueue(new Callback<List<PantryItem>>() {
            @Override
            public void onResponse(Call<List<PantryItem>> call, Response<List<PantryItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load pantry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PantryItem>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_pantry, null, false);
        EditText nameInput = dialogView.findViewById(R.id.inputName);
        EditText qtyInput = dialogView.findViewById(R.id.inputQuantity);
        EditText unitInput = dialogView.findViewById(R.id.inputUnit);
        EditText categoryInput = dialogView.findViewById(R.id.inputCategory);

        qtyInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Pantry Item")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String qtyText = qtyInput.getText().toString().trim();
                    String unit = unitInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BigDecimal quantity = qtyText.isEmpty() ? BigDecimal.ONE : new BigDecimal(qtyText);
                    addPantryItem(name, quantity, unit.isEmpty() ? "unit" : unit, category.isEmpty() ? "misc" : category);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
    }

    private void addPantryItem(String name, BigDecimal quantity, String unit, String category) {
        PantryItem item = new PantryItem();
        item.setItemName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setCategory(category);
        try {
            if (sessionManager.getUserId() != null) {
                item.setUserId(java.util.UUID.fromString(sessionManager.getUserId()));
            }
        } catch (IllegalArgumentException ignored) {
            // keep userId null if it is not a UUID
        }
        item.setExpiryDate(calculateExpiry(name));
        item.setApproved(true);

        Call<PantryItem> call = apiService.addPantryItem(item);
        call.enqueue(new Callback<PantryItem>() {
            @Override
            public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.addItem(response.body());
                    Toast.makeText(requireContext(), "Saved to pantry", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PantryItem> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePantryItem(PantryItem item, int position) {
        Call<Void> call = apiService.deletePantryItem(item.getId().toString());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.removeAt(position);
                } else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        });
    }

    private void showItemActionsDialog(PantryItem item, int position) {
        String[] options = new String[]{"Edit", "Delete"};
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getItemName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(item);
                    } else if (item.getId() != null) {
                        deletePantryItem(item, position);
                    }
                })
                .show();
    }

    private void showEditDialog(PantryItem sourceItem) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_pantry, null, false);
        EditText nameInput = dialogView.findViewById(R.id.inputName);
        EditText qtyInput = dialogView.findViewById(R.id.inputQuantity);
        EditText unitInput = dialogView.findViewById(R.id.inputUnit);
        EditText categoryInput = dialogView.findViewById(R.id.inputCategory);

        qtyInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        nameInput.setText(sourceItem.getItemName());
        if (sourceItem.getQuantity() != null) {
            qtyInput.setText(sourceItem.getQuantity().stripTrailingZeros().toPlainString());
        }
        unitInput.setText(sourceItem.getUnit());
        categoryInput.setText(sourceItem.getCategory());

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Pantry Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String qtyText = qtyInput.getText().toString().trim();
                    String unit = unitInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PantryItem updated = new PantryItem();
                    updated.setId(sourceItem.getId());
                    updated.setUserId(sourceItem.getUserId());
                    updated.setItemName(name);
                    updated.setQuantity(qtyText.isEmpty() ? BigDecimal.ONE : new BigDecimal(qtyText));
                    updated.setUnit(unit.isEmpty() ? "unit" : unit);
                    updated.setCategory(category.isEmpty() ? "misc" : category);
                    updated.setExpiryDate(sourceItem.getExpiryDate());
                    updated.setDateAdded(sourceItem.getDateAdded());
                    updated.setApproved(sourceItem.isApproved());

                    updatePantryItem(updated);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updatePantryItem(PantryItem item) {
        if (item.getId() == null) {
            Toast.makeText(requireContext(), "Unable to update item", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<PantryItem> call = apiService.updatePantryItem(item.getId().toString(), item);
        call.enqueue(new Callback<PantryItem>() {
            @Override
            public void onResponse(Call<PantryItem> call, Response<PantryItem> response) {
                if (response.isSuccessful()) {
                    loadPantryItems();
                    Toast.makeText(requireContext(), "Pantry item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PantryItem> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateExpiry(String itemName) {
        String lower = itemName.toLowerCase(Locale.getDefault());
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

    private void setLoading(boolean loading) {
        if (progress != null) {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (refreshLayout != null && refreshLayout.isRefreshing() && !loading) {
            refreshLayout.setRefreshing(false);
        }
    }
}
