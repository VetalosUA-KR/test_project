package com.vitalii.notification_project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.SystemUpdatePolicy;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private int tttt = 10;
    private String test = "привет";
    private String test2 = "привет";
    private String test3 = "hello wolrd";
    private String tes = "hello wolrd";
    private String tes22 = "ДЕНЯ";
    private int sum = 100;
    private int sumqqqq = 55555;
    private int f =898;


    public String teswt = "other";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        print1();

    }

    public void print1()
    {
        System.out.println("Hello world");

    }
}
