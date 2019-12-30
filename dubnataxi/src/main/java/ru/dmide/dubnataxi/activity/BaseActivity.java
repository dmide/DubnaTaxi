package ru.dmide.dubnataxi.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import ru.dmide.dubnataxi.R;

/**
 * Created by dmide on 09/03/14.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static <T extends View> T viewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setLogo(R.drawable.ic_launcher);
        ab.setDisplayUseLogoEnabled(true);
    }
}
