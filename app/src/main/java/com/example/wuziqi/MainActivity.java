package com.example.wuziqi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WuziqiPanel wuziqiPanel;

    private Button bt_restart;
    private Button bt_BackPiece;
    private Button bt_OnLine;
    private Button bt_GiveIn;
    private Button bt_Peace;
    private Button bt_StopOnline;

    //文字显示
    private TextView tvTextViewStatus;
    private TextView tvTextViewPieceColor;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wuziqiPanel = findViewById(R.id.id_Wuziqi);

        bt_restart = findViewById(R.id.bt_restart);
        bt_BackPiece = findViewById(R.id.bt_BackPiece);
        bt_OnLine = findViewById(R.id.bt_OnLine);
        bt_GiveIn = findViewById(R.id.bt_GiveIn);
        bt_Peace = findViewById(R.id.bt_Peace);
        bt_StopOnline = findViewById(R.id.bt_StopOnline);

        bt_restart.setOnClickListener(this);
        bt_BackPiece.setOnClickListener(this);
        bt_OnLine.setOnClickListener(this);
        bt_GiveIn.setOnClickListener(this);
        bt_Peace.setOnClickListener(this);
        bt_StopOnline.setOnClickListener(this);

        //在主线程不能使用网络连接解决办法（不推荐）
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().
                detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

    }

    @Override
    public void onClick(View v) {
    switch (v.getId()){
        case R.id.bt_restart:
            wuziqiPanel.restart();
        break;
        case R.id.bt_BackPiece:
            wuziqiPanel.BackPiece();
            break;
        case R.id.bt_OnLine:
            wuziqiPanel.online();
            break;
        case R.id.bt_GiveIn:
                wuziqiPanel.GiveIn();
            break;
        case R.id.bt_Peace:
                wuziqiPanel.computer();
            break;
        case R.id.bt_StopOnline:
            wuziqiPanel.stopOnline();
            break;

        }
    }


}
