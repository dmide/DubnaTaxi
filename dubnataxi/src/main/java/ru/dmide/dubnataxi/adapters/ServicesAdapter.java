package ru.dmide.dubnataxi.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.nhaarman.listviewanimations.ArrayAdapter;

import ru.dmide.dubnataxi.activity.MainActivity;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

public class ServicesAdapter extends ArrayAdapter {
    private static final int UNSELECTED = 0;
    private static final int SELECTED = 1;

    private final LayoutInflater inflater;
    private final ModelFragment model;

    public ServicesAdapter(MainActivity activity, ModelFragment model) {
        this.model = model;
        inflater = activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.service_view, null);
        }
        TextView textView = viewById(convertView, R.id.text);
        textView.setText(model.getServices().get(position));
        if (getItemViewType(position) == SELECTED) {
            viewById(convertView, R.id.phones_list).setVisibility(View.VISIBLE);
            viewById(convertView, R.id.separator).setVisibility(View.VISIBLE);
        } else {
            viewById(convertView, R.id.phones_list).setVisibility(View.GONE);
            viewById(convertView, R.id.separator).setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return model.getServices().size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return model.isServiceSelected(getItem(position)) ? SELECTED : UNSELECTED;
    }

    @NonNull
    @Override
    public String getItem(int position) {
        return model.getServices().get(position);
    }
}