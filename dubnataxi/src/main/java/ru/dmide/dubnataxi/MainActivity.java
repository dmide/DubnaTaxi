package ru.dmide.dubnataxi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

public class MainActivity extends ActionBarActivity {
    public static String MODEL = "MODEL";

    private ModelFragment model;
    private Controller controller;
    private ListView servicesListView;

    private PullToRefreshLayout pullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        servicesListView = (ListView) findViewById(R.id.list);
        servicesListView.setDivider(null);

        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
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
        if (model == null){
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
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    public <T extends View> T viewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

    protected <T extends View> T viewById(int id) {
        return (T) findViewById(id);
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
        new ContentLoadTask(this, pullToRefreshLayout, useCache).execute();
        if (!useCache) {
            controller.clearDeletedValues();
        }
    }
}
