package github.leavesczy.wifip2p;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Monitor_Progress extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;

    public String filename="progress.txt";
    private ProgressBar progress_bar;
    private TextView tvProgress;
    ArrayList<String> device_address=new ArrayList<>();
    ArrayList<String> device_progress=new ArrayList<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_progress);





        spinner=findViewById(R.id.spinner);

        progress_bar=findViewById(R.id.progressBar2);
        tvProgress=findViewById(R.id.tvProgress);


        if (ExternalStorageAvailable()) {

            File myExternalFile = new File("/storage/emulated/0/Download", filename);

            if (!myExternalFile.exists()) {

                Toast.makeText(Monitor_Progress.this, "'progress' file is not Available", Toast.LENGTH_LONG).show();
            }

            else {

                FileReader fr = null;
                File myexternalfile = new File(getExternalFilesDir("/storage/emulated/0/Download"), "progress.txt");
                StringBuilder stringbuilder = new StringBuilder();
                FileOutputStream fos = null;
                try {
                    fr = new FileReader(myExternalFile);
                    BufferedReader br = new BufferedReader(fr);
                    String line = br.readLine();

                    while (line != null) {
                        stringbuilder.append(line);
                        line = br.readLine();
                    }
                    String text_data = stringbuilder.toString();
                    //Storing values in the array list

                    char[] progress=text_data.toCharArray();
                    int index=0;
                    String extracted_total_progress="";
//                    //getting total progress
                    while (progress[index] != ',') {

                        extracted_total_progress += progress[index];
                        index++;
                    }
                    device_address.add("Total Progress");
                    device_progress.add(extracted_total_progress);

                    char[] rest_progress =text_data.substring(index+1).toCharArray();

                    String temp_progress="";
                    String temp_address="";
                    int index2=0;

                    while(index2<rest_progress.length) {

                        if(index2==rest_progress.length || rest_progress[index2]==',')
                        {
                            device_progress.add(temp_progress);
                            device_address.add(temp_address);
                            temp_progress="";
                            temp_address="";
                            index2++;

                        }

                        else if(rest_progress[index2]=='@')
                        {   index2++;
                            while (index2<rest_progress.length && rest_progress[index2] != ',') {

                                temp_progress += rest_progress[index2];

                                index2++;
                            }
                        }
                        else if(index2==rest_progress.length)
                        {
                            device_progress.add(temp_progress);
                            device_address.add(temp_address);
                            break;
                        }
                        else
                       {


                               temp_address+=rest_progress[index2];
                               index2++;


                       }


                    }
                    if(!temp_progress.equals("") && !temp_address.equals("") )
                    {
                        device_progress.add(temp_progress);
                        device_address.add(temp_address);
                        temp_progress="";
                        temp_address="";
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }


        }

       // Create an spinner with items
        ArrayAdapter<String> aa=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,device_address);
        spinner.setAdapter(aa);
        spinner.setOnItemSelectedListener(this);





//        btnViewProgress.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });


    }

    public String getMAC()
    {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    private boolean ExternalStorageAvailable() {
        String External_storage_state= Environment.getExternalStorageState();
        if(External_storage_state.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }
    private void getdata() {


        }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int index=parent.getSelectedItemPosition();
        String region_progress_string=device_progress.get(index);
        float region_progress_float= Float.parseFloat(region_progress_string);
        updateprogressBar(region_progress_float);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(Monitor_Progress.this, "Nothing Selected", Toast.LENGTH_LONG).show();
    }
    private void updateprogressBar(float progress)
    {
        progress_bar.setProgress((int)progress);
        tvProgress.setText(String.format("%.2f", progress)+" %");

    }
}



