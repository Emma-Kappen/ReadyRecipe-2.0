package com.readyrecipe.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.readyrecipe.android.models.LoginRequest;
import com.readyrecipe.android.models.LoginResponse;
import com.readyrecipe.android.network.ApiClient;
import com.readyrecipe.android.network.ApiService;
import com.readyrecipe.android.BottomNavigationActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        sharedPreferences = getSharedPreferences("ReadyRecipePrefs", MODE_PRIVATE);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog = ProgressDialog.show(this, "Authenticating...", "Please wait", true);
            authenticate(email, password);
        });

        Button signUpLink = findViewById(R.id.buttonSignUpLink);
        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // In debug builds pre-fill and optionally auto-submit credentials to speed emulator testing
        DebugLoginHelper.enableDebugAutoLogin(this, emailEditText, passwordEditText, loginButton);
    }

    private void authenticate(String email, String password) {
        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    sharedPreferences.edit()
                            .putString("jwt", response.body().getToken())
                            .putString("userId", response.body().getUserId())
                            .apply();
                    Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = "Invalid credentials";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                String message = "Request failed: " + t.getMessage();
                if (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException) {
                    message = "Cannot reach backend. Start it at " + BuildConfig.BASE_URL;
                }
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
