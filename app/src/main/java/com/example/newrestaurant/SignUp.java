package com.example.newrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;

public class SignUp extends AppCompatActivity {

    EditText username, password, repassword;
    Button signin, register;
    private static final String SIGNUP_URL = "http://192.168.1.113:8080/shipcampus/signup.php"; // Update with your local IP

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        repassword = findViewById(R.id.repassword);
        signin = findViewById(R.id.btnsignin);
        register = findViewById(R.id.btnregister);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                String repass = repassword.getText().toString();

                if (user.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(repass)) {
                        // Start the registration process in a new thread
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(SIGNUP_URL);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("POST");
                                    connection.setDoOutput(true);
                                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                                    String postData = "username=" + URLEncoder.encode(user, "UTF-8") +
                                            "&password=" + URLEncoder.encode(pass, "UTF-8");

                                    OutputStream outputStream = connection.getOutputStream();
                                    outputStream.write(postData.getBytes("UTF-8"));
                                    outputStream.flush();
                                    outputStream.close();

                                    int responseCode = connection.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                        StringBuilder response = new StringBuilder();
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            response.append(line);
                                        }
                                        reader.close();

                                        JSONObject jsonResponse = new JSONObject(response.toString());
                                        final String status = jsonResponse.getString("status");
                                        final String message = jsonResponse.getString("message");

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if ("success".equals(status)) {
                                                    Toast.makeText(SignUp.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                                                    // Navigate back to the LoginActivity
                                                    Intent intent = new Intent(SignUp.this, LoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish(); // Finish the current activity

                                                } else {
                                                    Toast.makeText(SignUp.this, "Registration failed: " + message, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SignUp.this, "Registration failed: Server error", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    connection.disconnect();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(SignUp.this, "Error connecting to server", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the LoginActivity
                Intent intent = new Intent(SignUp.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
