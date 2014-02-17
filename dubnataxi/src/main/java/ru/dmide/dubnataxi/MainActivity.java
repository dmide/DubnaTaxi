package ru.dmide.dubnataxi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity {

    private static final String CALLED_NUMS = "CALLED_NUMS";
    private final ArrayList<ArrayList<String>> numberLists = new ArrayList<ArrayList<String>>();
    private ListView servicesList;
    private ServicesAdapter servicesAdapter;
    private PhonesAdapter phonesAdapter;
    private PullToRefreshLayout pullToRefreshLayout;
    private Set<String> calledNumbers = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        servicesList = (ListView) findViewById(R.id.list);
        servicesList.setDivider(null);
        servicesAdapter = new ServicesAdapter(this);
        phonesAdapter = new PhonesAdapter(this);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetCalledNumbersTask().execute(this);
        }
        updateContent(true);
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

    protected <T extends View> T viewById(int id) {
        return (T) findViewById(id);
    }

    protected <T extends View> T viewById(View parent, int id) {
        return (T) parent.findViewById(id);
    }

    public void processContent(LinkedHashMap<String, ArrayList<String>> taxiNumbersTree) {
        numberLists.clear();
        for (ArrayList<String> numbers : taxiNumbersTree.values()) {
            numberLists.add(numbers);
        }
        servicesAdapter.init(new ArrayList<String>(taxiNumbersTree.keySet()));
        AnimationAdapter animAdapter = new ScaleInAnimationAdapter(servicesAdapter);

        animAdapter.setAbsListView(servicesList);
        servicesList.setAdapter(animAdapter);
        servicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                phonesAdapter.init(numberLists.get(position));
                ListView phonesList = new ListView(MainActivity.this);
                SwipeDismissAdapter dismissAdapter = new SwipeDismissAdapter(phonesAdapter, new OnDismissCallback() {
                    @Override
                    public void onDismiss(AbsListView absListView, int[] ints) {

                    }
                });
                dismissAdapter.setAbsListView(phonesList);
                phonesList.setAdapter(dismissAdapter);
                setChildsListener(phonesAdapter, phonesList);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).
                        setView(phonesList).
                        create();
                dialog.show();
            }
        });
    }

    private void setChildsListener(final PhonesAdapter phonesAdapter, ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView numberTV = viewById(view, R.id.phone_number_tv);
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String number = numberTV.getText().toString();
                calledNumbers.add(number);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new SaveCalledNumbersTask().execute(MainActivity.this);
                }
                callIntent.setData(Uri.parse("tel:" + number));
                startActivity(callIntent);
                phonesAdapter.setSelected(position);
                servicesAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateContent(boolean useCache) {
        new ContentLoadTask(this, pullToRefreshLayout, useCache).execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class GetCalledNumbersTask extends AsyncTask<Activity, Void, Void> {
        @Override
        protected Void doInBackground(Activity... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(params[0]);
            calledNumbers = preferences.getStringSet(CALLED_NUMS, Collections.EMPTY_SET);
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SaveCalledNumbersTask extends AsyncTask<Activity, Void, Void> {
        @Override
        protected Void doInBackground(Activity... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(params[0]);
            preferences.edit().putStringSet(CALLED_NUMS, calledNumbers).commit();
            return null;
        }
    }
}
