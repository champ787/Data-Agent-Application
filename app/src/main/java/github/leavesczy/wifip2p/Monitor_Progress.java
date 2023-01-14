package github.leavesczy.wifip2p;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class Monitor_Progress extends AppCompatActivity {

    private Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_progress);

        spinner=findViewById(R.id.spinner);

        ArrayList<String> al=new ArrayList<>();
        al.add("region 1");
        al.add("region 2");
        al.add("region 3");

        ArrayAdapter<String> aa=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,al);
        spinner.setAdapter(aa);
    }
}