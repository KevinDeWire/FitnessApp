package com.example.fitnessapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.CalendarView;
import android.widget.Toast;

public class Schedule extends AppCompatActivity implements CalendarView.OnDateChangeListener {

    private CalendarView mCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCalendarView = findViewById(R.id.calendarView);

        mCalendarView.setFirstDayOfWeek(2);

        mCalendarView.setOnDateChangeListener(this);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month,
                                    int dayOfMonth) {
        String date = month + "/" + dayOfMonth + "/" + year;
        Toast.makeText(this, date, Toast.LENGTH_SHORT).show();
    }
}