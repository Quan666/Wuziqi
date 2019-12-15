package com.example.wuziqi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class User {
    private String id;//唯一id
    private Socket socket;
    private int x;
    private int y;
    private boolean mIswhite;
    private boolean mIsGameOver;
    private String name;//联机名
    private DataOutputStream out;
    private DataInputStream in;

    public User(String id,String name,final Socket socket) throws IOException{
        this.id = id;
        this.name = name;
        this.socket = socket;
        OutputStream outToServer = null;
        InputStream inFromServer = null;
        try {
            outToServer = socket.getOutputStream();
            inFromServer = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.out = new DataOutputStream(outToServer);
        this.in  =  new DataInputStream(inFromServer);
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean ismIsGameOver() {
        return mIsGameOver;
    }

    public void setmIsGameOver(boolean mIsGameOver) {
        this.mIsGameOver = mIsGameOver;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean ismIswhite() {
        return mIswhite;
    }

    public void setmIswhite(boolean mIswhite) {
        this.mIswhite = mIswhite;
    }
}
