package ru.dmide.dubnataxi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;

public class ServicesAdapter extends ArrayAdapter {

    private final LayoutInflater inflater;
    private ArrayList<String> services;
    private final int listItemHeight;
    private ModelFragment model;

    public ServicesAdapter(MainActivity activity, ModelFragment model) {
        this.model = model;
        inflater = activity.getLayoutInflater();
        listItemHeight = (int) activity.getResources().getDimension(R.dimen.list_item_height);
    }

    public void init(ArrayList<String> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_view, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text);
        textView.setText(services.get(position));
        textView.setHeight(listItemHeight);
        return convertView;
    }

    @Override
    public int getCount() {
        return services.size();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}