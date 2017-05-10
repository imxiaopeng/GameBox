package com.mxth.gamebox;

import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/28.
 */

public class Utils {
    static  Toast toas = Toast.makeText(VApp.getContext(),"",Toast.LENGTH_SHORT);
    public static void toast(String msg){
        toas.setText(msg);
        toas.setDuration(Toast.LENGTH_SHORT);
        toas.show();
    }
}
