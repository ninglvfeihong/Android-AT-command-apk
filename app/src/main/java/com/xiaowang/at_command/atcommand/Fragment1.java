package com.xiaowang.at_command.atcommand;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import junit.framework.Test;

import java.io.PrintWriter;

/**
 * Created by NINI on 2015/5/7.
 */
public class Fragment1 extends Fragment {
    final String TAG = "AT_TAG";
    ATSocketClient client =null;
    Button send = null;
    TextView respText=null;
    EditText atCommandInput = null;
    EditText atDstAddr = null;
    EditText atDstPort = null;
    Button btnConnect = null;
    Button btnClear =null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment1,container,false);
        respText = (TextView) v.findViewById(R.id.at_command_resp_text);
        send = (Button) v.findViewById(R.id.at_command_send_btn);
        atCommandInput = (EditText) v.findViewById(R.id.at_command_input);
        btnClear = (Button) v.findViewById(R.id.btn_clear);
        btnConnect = (Button) v.findViewById(R.id.btn_connect);
        atDstAddr = (EditText) v.findViewById(R.id.dst_addr);
        atDstPort = (EditText) v.findViewById(R.id.dst_port);

        atCommandInput.requestFocus();

        recoverDst();
        configSocket();
        addLister();
        return v;
    }

    void configSocket(){

    }
    void addLister(){
        respText.setMovementMethod(new ScrollingMovementMethod());
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter output = ATSocketClient.getOutputStream();
                if (output != null) {
                    output.print(atCommandInput.getText().toString());
                    output.flush();
                }
            }
        });

        //for btnConnect text abbribute callback from ATSocketClient
        ATSocketClient.addCallBack(new ATSocketClient.OnPostExecute() {
            @Override
            public void onPostExecute(String result) {
                btnConnect.setText(R.string.btn_socket_connect);
            }
        });
        ATSocketClient.addCallBack(new ATSocketClient.OnPreExecute() {
            @Override
            public void onPreExecute() {
                btnConnect.setText(R.string.btn_socket_disconnect);
                if(client!=null) saveDst(client.getDstAddress(), client.getDstPort());
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(client ==null){
                    Log.d(TAG,"soket is initializing");
                    String dstAddr = getDstAddr();
                    int port = getDstPort();
                    if (port != -1 && dstAddr != null) {
                        client = new ATSocketClient(dstAddr, port, respText);
                        Log.d(TAG,"new ATSocket");
                        client.execute();
                        Log.d(TAG, "execute");

                    }
                }else if(!client.isActive() ) {
                    Log.d(TAG,"soket is closed");
                    String dstAddr = getDstAddr();
                    int port = getDstPort();
                    if (port != -1 && dstAddr != null) {
                        client = new ATSocketClient(dstAddr,port,respText);
                        //client.dstAddress = dstAddr;//this cause error, because a AsyncTask can only execute once. the task is executed
                        //client.dstPort = port;
                        client.execute();
                        Log.d(TAG, "soket is executed");
                    }
                }else{

                    Log.d(TAG,"soket is connected");
                    client.close();
                }
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respText.setText("");
            }
        });
    }
    String getDstAddr(){
        String dstAddr = atDstAddr.getText().toString().trim();
        if(dstAddr.length()==0){
            respText.setText(respText.getText().toString()+"\nError IP!\n");
            return null;
        }

        return dstAddr;
    }
    int getDstPort(){
        String dstPort = atDstPort.getText().toString().trim();
        int port;
        try{
            port = Integer.parseInt(dstPort);
        }catch (Exception e){
            Log.d(TAG,"Error port" );
            respText.setText(respText.getText().toString()+"\nError Port!\n");
            return -1;
        }finally {
        }
        return port;
    }
    void saveDst(String dstAddr, int port){
        SharedPreferences.Editor editor = ATCommandMain.at_sharedData.edit();
        editor.putString("dstAddr",dstAddr);
        editor.putInt("port",port);
        editor.commit();

    }
    void recoverDst(){
        String dstAddr =  ATCommandMain.at_sharedData.getString("dstAddr",null);
        int port = ATCommandMain.at_sharedData.getInt("port",-1);
        Log.d(TAG,dstAddr+": "+port);
        if(dstAddr!=null){
            atDstAddr.setText(dstAddr);
        }
        if(port!=-1){
            atDstPort.setText(String.valueOf(port));
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
