package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.dmide.dubnataxi.adapters.ServicesAdapter;

public class MainActivity extends BaseActivity implements ModelFragment.DataListener {
    public static final String MODEL = "MODEL";
    public static final String TO_RATE_OR_NOT_TO_RATE = "TO_RATE_OR_NOT_TO_RATE";

    private ModelFragment model;
    private Controller controller;
    private SharedPreferences preferences;

    @Bind(R.id.services_list)
    ListView servicesListView;
    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    private ServicesAdapter servicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        servicesListView.setDivider(null);
        swipeRefreshLayout.setOnRefreshListener(() -> model.loadContent(false, false));
        swipeRefreshLayout.setColorSchemeResources(
                R.color.orange,
                R.color.dark_grey);

        FragmentManager fragmentManager = getSupportFragmentManager();
        model = (ModelFragment) fragmentManager.findFragmentByTag(MODEL);
        if (model == null) {
            model = new ModelFragment();
            model.setRetainInstance(true);
            fragmentManager.beginTransaction().add(model, MODEL).commit();
            swipeRefreshLayout.setRefreshing(true);
        }
        controller = new Controller(model);
        initServicesList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.subscribe(this);
    }

    @Override
    protected void onPause() {
        model.saveUserActions();
        model.unsubscribe(this);
        super.onPause();
    }

    @Override
    public void onDataSetChanged() {
        swipeRefreshLayout.setRefreshing(false);
        servicesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataSetLoading() {
        swipeRefreshLayout.setRefreshing(true);
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
                model.loadContent(false, true);
                return true;
            case R.id.info:
                Intent i = new Intent(this, InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_where:
                Uri uri = Uri.parse("geo:");
                Intent w = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(w);
                Toast.makeText(this, getString(R.string.face), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (preferences.getBoolean(TO_RATE_OR_NOT_TO_RATE, true)) {
            View rateDialog = View.inflate(this, R.layout.rate_dialog, null);
            CheckBox checkBox = viewById(rateDialog, R.id.checkbox);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.edit().putBoolean(TO_RATE_OR_NOT_TO_RATE, !isChecked).commit());
            new AlertDialog.Builder(this)
                    .setView(rateDialog)
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        startActivity(new Intent(MainActivity.this, InfoActivity.class));
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                        finish();
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

    private void initServicesList() {
        servicesAdapter = new ServicesAdapter(this, model);
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(servicesAdapter);
        animAdapter.setAbsListView(servicesListView);
        servicesListView.setAdapter(animAdapter);
        servicesListView.setOnItemClickListener((parent, view, groupPos, id)
                -> {
            controller.onServiceClick(servicesAdapter.getItem(groupPos), MainActivity.this);
            servicesAdapter.notifyDataSetChanged();
        });
    }
}
