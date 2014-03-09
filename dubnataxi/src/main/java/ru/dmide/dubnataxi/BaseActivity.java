package ru.dmide.dubnataxi;

import android.app.Activity;
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
}
