package ru.dmide.dubnataxi.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.dmide.dubnataxi.R;

public class InfoActivity extends BaseActivity {
    public static final int REQUEST_CODE = 1337;
    public static final int RESULT_CODE_RATED = 1;

    @BindView(R.id.mail_to)
    TextView mailTo;
    @BindView(R.id.sources)
    TextView sources;
    @BindView(R.id.rate_btn)
    TextView rateBtn;

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
            setResult(RESULT_CODE_RATED);
            Uri uri = Uri.parse(getString(R.string.google_play_link));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
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
}
