package com.yydcdut.markdowndemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("RxMarkdown");

        findViewById(R.id.btn_edit_show).setOnClickListener(this);
        findViewById(R.id.btn_edit_show_rx).setOnClickListener(this);
        findViewById(R.id.btn_compare).setOnClickListener(this);
        findViewById(R.id.btn_compare_rx).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btn_compare || id == R.id.btn_compare_rx) {
            Intent intent = new Intent(this, CompareActivity.class);
            intent.putExtra("is_rx", id == R.id.btn_compare_rx);
            startActivity(intent);
            return;
        }

        if (id == R.id.btn_edit_show || id == R.id.btn_edit_show_rx) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra("is_rx", id == R.id.btn_edit_show_rx);
            startActivity(intent);
        }
    }
}