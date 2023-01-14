package github.leavesczy.wifip2p;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class algorithm extends AppCompatActivity {

    public String path;
    private Button select;
    private TextView csvText;
    private Button calculate;
    private Button viewdb;
    private Button droptable;
    DatabaseHelper regiondb;
    private EditText proportion;

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

        if (!Python.isStarted())
            Python.start(new AndroidPlatform(this));

       final Python py = Python.getInstance();


        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PyObject pyobj = py.getModule("algorithm");
                PyObject obj = pyobj.callAttr("main",path);
                String progress=obj.toString();
                csvText.setText(progress);


                float proportion_val = Float.parseFloat(proportion.getText().toString());
                boolean insertion=regiondb.addData((float)26.3,proportion_val);


                if(insertion==true)
                {
                    Toast.makeText(algorithm.this,"Data Successfully Inserted",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(algorithm.this,"Data insertion failed",Toast.LENGTH_LONG).show();
                }
            }
        });


        droptable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regiondb.Emptytable();

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
            csvText.setText(path);
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

    




}
