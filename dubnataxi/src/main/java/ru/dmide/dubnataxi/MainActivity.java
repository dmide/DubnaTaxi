package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends BaseActivity {
    public static final String MODEL = "MODEL";
    private static final String TO_RATE_OR_NOT_TO_RATE = "TO_RATE_OR_NOT_TO_RATE";

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
                        updateContent(false);
                    }
                })
                .setup(pullToRefreshLayout);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        model = (ModelFragment) fragmentManager.findFragmentByTag(MODEL);
        if (model == null) {
            model = new ModelFragment();
            model.setRetainInstance(true);
            fragmentManager.beginTransaction().add(model, MODEL);
            updateContent(true);
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
            case R.id.info:
                Intent i = new Intent(this, InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_settings:
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
                    preferences.edit().putBoolean(TO_RATE_OR_NOT_TO_RATE, false).apply();
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
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    public SharedPreferences getSharedPreferences() {
        return preferences;
    }

    public void processContent(LinkedHashMap<String, ArrayList<String>> taxiNumbersTree) {
        model.initContent(taxiNumbersTree);

        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(model.getServicesAdapter());
        animAdapter.setAbsListView(servicesListView);
        servicesListView.setAdapter(animAdapter);

        servicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int groupPos, long id) {
                onServiceClick(groupPos);
            }
        });
    }

    private void onServiceClick(final int groupPos) {
        model.initNumbers(groupPos);
        ListView phonesList = new ListView(this);
        final AlertDialog dialog = new AlertDialog.Builder(this).
                setView(phonesList).
                create();
        SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(model.getPhonesAdapter(), new OnDismissCallback() {
            @Override
            public void onDismiss(AbsListView absListView, int[] ints) {
                String number = (String) absListView.getItemAtPosition(ints[0]);
                controller.deleteNumber(number);
                boolean isEmpty = !model.initNumbers(groupPos);
                if (isEmpty) {
                    controller.deleteCurrentService();
                    dialog.dismiss();
                }
            }
        });
        dismissAdapter.setAbsListView(phonesList);
        phonesList.setAdapter(dismissAdapter);
        setChildsListener(phonesList);
        dialog.show();
    }

    private void setChildsListener(ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView numberTV = viewById(view, R.id.phone_number_tv);
                String number = numberTV.getText().toString();
                controller.callNumber(number);
            }
        });
    }

    private void updateContent(boolean useCache) {
        if (!useCache) {
            controller.clearDeletedValues();
        }
        new ContentLoadTask(this, pullToRefreshLayout, useCache).execute();
    }
}
