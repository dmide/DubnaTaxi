package ru.dmide.dubnataxi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by dmide on 09/03/14.
 */
public abstract class BaseActivity extends ActionBarActivity {
    public <T extends View> T viewById(int id){
        return (T) findViewById(id);
    }

    public static <T extends View> T viewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }
}
