package com.example.fitnessapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import static com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ChartView> {

    private ArrayList<ChartData> chartData;

    class ChartView extends RecyclerView.ViewHolder {

        TextView exerciseNameTextView;
        LineChart lineChart;

        ChartView(@NonNull View chartView) {
            super(chartView);

            exerciseNameTextView = chartView.findViewById(R.id.textViewChartExerciseName);
            lineChart = chartView.findViewById(R.id.chart);
        }
    }

    ChartAdapter(ArrayList<ChartData> chartDataList){
        chartData = chartDataList;
    }

    @NonNull
    @Override
    public ChartView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chartItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_card_view, parent, false);
        return new ChartView(chartItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartView holder, final int position) {
        holder.exerciseNameTextView.setText(chartData.get(position).getExerciseName());
        LineData lineData = new LineData(getDataSet(position));
        holder.lineChart.setData(lineData);
        final ArrayList<String> dates = (ArrayList<String>) chartData.get(position).getDates();
        holder.lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(){
            @Override
            public String getFormattedValue(float value){
                int index = (int)value;
                return dates.get(index);
            }
        });
        holder.lineChart.getXAxis().setLabelRotationAngle(-45f);
        holder.lineChart.getXAxis().setPosition(BOTTOM);
        holder.lineChart.getDescription().setEnabled(false);
        holder.lineChart.getLegend().setEnabled(false);
        holder.lineChart.invalidate();
    }

    @Override
    public int getItemCount(){
        return chartData.size();
    }

    private ArrayList<ILineDataSet> getDataSet(int position){
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        LineDataSet set1;
        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<String> dates = (ArrayList<String>) chartData.get(position).getDates();
        ArrayList<Double> weights = (ArrayList<Double>) chartData.get(position).getWeights();
        for (int i=0; i<dates.size(); i++){
            values.add(new Entry(i, weights.get(i).floatValue()));
        }
        set1 = new LineDataSet(values, chartData.get(position).getExerciseName());
        set1.setColor(Color.DKGRAY);
        set1.setCircleColor(Color.DKGRAY);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        dataSets.add(set1);
        return dataSets;
    }

    void updateData(ArrayList<ChartData> data) {
        chartData.clear();
        chartData = data;
        notifyDataSetChanged();
    }

}
