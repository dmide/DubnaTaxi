package ru.dmide.dubnataxi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        TextView mailTo = viewById(R.id.mail_to);
        mailTo.setText(Html.fromHtml("<a href=\"mailto:revdmide@gmail.com\">" + getString(R.string.mail_to) + "</a>"));
        mailTo.setMovementMethod(LinkMovementMethod.getInstance());

        TextView sources = viewById(R.id.sources);
        sources.setText(Html.fromHtml(getString(R.string.sources) + " <a href=\"https://github.com/dmide/DubnaTaxi\">github.com</a>"));
        sources.setMovementMethod(LinkMovementMethod.getInstance());

        Button play_btn = viewById(R.id.rate_btn);
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.google_play_link));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

}
