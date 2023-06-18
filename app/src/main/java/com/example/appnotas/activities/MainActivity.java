package com.example.appnotas.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.appnotas.R;
import com.example.appnotas.activities.CreateNoteActivity;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ADD_NOTE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                    }
                });

        imageAddNoteMain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            launcher.launch(intent);
        });
    }

}