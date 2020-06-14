package com.example.ssa_ezra.smsvoice;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

public class BlockActivity extends AppCompatActivity implements  View.OnClickListener
{


    ArrayList<String> phones = new ArrayList();
    ArrayAdapter<String> adapter;

    ArrayList<String> selectedPhones = new ArrayList();

    ListView phonesList;

    EditText phoneEditText;

    ImageButton buttonNext;
    ImageButton buttonPlus;
    ImageButton buttonMinus;
    ImageButton buttonShow;
    ImageButton buttonBlock;

    DataBaseHelper dbHelper;

    boolean viewContactList = false;
    boolean viewBlockedList = false;

    SQLiteDatabase database;

    private static final int REQUEST_CODE_READ_CONTACTS=1;
    private static boolean READ_CONTACTS_GRANTED =false;

    ContentValues contentValues1 = new ContentValues();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        dbHelper = new DataBaseHelper(this);

        phoneEditText = (EditText) findViewById(R.id.TextFields);

        buttonNext = (ImageButton) findViewById(R.id.button_Next);
        buttonNext.setOnClickListener(this);

        buttonPlus = (ImageButton) findViewById(R.id.button_Plus);
        buttonPlus.setOnClickListener(this);

        buttonMinus = (ImageButton) findViewById(R.id.button_Minus);
        buttonMinus.setOnClickListener(this);

        buttonShow = (ImageButton) findViewById(R.id.button_Show);
        buttonShow.setOnClickListener(this);

        buttonBlock = (ImageButton) findViewById(R.id.button_Block);
        buttonBlock.setOnClickListener(this);


        phonesList = (ListView) findViewById(R.id.phonesList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, phones);
        phonesList.setAdapter(adapter);

        // handling installation and unchecking the list
        phonesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            String phone;
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                // получаем нажатый элемент
                phone = adapter.getItem(position);
                if(phonesList.isItemChecked(position)==true)
                {
                    selectedPhones.add(phone);
                }
                else{
                    selectedPhones.remove(phone);
                }

            }
        });

        // get permissions
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        // if the device is up to API 23, set the resolution
        if(hasReadContactPermission == PackageManager.PERMISSION_GRANTED)
        {
            READ_CONTACTS_GRANTED = true;
        }
        else{
            // call the dialog box for setting permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }
        // if permission is set, download contacts
        if (READ_CONTACTS_GRANTED){
            buttonShow.setOnClickListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        switch (requestCode){
            case REQUEST_CODE_READ_CONTACTS:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    READ_CONTACTS_GRANTED = true;
                }
        }
        if(READ_CONTACTS_GRANTED){
            buttonShow.setOnClickListener(this);
        }
        else{
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
        }
    }


    ///обработчик нажатия кнопки
    @Override
    public void onClick(View v)
    {
        //take information from EditText
        String phone = phoneEditText.getText().toString();

        ///connect to database
        database = dbHelper.getWritableDatabase();

        ///content in database
        ContentValues contentValues = new ContentValues();

        switch (v.getId())
        {
            ///button Next
            case R.id.button_Next:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;

            ///button +
            case R.id.button_Plus:

                String output = "";
                if(!phone.isEmpty() && phones.contains(phone)==false)
                {
                    char[] chars = "1023456789".toCharArray();
                    StringBuilder sb = new StringBuilder(12);
                    Random random = new Random();
                    for (int i = 0; i <= 11; i++)
                    {
                        char c = chars[random.nextInt(chars.length)];
                        sb.append(c);
                    }
                    output = sb.toString();

                    adapter.setNotifyOnChange(true);
                    contentValues.put(DataBaseHelper.PHONE_NAME,phone);
                    contentValues.put(DataBaseHelper.PHONE_NUMBER,"+"+String.valueOf(output));
                    database.insert(DataBaseHelper.TABLE_CONTACTS,null,contentValues);
                    phoneEditText.setText("");
                    break;
                }

                for(int i=0; i< selectedPhones.size();i++)
                {
                    String phone1 = selectedPhones.get(i);
                    String[] words = phone1.split("\\+");
                    String phone_without_space;

                    ///delete space in phone number
                    String name;
                    phone_without_space = words[words.length-1].replaceAll("\\s","");
                    name = words[0];

                    contentValues.put(DataBaseHelper.PHONE_NUMBER,"+"+phone_without_space);
                    contentValues.put(DataBaseHelper.PHONE_NAME,name);
                    database.insert(DataBaseHelper.TABLE_CONTACTS,null,contentValues);
                }
                break;

            ///button -
            case R.id.button_Minus:

                // get and delete chosen items
                for(int i=0; i< selectedPhones.size();i++)
                {
                    String phone1 = selectedPhones.get(i);
                    String[] words = phone1.split("\\+");

                    String phone_without_space;
                    ///delete space in phone number
                    String name = words[0];
                    phone_without_space = words[words.length-1].replaceAll("\\s","");
                    phone_without_space = "+" + phone_without_space;

                    deletePhone(phone_without_space);
                    adapter.remove(selectedPhones.get(i));

                }
                // снимаем все ранее установленные отметки
                phonesList.clearChoices();
                // Cleared chosen object array
                selectedPhones.clear();
                adapter.notifyDataSetChanged();
                break;

            ///button Show
            case R.id.button_Show:
                viewContactList = !viewContactList;
                if(viewContactList)
                {
                    Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    startManagingCursor(cursor);
                    if (cursor.getCount() > 0)
                    {
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        while (cursor.moveToNext())
                        {
                            String phoneNum = cursor.getString(phoneIndex);
                            String phoneName = cursor.getString(nameIndex);
                            // process them as you want
                            if(phoneNum.contains("+"))
                            {
                                adapter.add(phoneName+" "+ phoneNum);
                            }
                            else
                            {
                                phoneNum = "+" + phoneNum;
                                adapter.add(phoneName+" "+ phoneNum);
                            }
                            Log.i("DATA"," ID "+cursor.getString(0)+" NAME "+cursor.getString(1)+" PHONE "+cursor.getString(2));
                        }
                    }
                }
                else
                {
                    adapter.clear();
                }
                break;

                ///button blockContacts
            case R.id.button_Block:
                viewBlockedList = !viewBlockedList;
                if(viewBlockedList)
                {
                    Cursor cursor = database.query(DataBaseHelper.TABLE_CONTACTS,null,null,null,null,null,null);
                    if(cursor.moveToFirst())
                    {
                        int phoneIndex = cursor.getColumnIndex(DataBaseHelper.PHONE_NUMBER);
                        int phoneName = cursor.getColumnIndex(DataBaseHelper.PHONE_NAME);
                        do {
                            String phoneNum = cursor.getString(phoneIndex);
                            String phoneNa = cursor.getString(phoneName);
                            adapter.add(phoneNa + " " + phoneNum);

                        }while(cursor.moveToNext());
                    }
                    cursor.close();
                }
                else
                {
                    adapter.clear();
                }
                //phoneEditText.setText("");
                break;
        }

        dbHelper.close();
    }


    public void deletePhone(String phone)
    {
        database.delete(DataBaseHelper.TABLE_CONTACTS, DataBaseHelper.PHONE_NUMBER + " = ?", new String[] {phone});
    }

}
