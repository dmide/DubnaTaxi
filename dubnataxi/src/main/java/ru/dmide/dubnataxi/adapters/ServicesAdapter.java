package ru.dmide.dubnataxi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import androidx.annotation.NonNull;
import ru.dmide.dubnataxi.ModelFragment;
import ru.dmide.dubnataxi.R;
import ru.dmide.dubnataxi.ViewHelper;
import ru.dmide.dubnataxi.activity.MainActivity;

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
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.service_view, null);
            holder = new ViewHolder(viewById(convertView, R.id.shadow_up),
                    viewById(convertView, R.id.shadow_down),
                    viewById(convertView, R.id.text),
                    viewById(convertView, R.id.phones_list));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(model.getServices().get(position));
        if (getItemViewType(position) == SELECTED) {
            ViewHelper.applyVisibility(new View[]{holder.phonesList, holder.shadowUp, holder.shadowDown}, View.VISIBLE);
        } else {
            ViewHelper.applyVisibility(new View[]{holder.phonesList, holder.shadowUp, holder.shadowDown}, View.GONE);
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

    public static class ViewHolder {
        private static int shadowsHeight = 0;

        public final View shadowUp;
        public final View shadowDown;
        public final ListView phonesList;
        final TextView text;

        ViewHolder(View shadowUp, View shadowDown, TextView text, ListView phonesList) {
            this.shadowUp = shadowUp;
            this.shadowDown = shadowDown;
            this.text = text;
            this.phonesList = phonesList;
            if (shadowsHeight == 0){
                shadowsHeight = shadowDown.getLayoutParams().height * 2;
            }
        }

        public int shadowsHeight() {
            return shadowsHeight;
        }
    }
}