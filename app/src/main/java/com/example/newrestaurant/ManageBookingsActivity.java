package com.example.newrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManageBookingsActivity extends AppCompatActivity {

    private ListView bookingsListView;
    private Button updateBookingButton;
    private Button cancelBookingButton;

    private List<String> bookings;
    private List<JSONObject> bookingData;
    private int selectedBookingIndex = -1;

    private static final int REQUEST_CODE_UPDATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bookings);

        bookingsListView = findViewById(R.id.bookingsListView);
        updateBookingButton = findViewById(R.id.updateBookingButton);
        cancelBookingButton = findViewById(R.id.cancelBookingButton);

        bookings = new ArrayList<>();
        bookingData = new ArrayList<>();
        retrieveBookings(); // Fetch bookings from server

        bookingsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        bookingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBookingIndex = position;
            }
        });

        updateBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBookingIndex == -1) {
                    Toast.makeText(ManageBookingsActivity.this, "Please select a booking to update", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject selectedBooking = bookingData.get(selectedBookingIndex);
                    Intent intent = new Intent(ManageBookingsActivity.this, UpdateReservation.class);
                    intent.putExtra("bookingData", selectedBooking.toString());
                    startActivityForResult(intent, REQUEST_CODE_UPDATE);
                }
            }
        });

        cancelBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBooking();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK) {
            // Refresh bookings after update
            retrieveBookings();
        }
    }

    private void retrieveBookings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://192.168.1.113:8080/shipcampus/manage_booking.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();

                        // Parse JSON response
                        JSONObject jsonResponse = new JSONObject(result.toString());
                        JSONArray reservations = jsonResponse.getJSONArray("data");

                        final List<String> bookingList = new ArrayList<>();
                        bookingData.clear();
                        for (int i = 0; i < reservations.length(); i++) {
                            JSONObject reservation = reservations.getJSONObject(i);
                            String bookingDetail = "Booking " + reservation.getString("id") + " - Date: " + reservation.getString("reservation_time");
                            bookingList.add(bookingDetail);
                            bookingData.add(reservation);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ManageBookingsActivity.this, android.R.layout.simple_list_item_single_choice, bookingList);
                                bookingsListView.setAdapter(adapter);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManageBookingsActivity.this, "Failed to retrieve bookings", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ManageBookingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void cancelBooking() {
        if (selectedBookingIndex == -1) {
            Toast.makeText(ManageBookingsActivity.this, "Please select a booking to cancel", Toast.LENGTH_SHORT).show();
            return;
        }

        final JSONObject selectedBooking = bookingData.get(selectedBookingIndex);

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

                    String postData = "booking_id=" + selectedBooking.getString("id") + "&action=cancel";
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookings.remove(selectedBookingIndex);
                                bookingData.remove(selectedBookingIndex);
                                selectedBookingIndex = -1;
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(ManageBookingsActivity.this, android.R.layout.simple_list_item_single_choice, bookings);
                                bookingsListView.setAdapter(adapter);
                                Toast.makeText(ManageBookingsActivity.this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManageBookingsActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ManageBookingsActivity.this, "Cancel Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
