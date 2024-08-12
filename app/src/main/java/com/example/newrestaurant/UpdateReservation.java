package com.example.newrestaurant;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateReservation extends AppCompatActivity {

    private EditText reservationDate;
    private EditText reservationTime;
    private Button submitReservationButton;

    private JSONObject bookingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_reservation);

        reservationDate = findViewById(R.id.reservationDate);
        reservationTime = findViewById(R.id.reservationTime);
        submitReservationButton = findViewById(R.id.submitReservationButton);

        try {
            String bookingDataString = getIntent().getStringExtra("bookingData");
            bookingData = new JSONObject(bookingDataString);
            String currentDateTime = bookingData.getString("reservation_time");
            String[] dateTimeParts = currentDateTime.split(" ");
            reservationDate.setText(dateTimeParts[0]);
            reservationTime.setText(dateTimeParts[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load booking data", Toast.LENGTH_SHORT).show();
        }

        submitReservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReservation();
            }
        });
    }

    private void updateReservation() {
        final String newDate = reservationDate.getText().toString();
        final String newTime = reservationTime.getText().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://192.168.1.113:8080/shipcampus/manage_booking.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "booking_id=" + bookingData.getString("id") + "&action=update&updateDateTime=" + newDate + " " + newTime;
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UpdateReservation.this, "Booking updated successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK); // Indicate successful update
                                finish(); // Close activity after successful update
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UpdateReservation.this, "Failed to update booking", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UpdateReservation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
