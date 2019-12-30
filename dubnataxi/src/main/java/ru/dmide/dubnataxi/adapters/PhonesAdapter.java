package ru.dmide.dubnataxi.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

public class PhonesAdapter extends ArrayAdapter {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

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
            holder.calledTV = viewById(convertView, R.id.called_time_ago);
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        String phone = phones.get(position);
        holder.phoneTV.setText(phone);

        boolean isCalled = model.isPhoneNumberCalled(phone);
        int calledVis = isCalled ? View.VISIBLE : View.GONE;
        long calledTime = model.getPhoneNumberCalledTime(phone);
        Context c = convertView.getContext();
        int bottomPadding = (int) c.getResources().getDimension(R.dimen.phone_textview_bottom_padding);

        holder.phoneTV.setPadding(0, 0, 0, isCalled ? bottomPadding : 0);
        holder.calledTV.setText(getTimeAgo(calledTime, c));
        holder.calledIcon.setVisibility(calledVis);
        holder.calledTV.setVisibility(calledVis);

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

    public static String getTimeAgo(long time, Context c) {
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return c.getString(R.string.time_unknown);
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return c.getString(R.string.time_just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return c.getString(R.string.time_a_minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + c.getString(R.string.time_minutes_ago);
        } else {
            Date date = new Date(time);
            if (diff < DAY_MILLIS) {
                String day = DateUtils.isToday(time) ? c.getString(R.string.time_today) : c.getString(R.string.time_yesterday);
                return day + ", " + new SimpleDateFormat("HH:mm").format(date);
            } else {
                return new SimpleDateFormat("dd MMM, HH:mm").format(date);
            }
        }
    }

    private static class ViewHolder {
        TextView phoneTV, calledTV;
        View calledIcon;
    }
}
