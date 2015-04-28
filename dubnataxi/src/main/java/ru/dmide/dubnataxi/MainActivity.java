package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends BaseActivity {
    public static final String MODEL = "MODEL";
    public static final String TO_RATE_OR_NOT_TO_RATE = "TO_RATE_OR_NOT_TO_RATE";

    private ModelFragment model;
    private Controller controller;
    private ListView servicesListView;
    private SharedPreferences preferences;
    private PullToRefreshLayout pullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);

        servicesListView = viewById(R.id.list);
        servicesListView.setDivider(null);

        pullToRefreshLayout = viewById(R.id.ptr_layout);

        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        updateContent(false, false);
                    }
                })
                .setup(pullToRefreshLayout);

        DefaultHeaderTransformer headerTransformer = (DefaultHeaderTransformer) pullToRefreshLayout.getHeaderTransformer();
        headerTransformer.setProgressBarColor(getResources().getColor(R.color.orange));

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        model = (ModelFragment) fragmentManager.findFragmentByTag(MODEL);
        if (model == null) {
            model = new ModelFragment();
            model.setRetainInstance(true);
            fragmentManager.beginTransaction().add(model, MODEL);
        }
        model.init(this);
        controller = new Controller(model);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                updateContent(false, true);
                return true;
            case R.id.info:
                Intent i = new Intent(this, InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_where:
                Uri uri = Uri.parse("geo:");
                Intent w = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(w);
                Toast.makeText(this, getString(R.string.face),Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (preferences.getBoolean(TO_RATE_OR_NOT_TO_RATE, true)) {
            View rateDialog = View.inflate(this, R.layout.rate_dialog, null);
            CheckBox checkBox = viewById(rateDialog, R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preferences.edit().putBoolean(TO_RATE_OR_NOT_TO_RATE, !isChecked).commit();
                }
            });
            new AlertDialog.Builder(this)
                    .setView(rateDialog)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, InfoActivity.class);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setInverseBackgroundForced(true)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    public SharedPreferences getSharedPrefs() {
        return preferences;
    }

    void initServicesList(){
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(model.getServicesAdapter());
        animAdapter.setAbsListView(servicesListView);
        servicesListView.setAdapter(animAdapter);

        servicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int groupPos, long id) {
                controller.onServiceClick(groupPos, MainActivity.this);
            }
        });
    }

    ModelFragment getModel() {
        return model;
    }

    void updateContent(boolean useCache, boolean clear) {
        if (pullToRefreshLayout != null) {
            pullToRefreshLayout.setRefreshing(true);
        }
        if (clear) {
            controller.clearDeletedValues();
        }
        new ContentLoadTask(this, pullToRefreshLayout, useCache).execute();
    }
}
