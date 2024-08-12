package com.example.newrestaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {

    private Button bookReservationButton;
    private Button checkEventsButton;
    private Button manageBookingsButton;
    private Button writeReviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        bookReservationButton = findViewById(R.id.bookReservationButton);
        checkEventsButton = findViewById(R.id.checkEventsButton);
        manageBookingsButton = findViewById(R.id.manageBookingsButton);
        writeReviewButton = findViewById(R.id.writeReviewButton);

        bookReservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ReservationActivity.class);
                startActivity(intent);
            }
        });

        checkEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, EventActivity.class);
                startActivity(intent);
            }
        });

        manageBookingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ManageBookingsActivity.class);
                startActivity(intent);
            }
        });
        writeReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dashboard.this, ReviewActivity.class);
                startActivity(intent);
            }
        });
    }
}
