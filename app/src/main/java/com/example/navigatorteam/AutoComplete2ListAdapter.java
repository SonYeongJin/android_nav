package com.example.navigatorteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skt.tmap.TMapAutoCompleteV2;

import java.util.ArrayList;

public class AutoComplete2ListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<TMapAutoCompleteV2> itemList;

    public AutoComplete2ListAdapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
    }

    public void clear() {
        this.itemList.clear();
    }

    public void setItemList(ArrayList<TMapAutoCompleteV2> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
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

        TMapAutoCompleteV2 item = itemList.get(position);
        holder.tv.setText(item.keyword);

        return cView;
    }

    private class ViewHolder {

        TextView tv;

        public ViewHolder(View v) {
            tv = v.findViewById(R.id.autoCompleteText);
        }
    }
}
