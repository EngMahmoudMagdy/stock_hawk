package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils.XAxisDateFormatter;
import com.udacity.stockhawk.Utils.YAxisPriceFormatter;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailsActivity extends AppCompatActivity {


        @BindView(R.id.line_chart)
    LineChart lineChart;
        @BindView(R.id.stock_name)
    TextView mNameView;
        @BindView(R.id.stock_symbol)
    TextView mSymbolView;
        @BindView(R.id.stock_bidprice)
    TextView mEbitdaView;
        @BindView(R.id.stock_change)
    TextView mChange;

    private String mStockSymbol , mStockHistory,mStockName;
    private float mPrice , mPercent , mAbs ;
    List<Entry> entries ;
    Float referenceTime ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Intent intent = getIntent();
        mStockSymbol = intent.getStringExtra(Contract.Quote.COLUMN_SYMBOL);
        mStockHistory = intent.getStringExtra(Contract.Quote.COLUMN_HISTORY);
        mStockName = intent.getStringExtra(Contract.Quote.COLUMN_NAME);
        mPrice = intent.getFloatExtra(Contract.Quote.COLUMN_PRICE,0);
        mPercent = intent.getFloatExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE,0);
        mAbs = intent.getFloatExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE,0);

        mNameView.setText(mStockName);
        mChange.setText(String.format(Locale.getDefault(),"%.2f", mPrice));
        mEbitdaView.setText(String.format(Locale.getDefault(),"%.2f (%c %.2f)", mAbs,'%', mPercent));
        mSymbolView.setText(mStockSymbol);

        splitdata(5);
        if (mStockHistory != null)
            setUpLineChart();
    }

    void splitdata(int lastElement )
    {
        entries = new ArrayList<>();
        String[] historyrow = mStockHistory.split("\\r?\\n");
        String s[] = historyrow[lastElement].split(",");
        referenceTime  = Float.valueOf(s[0]);

        for (int i = lastElement; i >=0; i--) {
            String split[] = historyrow[i].split(",");
            entries.add(new Entry(Float.valueOf(split[0])-referenceTime,Float.valueOf(split[1])));
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpLineChart() {

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setHighLightColor(Color.WHITE);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter("MM-dd", referenceTime));

        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(true);
        yAxis.setAxisLineColor(Color.WHITE);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(12f);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);


        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragDecelerationEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setGridBackgroundColor(R.color.white);
        Description description = new Description();
        description.setText(" ");
        lineChart.setDescription(description);
        lineChart.setExtraOffsets(10, 0, 0, 10);
        lineChart.animateX(1500, Easing.EasingOption.Linear);
        // TODO: 1/7/2017 Raise issue on MPAndroidChart to add accessibility for chart elements
    }






}
