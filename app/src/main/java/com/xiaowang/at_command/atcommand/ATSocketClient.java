package com.xiaowang.at_command.atcommand;

import android.location.Address;
import android.os.AsyncTask;
import android.text.method.MovementMethod;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by XiaoWang on 2015/5/7.
 */
    public class ATSocketClient extends AsyncTask<Void, String, Void> {

    static final String TAG = "AT_TAG";
    String dstAddress;
    int dstPort;

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getDstAddress() {
        return dstAddress;
    }

    String response = "";
    private boolean isActive=false;
    TextView  textResponse =null;
    public static Socket socket =null;
    BufferedReader input =null;
    static PrintWriter output =null;
    private static List<OnPostExecute> onPostExecutes= new ArrayList<>();
    private static List<OnProgressUpdate> onProgressUpdates= new ArrayList<>();
    private static List<OnPreExecute> onPreExecutes= new ArrayList<>();

    public static void addCallBack(OnPreExecute onPreExecute){
        if(!onPreExecutes.contains(onPreExecute)){
            onPreExecutes.add(onPreExecute);
        }
    }
    public static void addCallBack(OnPostExecute onPostExecute){
        if(!onPostExecutes.contains(onPostExecute)) {
            onPostExecutes.add(onPostExecute);
        }
    }
    public static void addCallBack(OnProgressUpdate onProgressUpdate){
        if(!onProgressUpdates.contains(onProgressUpdate)) {
            onProgressUpdates.add(onProgressUpdate);
        }
    }
    public static void removeCallBack(OnProgressUpdate onProgressUpdate){
        onProgressUpdates.remove(onProgressUpdate);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for(OnPreExecute v:onPreExecutes){
            v.onPreExecute();
        }
        Log.d(TAG, "onPreExecute");
    }

    public static PrintWriter getOutputStream(){
        return output;
    }
    ATSocketClient(String addr, int port,TextView  textResponse){
        dstAddress = addr;
        dstPort = port;
        this.textResponse = textResponse;
        socket = new Socket();
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            isActive = true;
            Log.d(TAG, "Background");
            SocketAddress addr = new InetSocketAddress(dstAddress,dstPort);
            publishProgress( "Connecting...\n\r");
            socket.connect(addr);
            //socket = new Socket(dstAddress, dstPort);
            Log.d(TAG,"new Socket(dstAddress, dstPort)" );
            output = new PrintWriter(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.write("AT\r");
            output.flush();
            publishProgress( "Connection successful!\n\r");
            Log.d(TAG,"receiver start..." );
            char buf[] = new char[512];
            int cnt =0;
            while(true){
                while((cnt = input.read(buf))!=-1){
                    Log.d(TAG, "received data");
                    publishProgress(String.valueOf(buf, 0, cnt));
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
            Log.d(TAG, "UnknownHostException: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
            Log.d(TAG,"IOException: " + e.toString());
        }finally{
            if(socket != null){
                //close();//finnally I get why I cant call close in this function.
                //this funcion is in other Thread. So, it can not change the GUI in which is not in this thread
                //however, the original close founction include invoking GUI that is upDateTextResponse("Socket closed\n\r");

                //because the  upDateTextResponse is removed, the we the close function can be call freely
                close();
                Log.d(TAG,"0000000000015");
                response = "Socket Closed\n\r";
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String result="";
        for (String str : values){
            result += str;
        }
        upDateTextResponse(result);
        Log.d(TAG, "Display data " + result);
        for(OnProgressUpdate v : onProgressUpdates){
            v.onProgressUpdate(result);
        }
    }
    public static boolean close(){
        if (socket==null) {
            return true;
        }
        try {
            if(socket.isClosed()){
                Log.d(TAG,"0000000000001");
                return true;
            }
            else {
                socket.close();
                Log.d(TAG, "0000000000002");
            }
        }catch(IOException e){Log.d(TAG,"0000000000004");
            return false;
        }finally{
            // when exception occuring, the other thread who refers the socket will get a exception
            //so the input sream and output stream in doInBackground() function (thread) will get a Exception
            //that's it will Exit the function and thread. then post the data
            //there for the " socket closed" remianding sentence can be put in onPostExecute
            //we also can put the "socket closed" in response variable
            Log.d(TAG, "0000000000005");
            //upDateTextResponse("Socket Closed\n\r");
        }
        return true;
    }
    @Override
    protected void onPostExecute(Void result) {
        isActive=false;
        upDateTextResponse(response);
        Log.d(TAG, "Post data");
        //upDateTextResponse("Socket closed\n\r");
        super.onPostExecute(result);
        for(OnPostExecute v : onPostExecutes){
            v.onPostExecute(response);
        }
    }
    public void upDateTextResponse(String str){
        textResponse.setText(textResponse.getText() + str);
        int scrollY = textResponse.getLineCount()*textResponse.getLineHeight()-textResponse.getHeight();
        if(scrollY>0)   textResponse.setScrollY(scrollY);
    }
    public boolean isActive(){
        return isActive;
    }

    public interface OnPostExecute{
        void onPostExecute(String result);
    }
    public interface OnProgressUpdate{
        void  onProgressUpdate(String values);
    }



    public interface OnPreExecute{
        void onPreExecute() ;
    }
}


/**************************************************************************************
 *******************************************************************************
Alert:
 1.almost all the unstable and the program dead in socket actually is caused by multiple Thread problem.
 2.Be careful to touch other thread's resource. Especially the GUI resource, GUI component


 *****************************************************************************************/

