package com.example.a21230113.tp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class Level implements Serializable{

    String nameLevel;
    int nCards;
    byte [][] images;

    public Level(int nCards, String nameLevel, ArrayList<Bitmap> bitmap){
        this.nCards = nCards;
        this.nameLevel = nameLevel;
        if(bitmap != null) {
            images = new byte[bitmap.size()][];
            for (int i = 0; i < bitmap.size(); i++) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.get(i).compress(Bitmap.CompressFormat.PNG, 100, stream);
                images[i] = stream.toByteArray();
            }
        }
        else{
            images = null;
        }
    }

    public int getNCards(){
        return nCards;
    }

    public ArrayList<Bitmap> getBitmapArray() {
        ArrayList<Bitmap> bitmapArray = new ArrayList<>();
        for(int i = 0; i < images.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(images[i], 0, images[i].length);
            bitmapArray.add(bitmap);
        }
        return bitmapArray;
    }

    public String getNameLevel(){
        return nameLevel;
    }

}
