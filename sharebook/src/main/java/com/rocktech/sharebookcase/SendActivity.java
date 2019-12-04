package com.rocktech.sharebookcase;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rocktech.sharebook.R;

public class SendActivity extends AppCompatActivity {
    private EditText addrEditText;
    private EditText contEditText;
    private Button sendButton;
    private Button connButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);


        addrEditText = (EditText) findViewById(R.id.et_addr);
        contEditText = (EditText) findViewById(R.id.et_content);
        sendButton = (Button) findViewById(R.id.btn_send);
        connButton = (Button) findViewById(R.id.btn_connect);
        connButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr = addrEditText.getText().toString().trim();
                Intent intent = new Intent(SendActivity.this,WebSocketService.class);
                intent.putExtra("addr",addr);
                startService(intent);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cont = contEditText.getText().toString().trim();
                WebSocketService.sendMsg(cont);
            }
        });
    }
}
