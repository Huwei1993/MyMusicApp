package com.haoxue.zixueplayer;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class AboutActivity extends Activity {

//    private Drawable oldBackground = null;
//    private int currentColor = 0x98000000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //去标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);
//        changeColor(currentColor);
    }

//    private void changeColor(int newColor) {
//
//
//        // change ActionBar color just if an ActionBar is available
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//
//            Drawable colorDrawable = new ColorDrawable(newColor);
//            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
//            LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});
//
//            if (oldBackground == null) {
//                getActionBar().setBackgroundDrawable(ld);
//
//            } else {
//                TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldBackground, ld});
//                getActionBar().setBackgroundDrawable(td);
//                td.startTransition(200);
//
//            }
//
//            oldBackground = ld;
//
//            getActionBar().setDisplayShowTitleEnabled(false);
//            getActionBar().setDisplayShowTitleEnabled(true);
//
//        }
//
//
//    }
}
