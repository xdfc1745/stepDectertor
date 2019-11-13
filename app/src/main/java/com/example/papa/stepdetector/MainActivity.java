package com.example.papa.stepdetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView xText, yText, zText;
    private int i = 0;
    private final String TAG = "GraphSensors";
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    //private Double svmValue; // svm값 저장
    private LinearLayout layout;
    private SensorManager mSensorManager, wSensorManager, sensorManager;
    private Sensor mSensor,stepDetectorSensor;
    MyDBHelper m_helper;
    //private GraphView line_graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_helper = new MyDBHelper(this, "accelerometer.db", null, 1);
        xText = (TextView) findViewById(R.id.xValue);
        yText = (TextView) findViewById(R.id.yValue);
        zText = (TextView) findViewById(R.id.zValue);


        mSeriesAccelX = initSeries(Color.BLUE, "X");
        mSeriesAccelY = initSeries(Color.RED, "Y");
        mSeriesAccelZ = initSeries(Color.GREEN, "Z");

        GraphView initGraph;
        mGraphAccel = initGraph(R.id.xyzgraph, "X, Y, Z direction Acceleration");

        mGraphAccel.addSeries(mSeriesAccelX);
        mGraphAccel.addSeries(mSeriesAccelY);
        mGraphAccel.addSeries(mSeriesAccelZ);

        //mGraphAccel = initGraph(R.id.svm, "svm value");
        //mGraphAccel.addSeries(mSeriesAccelA);

        startAccel();

    }

        GraphView initGraph( int id, String title){
            GraphView graph = (GraphView) findViewById(id);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(5);
            graph.getGridLabelRenderer().setLabelVerticalWidth(100);
            graph.setTitle(title);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            return graph;
        }

        public void startAccel () {
            mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            wSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            stepDetectorSensor = wSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            wSensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (stepDetectorSensor == null) {
                Log.d(TAG, "stepN :" + "NO!");

            }
        }

        public LineGraphSeries<DataPoint> initSeries ( int color, String title){
            LineGraphSeries<DataPoint> series;
            series = new LineGraphSeries<>();
            series.setDrawDataPoints(true);
            series.setDrawBackground(false);
            series.setColor(color);
            series.setTitle(title);
            return series;
        }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x, y, z;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        xText.setText("X: " + x);
        yText.setText("Y: " + y);
        zText.setText("Z: " + z);

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    Log.d(TAG, "stepN :" + "true");
                    Log.d(TAG, "Data received: " + x + "," + y + "," + z);
        }

        graphLastAccelXValue += 0.05d;

        mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue, x), true, 100);
        mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue, y), true, 100);
        mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue, z), true, 100);

        SQLiteDatabase db = m_helper.getWritableDatabase();
        String sql = String.format("INSERT INTO accelerometer VALUES('%lf', '%lf', '%lf', '%d', '%lf')", x, y, z, 0, 0);//ms step값 필요
        db.execSQL(sql);
        db.close();


        //svmValue = Math.sqrt(x * x + y * y + z * z);
        //mSeriesAccelA.appendData(new DataPoint(graphLastAccelXValue, svmValue), true, 100);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

class MyDBHelper extends SQLiteOpenHelper {
    public MyDBHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE accelerometer (x DOUBLE, y DOUBLE, z DOUBLE, step INT, ms LONG PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}


