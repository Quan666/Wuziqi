package com.example.wuziqi;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Online extends Thread {
    //0：创建对战
    //1：落子信息
    //2：悔棋信息
    //3：结束游戏
    //4：求和
    //详见服务器代码注释
    // 信息的格式：(0||1||2||3||4),name,发送人id,x,y,IsWhite,IsGameOver$
    String type;
    User user;
    ClientThread thread;
    Handler uiHandler;

    public Online(String type, String name, int x, int y, boolean IsWhite, boolean IsGameOver, Handler uiHandler){
        try {
            user = new User("",name,new Socket("60.205.212.238", 55555));
            //user.getOut().writeUTF(type+","+user.getName()+","+user.getId()+","+x+","+y+","+user.ismIswhite()+","+user.ismIsGameOver()+"$");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.type = type;
        user.setX(x);
        user.setY(y);
        user.setmIswhite(IsWhite);
        user.setmIsGameOver(IsGameOver);
        //开启一个线程接收信息，并解析
        thread = new ClientThread(user,uiHandler);
        thread.start();

    }
    public void run(){
        SendMsg(0,0,0);
        user.setId(thread.getUser().getId());
    }
    public User getUser(){
        return user;
    }
    public void stopGame(){
        try {
            user.getOut().close();
            user.getIn().close();
            user.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void SendMsg(int type,int x,int y){
        try {
            //这个线程用来发送信息
            user.getOut().writeUTF(type+","+user.getName()+","+user.getId()+","+x+","+y+","+user.ismIswhite()+","+user.ismIsGameOver()+"$");
        }catch(Exception e){
            System.out.println("服务器异常");
        }
    }

    public boolean getIsWhite(){
        return user.ismIswhite();
    }
}
class ClientThread extends Thread {
    Handler uiHandler;
    private User user;
    public ClientThread(User user, Handler uiHandler) {
        this.user = user;
        this.uiHandler = uiHandler;
    }
    public Point rePoint(String x,String y){
        return new Point(Integer.parseInt(x),Integer.parseInt(y));
    }
    public User getUser(){
        return user;
    }
    public void run() {
        Message message;

        try {
            while (true){
                //循环接收消息
                String msg = user.getIn().readUTF();
                String[] allstr = msg.split("$");
                String[] str = allstr[allstr.length-1].split(",");
                switch (str[0]) {
                    case "0":
                        user.setId(str[2]);
                        user.setmIswhite(Boolean.parseBoolean(str[5]));
                        message = new Message();
                        message.what = 0;
                        uiHandler.sendMessage(message);
                        break;
                    case "1":
                        //list.add(rePoint(str[3],str[4]));
                        message = new Message();
                        message.what = 1;
                        message.arg1 = Integer.parseInt(str[3]);
                        message.arg2 = Integer.parseInt(str[4]);
                        message.obj = Boolean.parseBoolean(str[5]);
                        uiHandler.sendMessage(message);
                        //uiHandler.removeMessages(message.what);
                        //通知下棋
                        break;
                    case "2":
                        message = new Message();
                        message.what = 2;
                        uiHandler.sendMessage(message);
                        break;
                    case "3":
                        message = new Message();
                        message.what = 3;
                        uiHandler.sendMessage(message);
                        break;
                    case "4":

                        break;
                    case "5":
                        message = new Message();
                        message.what = 5;
                        message.obj = Boolean.parseBoolean(str[5]);
                        uiHandler.sendMessage(message);
                        break;
                    case "6":
                        message = new Message();
                        message.what = 6;
                        uiHandler.sendMessage(message);
                        break;
                    case "7":
                        user.getSocket().close();
                        message = new Message();
                        message.what = 7;
                        message.arg1 = 1;
                        uiHandler.sendMessage(message);
                        break;
                    case "8":
                        message = new Message();
                        message.what = 8;
                        uiHandler.sendMessage(message);
                        break;
                    case "9":
                        message = new Message();
                        message.what = 9;
                        uiHandler.sendMessage(message);
                        break;
                    case "10":
                        message = new Message();
                        message.what = 10;
                        uiHandler.sendMessage(message);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e1) {
            try {
                user.getSocket().close();
                message = new Message();
                message.what = 7;
                message.arg1 = 0;
                uiHandler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}