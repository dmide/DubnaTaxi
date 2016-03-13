package ru.dmide.dubnataxi.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

public class PhonesAdapter extends ArrayAdapter {
    private static final int UNSELECTED = 0;
    private static final int SELECTED = 1;

    private final ModelFragment model;
    private final String serviceId;
    private final LinkedList<View> convertViewsEager;
    private List<String> phones = Collections.emptyList();

    public PhonesAdapter(ModelFragment model, String serviceId) {
        this.model = model;
        this.serviceId = serviceId;
        this.phones = model.getPhonesForService(serviceId);

        //optimization to avoid inflation when the service view is animated (expanding)
        LayoutInflater inflater = LayoutInflater.from(model.getActivity());
        convertViewsEager = new LinkedList<>();
        for (int i = 0; i < phones.size() * 3; i++) {
            View view = inflater.inflate(R.layout.phone_view, null);
            convertViewsEager.add(view);
        }
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void notifyDataSetChanged() {
        this.phones = model.getPhonesForService(serviceId);
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            if (!convertViewsEager.isEmpty()) {
                convertView = convertViewsEager.pop();
            } else {
                // sometimes on 4.* system passes here nulls several times for the same position
                // I'm too lazy now to delve into SDK sources to find out why
                // so in these situations eager works only partly
                convertView = LayoutInflater.from(model.getActivity()).inflate(R.layout.phone_view, parent, false);
            }

            if (getItemViewType(position) == SELECTED) {
                convertView.setBackgroundResource(R.drawable.selected_state);
            } else {
                convertView.setBackgroundResource(R.drawable.phones_selector);
            }
            ViewHolder holder = new ViewHolder();
            holder.phoneTV = viewById(convertView, R.id.phone_number_tv);
            holder.calledIcon = viewById(convertView, R.id.called);
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        String phone = phones.get(position);
        holder.phoneTV.setText(phone);
        holder.calledIcon.setVisibility(model.isPhoneNumberCalled(phone) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    @Override
    public int getCount() {
        return phones.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return model.getLastCalledNumber().equals(getItem(position)) ? SELECTED : UNSELECTED;
    }

    @NonNull
    @Override
    public String getItem(int position) {
        return phones.get(position);
    }

    private static class ViewHolder {
        TextView phoneTV;
        View calledIcon;
    }
}
