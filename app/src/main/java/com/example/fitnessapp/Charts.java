package com.example.fitnessapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class Charts extends AppCompatActivity implements View.OnClickListener{

    RecyclerView chartRecyclerView;
    RecyclerView.LayoutManager chartRecyclerViewLayoutManager;
    ChartAdapter mAdapter;
    LinearLayoutManager HorizontalLayout;
    SnapHelper snapHelper = new LinearSnapHelper();
    Button mLast7DaysButton, mLast30DaysButton;
    FitnessRoomDatabase db;
    StepCountDao mStepCountDao;
    ActiveTimeDao mActiveTimeDao;
    ExerciseSetsDao mExerciseSetsDao;
    ArrayList<ChartData> mChartData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        chartRecyclerView = findViewById(R.id.recyclerViewCharts);
        mLast7DaysButton = findViewById(R.id.buttonLast7Days);
        mLast30DaysButton = findViewById(R.id.buttonLast30Days);

        RecyclerViewSetup();

        mLast7DaysButton.setOnClickListener(this);
        mLast30DaysButton.setOnClickListener(this);

        db = FitnessRoomDatabase.getDatabase(this);
        mStepCountDao = db.stepCountDao();
        mActiveTimeDao = db.activeTimeDao();
        mExerciseSetsDao = db.exerciseSetsDao();

    }

    public void RecyclerViewSetup(){
        chartRecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        chartRecyclerView.setLayoutManager(chartRecyclerViewLayoutManager);
        mAdapter = new ChartAdapter(mChartData);
        HorizontalLayout = new LinearLayoutManager(Charts.this, LinearLayoutManager.HORIZONTAL, false);
        chartRecyclerView.setLayoutManager(HorizontalLayout);
        chartRecyclerView.setAdapter(mAdapter);
        snapHelper.attachToRecyclerView(chartRecyclerView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLast7Days:
                Last7Days();
                break;
            case R.id.buttonLast30Days:
                Last30Days();
                break;
        }
    }

    private void Last7Days(){
        RecyclerViewSetup();
        LocalDate dateMinus7;
        dateMinus7 = LocalDate.now().minusDays(7);
        mChartData = GetChartData(dateMinus7.toString());
        mAdapter.updateData(mChartData);
    }

    private void Last30Days(){
        RecyclerViewSetup();
        LocalDate dateMinus30;
        dateMinus30 = LocalDate.now().minusDays(30);
        mChartData = GetChartData(dateMinus30.toString());
        mAdapter.updateData(mChartData);
    }

    private ArrayList<ChartData> GetChartData(String date) {
        ArrayList<ChartData> chartDataArrayList = new ArrayList<>();
        ChartData chartData;

        //Get Step Count Data
        chartData = new ChartData();
        chartData.setExerciseName("Step Count");
        chartData.setDates(mStepCountDao.LastXDays(date));
        List<Integer> intSteps = mStepCountDao.LastXTotalSteps(date);
        List<Double> douSteps = new ArrayList<>();
        for (int i:intSteps) {douSteps.add(intSteps.indexOf(i), (double)i);}
        chartData.setWeights(douSteps);
        chartDataArrayList.add(chartData);

        //Get Active Time Data
        chartData = new ChartData();
        chartData.setExerciseName("Active Time");
        chartData.setDates(mActiveTimeDao.LastXDays(date));
        List<Integer> intActive = mActiveTimeDao.LastXTotalTime(date);
        List<Double> douActive = new ArrayList<>();
        for (int i : intActive){douActive.add(intActive.indexOf(i), (double)i/60);}
        chartData.setWeights(douActive);
        chartDataArrayList.add(chartData);

        //Get Exercise Data
        List<String> exercises = mExerciseSetsDao.ChartExercises(date);
        for (String exercise : exercises) {
            chartData = new ChartData();
            chartData.setExerciseName(exercise);
            List<String> dates = mExerciseSetsDao.ChartDates(exercise, date);
            chartData.setDates(dates);
            List<Double> maxWeights = new ArrayList<>();
            for (String d : dates) {
                maxWeights.add(dates.indexOf(d), mExerciseSetsDao.ChartWeights(d, exercise));
            }
            chartData.setWeights(maxWeights);
            chartDataArrayList.add(chartData);
        }

        return chartDataArrayList;
    }
}
