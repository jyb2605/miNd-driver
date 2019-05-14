package com.mind.taxi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mind.taxi.R;

import java.io.IOException;


public class CallDialog extends Dialog {
    Context con;
    Activity activity;
    Button ok;
    Button cancel;
    TextView start;
    TextView end;


    public CallDialog(@NonNull Context context, Activity activity) {
        super(context);
        con = context;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_call);
//        setCustomActionbar();
        ok = findViewById(R.id.ok);
        cancel = findViewById(R.id.cancel);

        start = findViewById(R.id.start);
        end = findViewById(R.id.end);

//        start.setText();

        if(LoadingActivity.start!=null && LoadingActivity.end!=null){
            start.setText(LoadingActivity.start.name);
            end.setText(LoadingActivity.end.name);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(con, ReadyActivity.class));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (SocketUtil.socket.isConnected()) {
                            try {
                                SocketUtil.output.writeUTF("c`2`");
                                SocketUtil.output.flush();
                                //002 : 기사가 승객의 콜 수락
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }).start();


                dismiss();
                activity.finish();

            }
        });


    }

}
