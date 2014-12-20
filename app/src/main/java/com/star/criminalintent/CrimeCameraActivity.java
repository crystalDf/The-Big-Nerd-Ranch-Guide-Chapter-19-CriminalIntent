package com.star.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

public class CrimeCameraActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 隐藏操作栏，实践无效
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return new CrimeCameraFragment();
    }

}
