package com.sensor_test;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private List<Bean> list;
    private Context context;

    public ListAdapter(List<Bean> list, Context context) {
        this.list = list;
        this.context = context;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            holder=new ViewHolder();
            convertView=View.inflate(context, R.layout.item,null);
            holder.title=convertView.findViewById(R.id.name);
            holder.address=convertView.findViewById(R.id.address);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        Bean bean=list.get(position);
        holder.title.setText(bean.name);
        holder.address.setText(bean.address);
        return convertView;
    }

    class ViewHolder{
        public TextView title;
        public TextView address;
    }
}
