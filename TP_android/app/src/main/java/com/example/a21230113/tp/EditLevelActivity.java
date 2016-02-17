package com.example.a21230113.tp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditLevelActivity extends AppCompatActivity{

    protected SeekBar seekbar;
    protected TextView tv;

    EditText editText;
    Button addLevelButton;
    Button button, delete;
    Spinner dropdown;
    String [] items;
    ArrayAdapter<String> adapter;
    List<Level> levels = new ArrayList<>();
    List<Bitmap> images = new ArrayList<>();
    int contador = 0;

    private static final int LOAD_IMAGE_RESULTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editText = (EditText) findViewById(R.id.editText);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        tv  = (TextView) findViewById(R.id.textView3);
        button = (Button) findViewById(R.id.button);
        addLevelButton = (Button) findViewById(R.id.addLevel);

        tv.setText(getResources().getString(R.string.number_pairs) + ": 2");

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int n = progress + 2;
                tv.setText(getResources().getString(R.string.number_pairs) + ": " + n);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addLevelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLevel();
                writeToFile();
            }
        });

        readFromFile();

        dropdown = (Spinner) findViewById(R.id.spinner);

        updateSpinner();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        delete = (Button) findViewById(R.id.delete);

        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(items != null) {
                    if(levels.size() == 0) return;
                    levels.remove(dropdown.getSelectedItemPosition());
                    updateSpinner();
                    adapter.notifyDataSetChanged();
                    writeToFile();
                    Toast.makeText(getApplicationContext(), getString(R.string.deleted_level), Toast.LENGTH_SHORT).show();
                }
            }
        });


        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, LOAD_IMAGE_RESULTS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(contador < seekbar.getProgress()+2) {
            if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
                Uri pickedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                Bitmap bm = BitmapFactory.decodeFile(imagePath);
                images.add(bm);
                cursor.close();
                contador += 1;
                Toast.makeText(getApplicationContext(), getString(R.string.image_added) + contador, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.images_added), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateSpinner(){
        String [] s = new String[levels.size()];
        for(int i=0; i<levels.size(); i++){
            s[i] = levels.get(i).getNameLevel();
        }
        items = s;
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }

    public void addLevel(){
        if(images.size() == seekbar.getProgress() + 2) {
            levels.add(new Level((seekbar.getProgress() + 2) * 2, editText.getText().toString(), (ArrayList<Bitmap>) images));
            updateSpinner();
            adapter.notifyDataSetChanged();
            writeToFile();
            Toast.makeText(getApplicationContext(), getString(R.string.level_saved), Toast.LENGTH_SHORT).show();
            contador = 0;
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.images_differs),Toast.LENGTH_SHORT).show();
        }
    }

    private void writeToFile(){
        FileOutputStream fos;
        ObjectOutputStream oos = null;
        try{
            fos = getApplicationContext().openFileOutput("fileLevels3.txt", Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(levels);
            oos.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(oos != null)
                try{
                    oos.close();
                }catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void readFromFile() {
        try {
            FileInputStream fis = openFileInput("fileLevels3.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);

            levels = (List<Level>) ois.readObject();

            ois.close();

        } catch (Exception e) {

        }
    }

}

