package com.xiaowang.at_command.atcommand;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Timer;

import java.util.Arrays;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by NINI on 2015/5/7.
 */
public class Fragment2 extends Fragment {
    final String TAG = "AT_TAG";
    final int temperatureIndex = 13;
    static final String btnNameKey = "BtnName";
    static final String btnCmdKey = "BtnCmd";
    static ATSocketClient.OnProgressUpdate sockeProgressCallback;
    final int searchForTempretureTimes=20;
    Map<Integer,Integer> idToIndex = new TreeMap<>(); //<Id,index>
    Map<Integer,Button> indexToButton =new TreeMap<>();//<index,button>
    TextView textTemperature;
    Map<Integer,ButtonAttribute> btnConfig = new TreeMap<>();
    Handler timerHandler = new TimerHandler();
    View popupWinLayout;
    PopupWindow popupWin;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment2, container, false);
       // popupWinLayout = inflater.inflate(R.layout.popup_layout,(ViewGroup) v.findViewById(R.id.fragment2_layout),false);
        popupWinLayout = inflater.inflate(R.layout.popup_layout,(ViewGroup) v,false);
        textTemperature =(TextView) v.findViewById(R.id.text_temp);
        initBtn(v);
        recoverConfig();
        implementConfig();
        addListener();
        initSocketCallback();
        return v;
    }
    void saveConfig(int k){
        if(btnConfig.containsKey(k)) {
            ButtonAttribute btnAttr = btnConfig.get(k);
            SharedPreferences.Editor editor = ATCommandMain.at_sharedData.edit();
            editor.putString(k + btnNameKey, btnAttr.name);
            editor.putString(k + btnCmdKey, btnAttr.command);
            editor.apply();
        }
    }
    interface SocketCallBackInit{
        void init();
    }

    class TimerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //ATSocketClient.removeCallBack(sockeProgressCallback);
            textTemperature.setText(R.string.text_temperature_fail);
            ATSocketClient.removeCallBack(sockeProgressCallback);
            indexToButton.get(temperatureIndex).setEnabled(true);
        }
    };
    void initSocketCallback(){
        class Temp implements  ATSocketClient.OnProgressUpdate,SocketCallBackInit{
            public int cnt;
            String str ="";
            Pattern p;
            Timer timer=null;
            TimerTask task;
            void ATSocketClient(){
                cnt = searchForTempretureTimes;
                Log.d(TAG,"Temp search Initialized");
            }
            public void init(){
                indexToButton.get(temperatureIndex).setEnabled(false);
                str="";
                cnt=searchForTempretureTimes;
                p = Pattern.compile("RESPATR:\\w+,(\\d+),0402,0000,(\\d+),29,(\\w+)");
                Log.d(TAG,"str and cnt Initialized");
                Log.d(TAG,"new timer");
                timer = new Timer();    //new is dengerous, it create a timer every clicked. this may lead memory and other problem. here, for easy, don't want to change.
                task = new TimerTask() {
                    @Override
                    public void run() {
                        //timerHandler.obtainMessage(1).sendToTarget();
                        timerHandler.obtainMessage().sendToTarget();
                    }
                };
                timer.schedule(task, 3000);

            }
            @Override
            public void onProgressUpdate(String values) {

                Log.d(TAG,"Temp search starting update");
                cnt--;
                if(cnt<0) {
                    timer.cancel();
                    indexToButton.get(temperatureIndex).setEnabled(true);
                    ATSocketClient.removeCallBack(sockeProgressCallback);
                    textTemperature.setText(R.string.text_temperature_fail);
                }
                str+=values;
                String[] s = str.split("\n");

                Log.d(TAG,"s is :"+ Arrays.toString(s));
                if(s.length>0) {
                    for (int i = 0; i < s.length;i++ ){
                        Log.d(TAG,"pre s[i] is :"+ s[i]);
                        if(s[i].length()<10) continue;
                        Log.d(TAG,"after s[i] is :"+ s[i]);
                        Matcher matcher = p.matcher(s[i]);
                        Log.d(TAG,"matcher is: "+matcher);
                        if(matcher.find()){
                            Log.d(TAG,"Found group(0)"+matcher.group(0));
                            ATSocketClient.removeCallBack(sockeProgressCallback);
                            if(matcher.group(2).compareTo("00")==0) {
                                textTemperature.setText(Integer.parseInt(matcher.group(3),16)/100.0
                                        +" \u2103");
                                timer.cancel();
                                indexToButton.get(temperatureIndex).setEnabled(true);

                            }else{
                                //fail
                            }
                        }
                        str=s[s.length-1];
                    }
                }
            }
        }


        sockeProgressCallback = new Temp();
    }
    void saveConfig(){
        SharedPreferences.Editor editor= ATCommandMain.at_sharedData.edit();
        for(int k : btnConfig.keySet()){
            ButtonAttribute btnAttr = btnConfig.get(k);
            editor.putString(k+btnNameKey,btnAttr.name);
            editor.putString(k+btnCmdKey,btnAttr.command);
        }
        editor.apply();
    }
    void recoverConfig(){
        for(int k : indexToButton.keySet()) {
            String name = ATCommandMain.at_sharedData.getString(k + btnNameKey, null);
            String cmd = ATCommandMain.at_sharedData.getString(k+btnCmdKey,null);
            if(name!=null&&cmd!=null) {
                ButtonAttribute btnAttr = new ButtonAttribute(name,cmd);
                btnConfig.put(k, btnAttr);
            }
        }
    }
    void implementConfig(){
        for(int k : indexToButton.keySet()){
            implementConfig(k);
        }
    }
    void implementConfig(int k){
            if(btnConfig.containsKey(k)){
                ButtonAttribute btnAttr = btnConfig.get(k);
                if(btnAttr.name.length()==0){
                    switch (k){
                        case temperatureIndex:
                            indexToButton.get(k).setText(R.string.btn_get_temperature);
                            break;
                        default:
                            indexToButton.get(k).setText("Button"+ k);
                            break;
                    }
                }else {
                    indexToButton.get(k).setText(btnConfig.get(k).name);
                }
            }
    }

    public class BtnListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Button btn = (Button) v;
            int index = idToIndex.get(btn.getId());
            ButtonAttribute btnAttr = btnConfig.get(index);
            PrintWriter output = ATSocketClient.getOutputStream();
            if (output != null && btnAttr!=null) {
                output.write(btnAttr.command);
                output.flush();
            }
            if(index == temperatureIndex){
                if(indexToButton.get(temperatureIndex).isEnabled()) {//in case double click which leads the founction inintial for two times and build two timers
                    textTemperature.setText(R.string.text_temperature_getting);
                    ((SocketCallBackInit) sockeProgressCallback).init();
                    ATSocketClient.addCallBack(sockeProgressCallback);
                }
            }
        }
    }

    void addListener(){
        BtnListener btnListener =new BtnListener();
        for(int k : indexToButton.keySet()){
            indexToButton.get(k).setOnClickListener(btnListener);
        }
        BtnLongClickListener listener = new BtnLongClickListener();
        listener.initBtnLongClickListener();
        for(int k : indexToButton.keySet()){

            Button btn = indexToButton.get(k);
            btn.setLongClickable(true);
            btn.setOnLongClickListener(listener);
        }
    }
    public class BtnLongClickListener implements  View.OnLongClickListener{
        int sourceBtnInndex;
        EditText text_cmd;
        EditText text_name;
        Button btnCancel;
        Button btnConfirm;
        void initBtnLongClickListener(){
            popupWin = new PopupWindow(popupWinLayout,ATCommandMain.screen_pix_x,
                    ATCommandMain.screen_pix_y/2,true);
            text_name= (EditText) popupWinLayout.findViewById(R.id.textBtnName);
            text_cmd= (EditText) popupWinLayout.findViewById(R.id.textBtnCmd);
            btnCancel = (Button)popupWinLayout.findViewById(R.id.btn_Cancel);
            btnConfirm = (Button)popupWinLayout.findViewById(R.id.btn_Confirm);
            Listener listener = new Listener();
            btnCancel.setOnClickListener(listener);
            btnConfirm.setOnClickListener(listener);

        }

        @Override
        public boolean onLongClick(View v) {
            popupWin.showAtLocation(popupWinLayout, Gravity.CENTER, 0, -ATCommandMain.screen_pix_y / 10);
            sourceBtnInndex = idToIndex.get(((Button) v).getId());
            Log.d(TAG,"sourceBtn1: "+sourceBtnInndex);
            //recover textfied
            if(btnConfig.containsKey(sourceBtnInndex)) {
                ButtonAttribute btnAttr = btnConfig.get(sourceBtnInndex);
                text_name.setText(btnAttr.name, TextView.BufferType.EDITABLE);
                text_cmd.setText(btnAttr.command);
                text_name.setSelection(btnAttr.name.length());//Without this sentence the EidtText will be error when Edited by users
                                        //new found:: the error is cause by Android suggestion and the popup window may not support this , so remove the suggestion in layout add sentence android:inputType="textNoSuggestions"
                text_cmd.setSelection(btnAttr.command.length());
                Log.d(TAG, "text_name.setText: " + btnAttr.name);
                if(sourceBtnInndex == temperatureIndex && text_name.getText().length()==0){
                    text_name.setText(R.string.btn_get_temperature, TextView.BufferType.EDITABLE);
                    text_name.setSelection(text_name.getText().length());
                    Log.d(TAG, "text_name.setText: " + R.string.btn_get_temperature);
                }
            }else if(sourceBtnInndex == temperatureIndex) {
                text_name.setText(R.string.btn_get_temperature, TextView.BufferType.EDITABLE);
                text_cmd.setText("");
                text_name.setSelection(text_name.getText().length());
                Log.d(TAG, "text_name.setText: " + R.string.btn_get_temperature);
            }
            else{
                    //dispaly hint text
                    text_name.setText("");
                    text_cmd.setText("");
                Log.d(TAG, "text_name.setText: " );

            }
            Log.d(TAG, "Starting return");
            return true;
        }
        class Listener implements View.OnClickListener{


            @Override
            public void onClick(View v) {
                Log.d(TAG,"sourceBtn2: "+sourceBtnInndex);
                if(v.getId()==R.id.btn_Cancel){
                    popupWin.dismiss();
                    Log.d(TAG, "popupWin: dismiss");
                }else if(v.getId() == R.id.btn_Confirm){
                    Log.d(TAG, "Start confirm: ");
                    String name = text_name.getText().toString().trim();
                    String cmd = text_cmd.getText().toString().trim();
                    Log.d(TAG, "confirm content: " + name + "  " + cmd);
                    btnConfig.put(sourceBtnInndex,
                            new ButtonAttribute(name, cmd));
                    Log.d(TAG, "btnConfig.put: ");
                    saveConfig(sourceBtnInndex);
                    Log.d(TAG, "saveConfig(sourceBtnInndex): " + sourceBtnInndex);
                    implementConfig(sourceBtnInndex);
                    Log.d(TAG, "implementConfig ");
                    popupWin.dismiss();
                    Log.d(TAG, "popupWin2: dismiss");
                }
            }
        }
    }
    void initBtn(View v){
        int[] button_ids={
                R.id.button1,
                R.id.button2,
                R.id.button3,
                R.id.button4,
                R.id.button5,
                R.id.button6,
                R.id.button7,
                R.id.button8,
                R.id.button9,
                R.id.button10,
                R.id.button11,
                R.id.button12,
                R.id.button13
        };
        int indx =0;
        for(int i : button_ids){
            indx++;
            idToIndex.put(i, indx);
            indexToButton.put(indx, (Button) v.findViewById(i));
        }
    }
    class ButtonAttribute{
        String name;
        String command;

        public ButtonAttribute(String name, String command) {
            this.name = name;
            this.command = command;
        }
    }
}
