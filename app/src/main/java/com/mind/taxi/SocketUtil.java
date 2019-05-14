package com.mind.taxi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketUtil {
    public static String SERVER_IP = "112.149.54.212";
    public static int SERVER_PORT = 19999;
    static Socket socket;
    static DataInputStream input;
    static DataOutputStream output;
    static Thread tcp;
    static Handler handler = new Handler();
    static Context con;
    Activity activity;
    static Gson gson = new Gson();

    SocketUtil(Context con, Activity activity) {
        this.con = con;
        this.activity = activity;

        if (socket == null) {
            tcp = new Thread(new Runnable() {
                public void run() {
                    connect();
                }
            }
            );
            tcp.start();

        }
    }


    public static void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            while (socket != null) {
                if (socket.isConnected()) {
                    output.writeUTF("p`1`1`연결`");
                    output.flush();
                    break;
                }
            }
            MessageReciver messageReceiver = new MessageReciver();
            messageReceiver.start();
        } catch (Exception e) {
            System.out.println("서버에 접속할 수 없습니다.");
//            e.printStackTrace();
        }
    }

    public static class MessageReciver extends Thread {
        public void run() {
            try {
                String received;
                while ((received = input.readUTF()) != null) {
                    final String[] buffer = received.split("`");
//                    Log.e("TCP", buffer[0]);
                    switch (buffer[0].charAt(0)) {
                        case 'n':
//                            chatMessage = buffer[1]; //입장
//                            System.out.println(buffer[1]);
                            break;
                        case 'c':
//                            trash_can_num = buffer[1];
                            Log.e("TCP", buffer[1]);

                            if (buffer[1].charAt(0)=='1' && LoadingActivity.loading_activity_running) {
                                String response = buffer[1].split("/")[1];
                                Point[] result = gson.fromJson(response, Point[].class);
//                                Log.e("response", result[0].name + " " + result[0].address  + " " + result[0].latlng.latitude + " " + result[0].latlng.longitude);
                                LoadingActivity.start = result[0];
                                LoadingActivity.end = result[1];


                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(LoadingActivity.activity)
                                                .setTitle("콜 요청")
                                                .setMessage("[" + LoadingActivity.start.name + "] 에서 ["+LoadingActivity.end.name+"] 까지 가주세요")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
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
                                                                    LoadingActivity.activity.startActivity(new Intent(con, ReadyActivity.class));
                                                                    LoadingActivity.activity.finish();

                                                                }

                                                            }
                                                        }).start();

                                                    }
                                                })
                                                .show();
                                    }
                                });
//
//
//                                ((Activity) con).runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        CallDialog dialog = new CallDialog(LoadingActivity.mContext, LoadingActivity.activity);
//
//                                        dialog.show();
//                                    }
//                                });
//
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        LoadingActivity.createDialogInstance();
//                                        if(LoadingActivity.call_dialog!=null){
//                                            LoadingActivity.call_dialog.show();
//                                        }
//
//                                    }
//                                });

                            }

                            break;
                        case 'x':
//                            chatMessage = buffer[1]; //퇴장
                            break;
                        default:
                            break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
