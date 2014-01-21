package ru.dmide.dubnataxi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ListAdapter extends BaseExpandableListAdapter {

    private final MainActivity activity;
    private final LayoutInflater inflater;
    private final ArrayList<String> serviceNames = new ArrayList<String>();
    private final ArrayList<ArrayList<String>> numberLists = new ArrayList<ArrayList<String>>();
    private final int listItemHeight;

    public ListAdapter(MainActivity activity) {
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        listItemHeight = (int) activity.getResources().getDimension(R.dimen.list_item_height);
    }

    public void init(LinkedHashMap<String, ArrayList<String>> taxiNumbersTree) {
        serviceNames.clear();
        numberLists.clear();
        for (String name : taxiNumbersTree.keySet()) {
            serviceNames.add(name);
        }
        for (ArrayList<String> numbers : taxiNumbersTree.values()) {
            numberLists.add(numbers);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_view, null);
        }
        //don't need viewholder here because list is short
        TextView textView = (TextView) convertView;
        textView.setText(numberLists.get(groupPosition).get(childPosition));
        textView.setHeight(listItemHeight);
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.group_view, null);
        }
        TextView textView = (TextView) convertView;
        textView.setText(serviceNames.get(groupPosition));
        textView.setHeight(listItemHeight);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return numberLists.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return serviceNames.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}