package com.example.austinwilliams.swipeawayhate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button label = (Button) findViewById(R.id.label);
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLabel();
            }
        });
    }

    private void goToLabel() {
        Intent intent = new Intent(this, LabelActivity.class);
        startActivity(intent);
    }
}
