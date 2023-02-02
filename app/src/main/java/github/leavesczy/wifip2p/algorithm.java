package github.leavesczy.wifip2p;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import java.util.Scanner;

public class algorithm extends AppCompatActivity {

    public String path;
    private Button select;
    private TextView csvText;
    private Button calculate;
    private Button viewdb;
    private Button droptable;
    private TextView regional_progress;
    private TextView total_progress_tv;
    String filename="progress.txt";
    String filepath="ProgressDir";
    DatabaseHelper regiondb;
    private EditText proportion;
    public float current_volume=0;
    private ProgressBar progress_bar;
    private TextView progress_text;
    private float global_proportion=0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algorithm);

        // Create object of database
        regiondb=new DatabaseHelper(this);


        csvText=findViewById(R.id.csvtext);
        select=findViewById(R.id.button);
        calculate=findViewById(R.id.calculate);
        proportion=findViewById(R.id.proportion);
        viewdb=findViewById(R.id.btnViewdb);
        droptable=findViewById(R.id.btnDroptable);
        regional_progress=findViewById(R.id.regionalprogress);
        total_progress_tv=findViewById(R.id.totalprogress);
        progress_bar=findViewById(R.id.progressBar);
        progress_text=findViewById(R.id.progresstext);


        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));

       final Python py = Python.getInstance();


        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PyObject pyobj = py.getModule("algorithm");
                PyObject obj = pyobj.callAttr("main",path);
                String c_volume=obj.toString();

                current_volume=Float.valueOf(c_volume);
                csvText.setText(String.format("%.4f", current_volume));

                float avg_look_back=0;
                float relative_progress=0;
                float total_progress=0;
                int count=regiondb.counter();
                boolean insertion=false;



                float proportion_val = Float.parseFloat(proportion.getText().toString());
                global_proportion=proportion_val;
//                boolean insertion=regiondb.addData((float)26.3,proportion_val);
//                Cursor avg_look_back_cursor=regiondb.get_avg_lookback();

                 if(count==1)
                {

                    insertion=regiondb.addData(current_volume,proportion_val);
                    Cursor avg_look_back_cursor=regiondb.get_avg_lookback();
                    relative_progress = current_volume;
                    total_progress = total_progress(relative_progress, proportion_val);

//                    regional_progress.setText(Float.toString(relative_progress));
//                    total_progress_tv.setText(Float.toString(total_progress));

                    Toast.makeText(algorithm.this, "Data Found "+count, Toast.LENGTH_LONG).show();

                }
                else {
                    Cursor avg_look_back_cursor=regiondb.get_avg_lookback();

                    if (avg_look_back_cursor.getCount() == 0) {
                        Toast.makeText(algorithm.this, "No Data Found", Toast.LENGTH_LONG).show();

                    } else {
                        if (avg_look_back_cursor.moveToNext()) {

                            avg_look_back = avg_look_back_cursor.getFloat(0);
                            insertion=regiondb.addData((current_volume+avg_look_back),proportion_val);
                            relative_progress = relative_progress((avg_look_back/count));
                            total_progress = total_progress(relative_progress, proportion_val);



                            Toast.makeText(algorithm.this, "Data Found ", Toast.LENGTH_LONG).show();

                        }
                    }
                }
//                updateprogressBar(total_progress);

                if(insertion==true)
                {
                    Toast.makeText(algorithm.this,"Data Successfully Inserted",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(algorithm.this,"Data insertion failed",Toast.LENGTH_LONG).show();
                }

                //Progress is being recorded in to the text file

                if(ExternalStorageAvailable()) {
                    if (total_progress != 0) {
                        // File myExternalFile = new File(getExternalFilesDir(filepath), filename);


                        File myExternalFile = new File("/storage/emulated/0/Download", filename);


                        if (!myExternalFile.exists()) {
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(myExternalFile);
                                String data=String.format("%.2f", total_progress)+","+getMAC()+"@"+String.format("%.2f", relative_progress);
                                fos.write(data.getBytes());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(algorithm.this, "Text File Created", Toast.LENGTH_LONG).show();
                        }
                        else {
                            FileReader fr=null;
                            File myexternalfile=new File(getExternalFilesDir("/storage/emulated/0/Download"),"progress.txt");
                            StringBuilder stringbuilder =new StringBuilder();
                            FileOutputStream fos = null;
                            try{
                                fr=new FileReader(myExternalFile);
                                BufferedReader br=new BufferedReader(fr);
                                String line=br.readLine();
//                                if(line!=null)
//                                {
//                                    float old_progress=Float.valueOf(line);
//                                    total_progress+=old_progress;
//                                    fos = new FileOutputStream(myExternalFile);
//                                    fos.write(Float.toString(total_progress).getBytes());
//                                }

                                while(line!=null)
                                {
                                    stringbuilder.append(line);
                                     line=br.readLine();
                                }
                                String text_data=stringbuilder.toString();

                                char[] prgress=text_data.toCharArray();
                                int index=0;
                                String extracted_total_progress="";
                                while(prgress[index]!=',') {

                                        extracted_total_progress+=prgress[index];
                                        index++;
                                }
                                float old_progress=Float.valueOf(extracted_total_progress);
                                total_progress+=old_progress;
                                String data=","+getMAC()+"@"+String.format("%.2f", relative_progress);
                                String result = String.format("%.2f", total_progress)+text_data.substring(index)+data;
                                fos = new FileOutputStream(myExternalFile);
                                fos.write(result.getBytes());

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            Toast.makeText(algorithm.this, "File Updated", Toast.LENGTH_LONG).show();
                        }

                    }
                    regional_progress.setText(Float.toString(relative_progress));
                    total_progress_tv.setText(Float.toString(total_progress));
                    updateprogressBar(total_progress);
                }
            }
        });


        droptable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regiondb.Emptytable();
               regiondb.addData((float)0,global_proportion);

                Toast.makeText(algorithm.this, "Data Deleted Successfully", Toast.LENGTH_LONG).show();
            }
        });


        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imp step
                if(SDK_INT >= Build.VERSION_CODES.R)
                {
                    if(Environment.isExternalStorageManager()){
                        //choosing csv file
                        Intent intent=new Intent();
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE,true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select CSV File "),101);
                    }
                    else{
                        //getting permission from user
                        Intent intent=new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri=Uri.fromParts("package",getPackageName(),null);
                        startActivity(intent);
                    }
                }
                else{
                    // for below android 11

                    Intent intent=new Intent();
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE,true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    ActivityCompat.requestPermissions(algorithm.this,new String[] {WRITE_EXTERNAL_STORAGE},102);


                }
            }
        });


        viewdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor data= regiondb.ShowData();

                if(data.getCount()==0)
                {
                    display("Error","No Data Found");
                }
                StringBuffer buffer=new StringBuffer();
                while(data.moveToNext())
                {
                    buffer.append("COUNTER "+ data.getString(0)+"\n");
                    buffer.append("AVGLOOKBACK "+ data.getString(1)+"\n");
                    buffer.append("PROPORTION "+ data.getString(2)+"\n");


                }
                display("All Stored Data", buffer.toString());
            }
        });


    }

    private boolean ExternalStorageAvailable() {
        String External_storage_state=Environment.getExternalStorageState();
        if(External_storage_state.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }


    public void display(String title,String message)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }

    Uri fileuri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101 && data!=null){
            fileuri=data.getData();
            // csvText.setText(readCSVFile(getFilePathFromUri(fileuri)));
            path= getFilePathFromUri(fileuri);
            //csvText.setText(path);
        }
    }


    // this method is used for getting file path from uri
    public String getFilePathFromUri(Uri uri){
        String[] filename1;
        String fn;
        String filepath=uri.getPath();
        String filePath1[]=filepath.split(":");
        filename1 =filepath.split("/");
        fn=filename1[filename1.length-1];
        return Environment.getExternalStorageDirectory().getPath()+"/"+filePath1[1];
    }

    //reading file data

    public String readCSVFile(String path){
        String filedata = null;
        File file=new File(path);
        try {

            Scanner scanner=new Scanner(file);
            while (scanner.hasNextLine()){

                String line=scanner.nextLine();
                String [] splited=line.split(",");
                String row="";
                for (String s:splited){

                    row=row+s+"  ";

                }

                filedata=filedata+row+"\n";

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(algorithm.this,"Error",Toast.LENGTH_SHORT).show();
        }

        return filedata;

    }

    public float relative_progress(float avg_look_back)
    {
        float relative_progress=current_volume/avg_look_back;

        return relative_progress;
    }
    public float total_progress(float relativ_progress,float proportion)
    {
        return relativ_progress*(proportion/100);
    }

    private void updateprogressBar(float progress)
    {
        progress_bar.setProgress((int)progress);
        progress_text.setText(String.format("%.2f", progress)+" %");

    }
    public String getMAC()
    {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();


        return macAddress;
    }



}
