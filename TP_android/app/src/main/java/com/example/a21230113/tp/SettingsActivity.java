package com.example.a21230113.tp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity{

    private static RadioButton rb1;
    private static RadioButton rb2;
    private static RadioButton rb3;
    private static RadioButton rb4;
    private Spinner dropdown;
    private Button playGame;
    private Button createGame;
    private Button joinGame;
    private ArrayList<Level> levels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        levels = new ArrayList<>();
        levels.add(new Level(4, getString(R.string.beginner),null));
        levels.add(new Level(8, getString(R.string.easy),null));
        levels.add(new Level(10, getString(R.string.medium),null));
        levels.add(new Level(20, getString(R.string.hard),null));
        levels.add(new Level(30, getString(R.string.professional),null));

        readFromFile();

        rb1 = (RadioButton) findViewById(R.id.radioButton1);
        rb2 = (RadioButton) findViewById(R.id.radioButton2);
        rb3 = (RadioButton) findViewById(R.id.radioButton3);
        rb4 = (RadioButton) findViewById(R.id.radioButton4);
        playGame = (Button) findViewById(R.id.button1);
        createGame = (Button) findViewById(R.id.btnCreateGame);
        joinGame = (Button) findViewById(R.id.btnJoinGame);
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb2.setChecked(false);
                rb3.setEnabled(false);
                rb4.setEnabled(false);
                joinGame.setEnabled(false);
                createGame.setEnabled(false);
                playGame.setEnabled(true);
            }
        });
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb1.setChecked(false);
                rb3.setEnabled(true);
                rb4.setEnabled(true);
                if(rb4.isChecked()){
                    joinGame.setEnabled(true);
                    createGame.setEnabled(true);
                    playGame.setEnabled(false);
                }
            }
        });
        rb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb4.setChecked(false);
                joinGame.setEnabled(false);
                createGame.setEnabled(false);
                playGame.setEnabled(true);
            }
        });
        rb4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb3.setChecked(false);
                joinGame.setEnabled(true);
                createGame.setEnabled(true);
                playGame.setEnabled(false);
            }
        });

        dropdown = (Spinner)findViewById(R.id.spinner);
        String []items = new String[levels.size()];
        for(int i=0; i<levels.size(); i++){
            items[i] = levels.get(i).getNameLevel();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Level level = null;
                for (int i = 0; i < levels.size(); i++) {
                    if (levels.get(i).getNameLevel().equals(dropdown.getSelectedItem())) {
                        level = levels.get(i);
                        break;
                    }
                }
                Intent intent = new Intent(v.getContext(), GameActivity.class);
                intent.putExtra("level", level.getNameLevel());
                if (rb1.isChecked()) {
                    intent.putExtra("type", "single");
                } else {
                    if (rb2.isChecked())
                        intent.putExtra("type", "multi1");
                }
                startActivity(intent);
            }
        });
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Level level = null;
                for (int i = 0; i < levels.size(); i++) {
                    if (levels.get(i).getNameLevel().equals(dropdown.getSelectedItem())) {
                        level = levels.get(i);
                        break;
                    }
                }
                Intent intent = new Intent(v.getContext(), GameActivity.class);
                intent.putExtra("level", level.getNameLevel());
                intent.putExtra("type", "multi2serv");
                startActivity(intent);
            }
        });
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Level level = null;
                for (int i = 0; i < levels.size(); i++) {
                    if (levels.get(i).getNameLevel().equals(dropdown.getSelectedItem())) {
                        level = levels.get(i);
                        break;
                    }
                }
                Intent intent = new Intent(v.getContext(), GameActivity.class);
                intent.putExtra("level", level.getNameLevel());
                intent.putExtra("type", "multi2cli");
                startActivity(intent);
            }
        });
    }

    private void readFromFile() {
        try {
            FileInputStream fis = openFileInput("fileLevels3.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);


            List<Level> level = new ArrayList<>();
            level = (List<Level>) ois.readObject();
            ois.close();
            levels.addAll(level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
