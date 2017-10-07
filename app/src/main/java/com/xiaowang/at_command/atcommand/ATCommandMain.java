package com.xiaowang.at_command.atcommand;


import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.widget.TextView;

public class ATCommandMain extends FragmentActivity {

    public static int screen_pix_x;
    public static int screen_pix_y;
    public static final String fileName = "_AT_COMMAND_SHARED_FILE";
    public static SharedPreferences at_sharedData= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atcommand_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        FragmentManager fmger = getSupportFragmentManager();
        viewPager.setAdapter(new ATCommandAdapter(fmger));
        at_sharedData = getSharedPreferences(fileName,0);

        //get screen parameter for future using
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_pix_x = size.x;
        screen_pix_y = size.y;

        //override strict mode default behavior to all network operation in main thread. However I have to accept the consequences:
        // the app will (in areas of spotty internet connection) become unresponsive and lock up, the user perceives slowness and has to do a force kill,
        // and you risk the activity manager killing your app and telling the user that the app has stopped.
        android.os.StrictMode.ThreadPolicy policy = new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
        android.os.StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ATSocketClient.close();
    }

}
class ATCommandAdapter extends FragmentPagerAdapter{

    public ATCommandAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fm = null;
        if(i==0) {
            fm = new Fragment1();
        }else if(i==1){
            fm = new Fragment2();
        }
        return fm;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position ==0){
            return "Commands";
        }else if(position ==1){
            return "Switches";
        }
        return null;
    }

}
