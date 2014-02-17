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
    private Set<Integer> calledNumbers = new HashSet<Integer>();

    public PhonesAdapter(MainActivity activity) {
        inflater = activity.getLayoutInflater();
        listItemHeight = (int) activity.getResources().getDimension(R.dimen.list_item_height);
    }

    public void init(ArrayList<String> phones) {
        this.phones = phones;
        notifyDataSetChanged();
    }

    public void setSelected(int position){
        calledNumbers.add(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.phone_number_tv);
        textView.setText(phones.get(position));
        textView.setHeight(listItemHeight);
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
