package ru.dmide.dubnataxi.adapters;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.Collections;
import java.util.List;

import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;

import static ru.dmide.dubnataxi.activity.BaseActivity.viewById;

public class PhonesAdapter extends ArrayAdapter {
    private static final int UNSELECTED = 0;
    private static final int SELECTED = 1;

    private final ModelFragment model;
    private final String serviceId;
    private List<String> phones = Collections.emptyList();

    public PhonesAdapter(ModelFragment model, String serviceId) {
        this.model = model;
        this.serviceId = serviceId;
        this.phones = model.getPhonesForService(serviceId);
    }

    @Override
    public void notifyDataSetChanged() {
        this.phones = model.getPhonesForService(serviceId);
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(model.getActivity());
            convertView = inflater.inflate(R.layout.phone_view, null);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            convertView.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.bg_default));
        }
        TextView textView = viewById(convertView, R.id.phone_number_tv);
        String phone = phones.get(position);
        textView.setText(phone);
        viewById(convertView, R.id.called).setVisibility(model.isPhoneNumberCalled(phone) ? View.VISIBLE : View.INVISIBLE);

        if (getItemViewType(position) == SELECTED) {
            convertView.setBackgroundResource(R.drawable.selected_state);
        } else {
            convertView.setBackgroundResource(R.drawable.default_state);
        }

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
}
