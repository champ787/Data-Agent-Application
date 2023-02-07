package github.leavesczy.wifip2p;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.OpenableColumns;
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
import java.util.List;

public class Monitor_Progress extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;

    private static final int READ_REQUEST_CODE = 42;
    public String filename="progress.txt";
    private ProgressBar progress_bar;
    private Button btnMergeprogress;
    private TextView tvProgress;
    private float total_progress=0;
    private float relative_progress= 0;
    public int index=0;

    ArrayList<String> device_address=new ArrayList<>();
    ArrayList<String> device_progress=new ArrayList<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_progress);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);




        spinner=findViewById(R.id.spinner);
        progress_bar=findViewById(R.id.progressBar2);
        tvProgress=findViewById(R.id.tvProgress);

        btnMergeprogress=findViewById(R.id.btnMergeprogress);
        btnMergeprogress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });




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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getClipData() != null) {
                // handle multiple files
                int count = data.getClipData().getItemCount();
                String path=null;
                for (int i = 0; i < count; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();

                    // do something with the file URI
                    //Following commented code crashed due to unknown reasons
//                    algorithm al=new algorithm();
//                    path=al.getFilePathFromUri(fileUri);
                      path="/storage/emulated/0/Download/"+getName(this,fileUri);

                    merge(path);



                }
            } else if (data.getData() != null) {
                // handle single file
                Uri fileUri = data.getData();
                // do something with the file URI
            }
        }
    }

    private void merge(String path) {

        if(ExternalStorageAvailable()) {

            // File myExternalFile = new File(getExternalFilesDir(filepath), filename);
            //https://www.google.com/search?q=how+to+access+file+of+android+internal+storage+like+downloads+in+android+studio&tbm=vid&sa=X&ved=2ahUKEwiS1_Wd34L9AhXoSWwGHdE4B68Q0pQJegQIEhAB&biw=1920&bih=969&dpr=1#fpstate=ive&vld=cid:f94e1c6b,vid:N75CSHHcgr0

            StorageManager storagemanager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

            List<StorageVolume> storagevolumes = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                storagevolumes = storagemanager.getStorageVolumes();
            }

                StorageVolume storagevolume = storagevolumes.get(0);


                File mainprogressFile = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    mainprogressFile = new File(storagevolume.getDirectory().getPath()+"/Download/"+filename);
                }
//            File mainprogressFile = new File(getExternalFilesDir("/storage/emulated/0/Download"), "progress.txt");


                if (!mainprogressFile.exists()) {


                    FileOutputStream fos = null;
                    FileReader fr = null;
                    File clientfile = new File(path);
                    StringBuilder stringbuilder = new StringBuilder();

                    try {
                        fos = new FileOutputStream(mainprogressFile);

                        fr = new FileReader(clientfile);

                        BufferedReader br = new BufferedReader(fr);
                        String line = br.readLine();

                        while (line != null) {
                            stringbuilder.append(line);
                            line = br.readLine();
                        }

                        String text_data_client = stringbuilder.toString();

                        char[] prgress = text_data_client.toCharArray();
                        String extracted_total_progress = "";
                        while (prgress[index] != ',') {

                            extracted_total_progress += prgress[index];
                            index++;
                        }
                        float old_progress = Float.valueOf(extracted_total_progress);
                        total_progress += old_progress;
//                            String data=","+getMAC()+"@"+String.format("%.2f", relative_progress);
//                            String result = String.format("%.2f", total_progress)+text_data.substring(index)+data;
//                            fos = new FileOutputStream(mainprogressFile);
//                            fos.write(result.getBytes());
                        relative_progress = relative_progress(text_data_client);
                        String data1 = String.format("%.2f", total_progress) + "," + getMAC() + "@" + String.format("%.2f", relative_progress);
                        fos.write(data1.getBytes());
                        Toast.makeText(Monitor_Progress.this, "write successful", Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        Toast.makeText(Monitor_Progress.this, "fnf", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (IOException e) {
                        Toast.makeText(Monitor_Progress.this, "io", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }


                    Toast.makeText(Monitor_Progress.this, "Main Progress File Created", Toast.LENGTH_LONG).show();
                } else {


                    FileReader fr = null;
                    File mainprogressfile = new File(getExternalFilesDir("/storage/emulated/0/Download"), "progress.txt");
                    StringBuilder stringbuilder = new StringBuilder();
                    FileOutputStream fos = null;

                    FileReader fr_client = null;
                    File clientfile = new File(path);
                    StringBuilder stringbuilder_client = new StringBuilder();
                    FileOutputStream fos_client = null;

                    try {
                        fr = new FileReader(mainprogressFile);
                        BufferedReader br = new BufferedReader(fr);
                        String line = br.readLine();

                        //file reader for main progressfile
                        while (line != null) {
                            stringbuilder.append(line);
                            line = br.readLine();
                        }
                        String text_data = stringbuilder.toString();


                        //file reader for client file
                        fr_client = new FileReader(clientfile);
                        BufferedReader br_client = new BufferedReader(fr_client);
                        String line_client = br_client.readLine();
                        while (line_client != null) {
                            stringbuilder_client.append(line_client);
                            line_client = br_client.readLine();
                        }
                        String text_data_client = stringbuilder_client.toString();

                        //Reading the total progress of both the files and then adding them in to a file and storing in th e total progress file

                        float old_progress = total_progress(text_data, index);

                        total_progress = total_progress(text_data_client, 0);

                        total_progress += old_progress;

                        relative_progress = relative_progress(text_data_client);

                        String data = "," + getMAC() + "@" + String.format("%.2f", relative_progress);

                        String result = String.format("%.2f", total_progress) + text_data.substring(index) + data;
                        fos = new FileOutputStream(mainprogressFile);
                        fos.write(result.getBytes());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Toast.makeText(Monitor_Progress.this, "File Updated", Toast.LENGTH_LONG).show();
                }



        }
        updateprogressBar(total_progress);
        index=0;
    }


    private Float total_progress(String str,int index1)
    {
        char[] prgress=str.toCharArray();

        String extracted_total_progress="";

        while(prgress[index1]!=',') {

            extracted_total_progress+=prgress[index1];
            index1++;
        }
        index=index1;
     return Float.valueOf(extracted_total_progress);
    }

    private Float relative_progress(String str)
    {
       int l_index=str.indexOf('@');

       return Float.valueOf(str.substring(l_index+1));

    }
    @SuppressLint("Range")
    public static String getName(Context context, Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                // get file name
                fileName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return fileName;
    }
}



