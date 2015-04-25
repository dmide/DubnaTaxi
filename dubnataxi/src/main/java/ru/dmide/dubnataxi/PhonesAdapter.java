package ru.dmide.dubnataxi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;

import static ru.dmide.dubnataxi.BaseActivity.viewById;

/**
 * Created by dmide on 16/02/14.
 */
public class PhonesAdapter extends ArrayAdapter {
    private final LayoutInflater inflater;
    private final int listItemHeight;
    private ArrayList<String> phones;
    private MainActivity activity;
    private ModelFragment model;

    public PhonesAdapter(MainActivity activity, ModelFragment model) {
        this.activity = activity;
        this.model = model;
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
        TextView textView = viewById(convertView, R.id.phone_number_tv);
        textView.setHeight(listItemHeight);
        String phone = phones.get(position);
        textView.setText(phone);
        if (model.getCalledNumbers().contains(phone)) {
            viewById(convertView, R.id.called).setVisibility(View.VISIBLE);
        } else {
            viewById(convertView, R.id.called).setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return phones.size();
    }

    @Override
    public Object getItem(int position) {
        return phones.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
