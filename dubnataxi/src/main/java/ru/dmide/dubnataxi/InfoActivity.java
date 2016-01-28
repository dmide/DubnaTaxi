package ru.dmide.dubnataxi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InfoActivity extends BaseActivity {

    @Bind(R.id.mail_to)
    TextView mailTo;
    @Bind(R.id.sources)
    TextView sources;
    @Bind(R.id.rate_btn)
    TextView rateBtn;
    @Bind(R.id.donate_btn)
    TextView donateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mailTo.setText(Html.fromHtml(getString(R.string.mail_to)));
        mailTo.setMovementMethod(LinkMovementMethod.getInstance());

        sources.setText(Html.fromHtml(getString(R.string.sources)));
        sources.setMovementMethod(LinkMovementMethod.getInstance());

        rateBtn.setOnClickListener(v -> {
            saveHasBeenRated();
            Uri uri = Uri.parse(getString(R.string.google_play_link));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        donateBtn.setOnClickListener(v -> {
            saveHasBeenRated();
            Intent i = new Intent(InfoActivity.this, DonateActivity.class);
            startActivity(i);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveHasBeenRated() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(MainActivity.TO_RATE_OR_NOT_TO_RATE, false)
                .commit();
    }
}
