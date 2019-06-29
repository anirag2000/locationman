package com.example.anirudh.locationman;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Telephony.Mms.Part.FILENAME;


public class DTWjava extends AppCompatActivity  implements SensorEventListener {
    private SensorManager sensorManager;
    String reference;
    int count=0;
    Uri uri;
    int status=0;
static double pressure;
String serverurl="https://path2pressure-244816.appspot.com";
int postcount=1;
String dtwmp;
static String m_Text;
int referencecount=0;
String reference_multiple="";
    String inputref;
    int countref = 0;
    String realtimevalue="0\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtw);
        ProgressBar  progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        Button select=findViewById(R.id.selectfile);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the name of the file");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DTWjava.m_Text=input.getText().toString();
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(DTWjava.this,Home.class);
                startActivity(intent);
            }
        });

        builder.show();
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);

        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
showFileChooser();
            }
        });




        Button upload=findViewById(R.id.uploadfile);
upload.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
addreference();
    }
});

        Button start=findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri.toString().isEmpty())
                {
                    Toast.makeText(DTWjava.this,"PLEASE UPLOAD REFERENCE",Toast.LENGTH_LONG).show();
                }
                else
                {
                comparedata();
            }}
        });



    }
    void comparedata()
    {


        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.w("run", "run" );

                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    while(status==0) {
                        try {
                            if (status == 0) {


                                Button button = findViewById(R.id.stop);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        status = 1;
                                    }
                                });

                                realtimevalue=realtimevalue+Double.toString(pressure)+"\n";
                                if (postcount % 8 == 0) {

                                    Log.w("run", "run: " );
                                    //constants
                                    URL url = new URL(serverurl + "/postrealtime");
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("filename", m_Text);


                                    jsonObject.put("pressure", realtimevalue);
                                    String message = jsonObject.toString();

                                    conn = (HttpURLConnection) url.openConnection();
                                    conn.setReadTimeout(10000 /*milliseconds*/);
                                    conn.setConnectTimeout(15000 /* milliseconds */);
                                    conn.setRequestMethod("POST");
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);
                                    conn.setFixedLengthStreamingMode(message.getBytes().length);

                                    //make some HTTP header nicety
                                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                                    //open
                                    conn.connect();

                                    //setup send
                                    os = new BufferedOutputStream(conn.getOutputStream());
                                    os.write(message.getBytes());
                                    // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                                    //Log.w("MESSAGE", message);
                                    //clean up
                                    os.flush();


                                    //do somehting with response
                                    is = conn.getInputStream();
                                    try {


                                        StringBuffer sb = new StringBuffer();
                                        int chr;
                                        while ((chr = is.read()) != -1) {
                                            sb.append((char) chr);
                                        }
                                        String reply = sb.toString();
                                        if (reply.equalsIgnoreCase("0")) {

                                        } else {
                                            dtwmp = reply;
                                        }
                                        TextView textView = findViewById(R.id.dtwresult);
                                        textView.setText(dtwmp);

                                    } finally {
                                        is.close();
                                    }
                                    realtimevalue="0\n";
                                }

                            }
                            else {
                                conn.disconnect();
                                break;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.w("warning", e.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.w("warning", e.toString());
                        }
                        postcount=postcount+1;
                    }


                    }


        }, 0, 100);

    }

    void addreference() {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);


        Toast.makeText(DTWjava.this, "STARTED", Toast.LENGTH_LONG).show();
        count = 0;

        checkfile();

        Thread thread = new Thread() {
            @Override
            public void run() {

                // Do your task

        try {

            CSVReader reader = new CSVReader(new FileReader(uri.getLastPathSegment().substring(5)));
            String[] nextLine;
            referencecount=0;
            while ((nextLine = reader.readNext()) != null && count!=2) {

                // nextLine[] is an array of values from the line
                reference=nextLine[10];
                if(reference.equalsIgnoreCase("Pressure"))
                {
                    count+=1;
                    if(count==2)
                    {
                        break;
                    }
                }
                if(referencecount%80==0 ) {


                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl + "/postreference");
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("filename", m_Text);
                        jsonObject.put("reference", reference_multiple);
                        Log.w("lol", reference_multiple);

                        String message = jsonObject.toString();

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000 /*milliseconds*/);
                        conn.setConnectTimeout(15000 /* milliseconds */);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setFixedLengthStreamingMode(message.getBytes().length);

                        //make some HTTP header nicety
                        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                        //conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                        //  is.close();

                        //open
                        conn.connect();

                        //setup send
                        os = new BufferedOutputStream(conn.getOutputStream());
                        os.write(message.getBytes());
                        // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                        //Log.w("MESSAGE", message);
                        //clean up
                        os.flush();

                        //do somehting with response
                        is = conn.getInputStream();
                        StringBuffer sb = new StringBuffer();
                        try {
                            int chr;
                            while ((chr = is.read()) != -1) {
                                sb.append((char) chr);
                            }
                            String reply = sb.toString();
                            Log.w("RESULY", reply);
                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.w("warning", e.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("warning", e.toString());
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                reference_multiple="";
                }


                else
                {
                    reference_multiple=reference_multiple+reference+"\n";
                }
                referencecount=referencecount+1;

            }
        }
        catch (IOException e)
        {

        }
            }
        };

        thread.start();

        progressBar.setVisibility(View.INVISIBLE);
    }


    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                     uri = data.getData();
                    //Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path

                    TextView display=findViewById(R.id.filename);
                    display.setText(uri.getLastPathSegment().substring(5));
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    void checkfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    OutputStream os = null;
                    InputStream is = null;
                    HttpURLConnection conn = null;
                    try {
                        //constants
                        URL url = new URL(serverurl+"/checkfile");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("filename", m_Text);
                       
                        Log.w("lol", " " + "lol");

                        String message = jsonObject.toString();

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000 /*milliseconds*/);
                        conn.setConnectTimeout(15000 /* milliseconds */);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setFixedLengthStreamingMode(message.getBytes().length);

                        //make some HTTP header nicety
                        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                        //open
                        conn.connect();

                        //setup send
                        os = new BufferedOutputStream(conn.getOutputStream());
                        os.write(message.getBytes());
                        // Toast.makeText(DTWjava.this,message,Toast.LENGTH_LONG).show();
                        //Log.w("MESSAGE", message);
                        //clean up
                        os.flush();

                        //do somehting with response

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("warningfile", " " + e.toString());
                    } finally {

                        conn.disconnect();
                    }
                } catch (Exception e1) {
                    Log.w("warningfile", " " + e1.toString());

                }


            }
        }).start();
    }
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()==Sensor.TYPE_PRESSURE){
            Log.w("pressure", Double.toString(DTWjava.pressure) );
            DTWjava.pressure=event.values[0];
            TextView pressuretext=findViewById(R.id.displaypressureit);
            pressuretext.setText(Double.toString(pressure));

        }
    }






    }










