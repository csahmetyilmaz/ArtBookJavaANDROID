package com.ahmetyilmaz.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView1);
        nameArray=new ArrayList<String>();
        idArray=new ArrayList<Integer>();

        //Choose adapter getting String data from nameArray for our listView
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);

        //Set clickListener to selected items on listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //make new Intent to go to other Activity and charge id data
                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                intent.putExtra("artId",idArray.get(position));
                intent.putExtra("info","old");
                startActivity(intent);
            }
        });

        getData();
    }


    //creating new menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflater important :) it is getting our menu
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu);

        return super.onCreateOptionsMenu(menu);
    }

    //adjust what program will do when onclick to menuItem
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //if my menu item is clicked open activity interested
        if (item.getItemId() == R.id.add_art_item){
            Intent intent = new Intent(getApplicationContext(),Main2Activity.class);
            intent.putExtra("info","new");
            startActivity(intent);

        }


        return super.onOptionsItemSelected(item);
    }



    public void getData(){

    try {

        //connect to db
        SQLiteDatabase database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        //Getting indexes
        Cursor cursor = database.rawQuery("SELECT * FROM arts",null);
        int nameIndex=cursor.getColumnIndex("artname");
        int idIndex = cursor.getColumnIndex("id");

        //getting results
        while (cursor.moveToNext()){

            //adding result to arrays
            nameArray.add(cursor.getString(nameIndex));
            idArray.add(cursor.getInt(idIndex));
        }

        //Show my changed data in the list!
        arrayAdapter.notifyDataSetChanged();

        cursor.close();

        System.out.println("Veriler cekildi");

    }catch (Exception exc){
        exc.printStackTrace();
    }


    }
}
