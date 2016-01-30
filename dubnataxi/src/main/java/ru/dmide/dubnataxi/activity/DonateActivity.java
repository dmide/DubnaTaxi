package ru.dmide.dubnataxi.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.sufficientlysecure.donations.DonationsFragment;

import ru.dmide.dubnataxi.R;

public class DonateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        if (savedInstanceState == null) {
            Resources r = getResources();
            DonationsFragment donationsFragment = DonationsFragment.newInstance(false, true,
                    getString(R.string.donate_key), r.getStringArray(R.array.donation_google_catalog_keys),
                    r.getStringArray(R.array.donation_google_catalog_values), false, null, null,
                    null, false, null, null);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, donationsFragment)
                    .commit();
        }
    }
}
