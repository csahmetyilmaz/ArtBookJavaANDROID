package com.ahmetyilmaz.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView;
    Bitmap selectedImage;
    EditText artNameText,painterText,yearText;
    Button saveButton;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView1);
        artNameText=findViewById(R.id.nameEditText);
        painterText=findViewById(R.id.painterText);
        yearText=findViewById(R.id.yearText);
        saveButton=findViewById(R.id.saveButton);

        //connect to db
        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        //getting info is it a new article or not
        Intent intent = getIntent();
        String info=intent.getStringExtra("info");

        if (info.matches("new")){
            //if this is a new register  than the fields come empty and button visible
            artNameText.setText("");
            painterText.setText("");
            yearText.setText("");
            saveButton.setVisibility(View.VISIBLE);
            Bitmap selectImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.select);
            imageView.setImageBitmap(selectImage);

        }else {
            //if this is a OLD  register  than the fields come with VALUES from db and button invisible
            int artId =intent.getIntExtra("artId",1);
            saveButton.setVisibility(View.INVISIBLE);

            //Connect DB to get selected items details
            try {
                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});
                //getting column indexses of selected item
                int artNameIndex=cursor.getColumnIndex("artname");
                int painterNameIndex=cursor.getColumnIndex("paintername");
                int yearIndex=cursor.getColumnIndex("year");
                int imageIndex=cursor.getColumnIndex("image");

                //Retriewing data from db with indexes
                while (cursor.moveToNext()){
                    artNameText.setText(cursor.getString(artNameIndex));
                    painterText.setText(cursor.getString(painterNameIndex));
                    yearText.setText(cursor.getString(yearIndex));
                    //getting bytes and convert to blob and image
                    byte[] bytes = cursor.getBlob(imageIndex);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);


                }

            }catch (Exception exc){
                exc.printStackTrace();
            }


        }

    }

    public void save(View view){

        String artName=artNameText.getText().toString();
        String painterName=painterText.getText().toString();
        String year=yearText.getText().toString();

        Bitmap smallImage =makeSmallerImage(selectedImage,300);

        //prepare convert  our image to data
        ByteArrayOutputStream outputStream1=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream1);
        byte[] byteArray = outputStream1.toByteArray();

        //SAVE to DATABASE
        try {

            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            //create table
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR, paintername VARCHAR, year VARCHAR,image BLOB)");

            //INSERTING STATEMENTS
            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?) ";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            //EXECUTE insert operation
            sqLiteStatement.execute();
            System.out.println("Data saved");




        }catch (Exception exc){
            exc.printStackTrace();
        }

        //Close this activity and go to mainActivity
       //--> finish(); if we use it we cannot see the last thing added intent is better
        Intent intent =new Intent(Main2Activity.this,MainActivity.class);
        //clear other activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio =(float)width/(float) height;
        if (bitmapRatio>1){
            width=maximumSize;
            height=(int)(width/bitmapRatio);
        }else {

            height=maximumSize;
            width = (int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view){
        //if there is no permission to go to gallery asks permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){ //if we got permission
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==2 && resultCode== RESULT_OK && data != null){

            //get the uri of selected image
             Uri imageData = data.getData();
            try {

                //Version Control
                if (Build.VERSION.SDK_INT >= 28){

                    //imageDecoder is a new class instead of getBitMap we must use after 28
                    //preparing source to get my bitmap image
                    ImageDecoder.Source source1 = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    //decoding my bitmap image coming from source1
                    selectedImage=ImageDecoder.decodeBitmap(source1);
                    imageView.setImageBitmap(selectedImage);
                }else{

                    //getting image selected
                    selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    //Setting to imageView
                    imageView.setImageBitmap(selectedImage);

                }



            } catch (IOException exc) {
                exc.printStackTrace();
            }

        }



        super.onActivityResult(requestCode, resultCode, data);
    }
}
