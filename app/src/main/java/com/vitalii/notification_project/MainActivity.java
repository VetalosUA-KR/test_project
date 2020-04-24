package com.vitalii.notification_project;

import android.content.ContentResolver;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements  View.OnClickListener,TextToSpeech.OnInitListener{
    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;

    ArrayAdapter arrayAdapter;

    TextToSpeech tts;
    Button btnSpeak;
    Button button;
    TextView textView;

    public static  MainActivity instance()
    {
        return inst;
    }

    public void onStart(){
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this,this);
        textView = (TextView) findViewById(R.id.textView);
        btnSpeak = (Button)findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(this);

        smsListView = (ListView)findViewById(R.id.List);

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        //if Permission Is Not GRANTED
        if(ContextCompat.checkSelfPermission(getBaseContext(),
                "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED)
        {
            // Show SMS
            refreshSMSInbox();
        }
        else
        {
            final  int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public void refreshSMSInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("Body");
        int indexAdress = smsInboxCursor.getColumnIndex("address");
        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        String str = "SMS From: " + smsInboxCursor.getString(indexAdress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
        arrayAdapter.add(str);


        ///added info about who sended message for us
        String from = "You have SMS From: " + smsInboxCursor.getString(indexAdress);
        String sms = "Your sms is " + smsInboxCursor.getString(indexBody);
        textView.setText(from + "\n" + sms);


    }

    public void updateList(final String smsMessage){
        arrayAdapter.insert(smsMessage,0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
         speakOut();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    ///speech to text
    private void speakOut() {
        String text = textView.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
