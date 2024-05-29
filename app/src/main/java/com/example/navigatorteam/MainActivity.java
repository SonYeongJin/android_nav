package com.example.navigatorteam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // 레이아웃 설정

        // Button 초기화와 클릭 리스너 설정은 onCreate 메서드 내에서 해야 합니다.
        Button homeButton = findViewById(R.id.Homebutton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity가 아니라 Home 액티비티로 이동하려는 의도이므로 this를 사용해야 합니다.
                Intent intent = new Intent(MainActivity.this, Main.class);
                startActivity(intent);
            }
        });
    }
}
