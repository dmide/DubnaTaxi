package ru.dmide.dubnataxi;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends ActionBarActivity {

    private ExpandableListView list;
    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ExpandableListView) findViewById(R.id.list);
        listAdapter = new ListAdapter(this);
        new ContentLoadTask(this).execute();
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
            case R.id.action_update:
                new ContentLoadTask(this).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void drawContent(LinkedHashMap<String, ArrayList<String>> content){
        listAdapter.init(content);
        list.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }
}
