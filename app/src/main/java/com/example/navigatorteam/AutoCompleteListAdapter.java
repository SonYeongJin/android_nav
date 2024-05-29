package com.example.navigatorteam;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AutoCompleteListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> itemList;

    public AutoCompleteListAdapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
    }

    public void clear() {
        this.itemList.clear();
        notifyDataSetChanged();
    }

    public void setItemList(ArrayList<String> itemList) {
        if (itemList != null) { // 아이템 목록이 null이 아닌 경우에만 처리
            this.itemList = itemList;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View cView = convertView;
        ViewHolder holder;
        if (cView == null) {
            cView = LayoutInflater.from(context).inflate(R.layout.layout_auto_complete, parent, false);
            holder = new ViewHolder(cView);
            cView.setTag(holder);
        } else {
            holder = (ViewHolder) cView.getTag();
        }

        String text = itemList.get(position);
        holder.tv.setText(text);

        return cView;
    }

    private class ViewHolder {

        TextView tv;

        public ViewHolder(View v) {
            tv = v.findViewById(R.id.autoCompleteText);
        }
    }
}
