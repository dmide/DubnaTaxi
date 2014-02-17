package ru.dmide.dubnataxi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dmide on 16/02/14.
 */
public class PhonesAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private final int listItemHeight;
    private ArrayList<String> phones;
    private MainActivity activity;

    public PhonesAdapter(MainActivity activity) {
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        listItemHeight = (int) activity.getResources().getDimension(R.dimen.list_item_height);
    }

    public void init(ArrayList<String> phones) {
        this.phones = phones;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.phone_number_tv);
        textView.setHeight(listItemHeight);
        String phone = phones.get(position);
        textView.setText(phone);
        if (activity.getCalledNumbers().contains(phone)){
            convertView.findViewById(R.id.called).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.called).setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return phones.size();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
