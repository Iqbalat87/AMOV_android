package com.example.a21230113.tp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class HistoricActivity extends AppCompatActivity{

    TextView txtView;
    Button btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historic_layout);

        txtView = (TextView) findViewById(R.id.histTxtView);
        btn = (Button) findViewById(R.id.buttonDel);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileOutputStream fos = openFileOutput("histFile", Context.MODE_PRIVATE);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                    bw.write("");
                    bw.newLine();
                    txtView.setText("");
                }catch(IOException e){e.printStackTrace();}
            }
        });

        readFromFile();
    }

    public void readFromFile() {
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("histFile");
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            int i=0;
            while ((line = bufferedReader.readLine()) != null) {
                i++;
                result.append(line + "\r\n");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }finally {
            try{
                bufferedReader.close();
            }catch (Exception e){e.printStackTrace();}
        }
        txtView.setText(result);
    }
}
