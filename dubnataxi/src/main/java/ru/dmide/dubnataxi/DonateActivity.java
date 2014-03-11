package ru.dmide.dubnataxi;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.sufficientlysecure.donations.DonationsFragment;

public class DonateActivity extends ActionBarActivity {
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAif5i4DlVrmy1/breLbTA3X17Su2819taOLSsh8AZauTS/N6lwVAIi0vWVQPeWx4iEzgXc0h8gGBMoK/x56KR4gs4pUWfw6ZyeZdn1MOaePqCxKHKvUa5tigCJQVgpRYeCdR58shTGDYOttd2gmnKmRxeZcGa65h9r91UfcrVLWVTl3k67Sae0zEbZt8tZZTjNG3zzGKBoZtxmYZU8kgbAuKMtCSKUlUK7FaG+/hZxpCzcT7tmEXrRx4AAE9phlfdR9ebYdK9oJMJbfCUoQfnmdtbtGzotNqCKgCgxNK9ChiSL0TpUv+wJ2LkkdlBivHv1BakqpJCwe3tawABah2XYwIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"ntpsync.donation.1",
            "ntpsync.donation.3", "ntpsync.donation.5", "ntpsync.donation.10", "ntpsync.donation.15",
            "ntpsync.donation.25"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        if (savedInstanceState == null) {
            DonationsFragment donationsFragment = DonationsFragment.newInstance(false, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
                    getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null,
                    null, false, null, null);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, donationsFragment)
                    .commit();
        }
    }
}
