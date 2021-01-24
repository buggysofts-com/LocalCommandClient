package com.ruet_cse_1503050.ragib.localcommandcilent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.content.Context;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OnlineListAdapter extends ArrayAdapter<Pair<String,String>> {

    public static class Holder{
        ImageView icon;
        TextView name;
        TextView ip;
    }

    public OnlineListAdapter(@NonNull Context context, int resource, @NonNull List<Pair<String, String>> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Holder H;
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.online_list_node,parent,false);
            H=new Holder();
            H.icon=convertView.findViewById(R.id.com_ic);
            H.name=convertView.findViewById(R.id.DeviceName);
            H.ip=convertView.findViewById(R.id.DeviceIP);
            convertView.setTag(H);
        } else {
            H=(Holder) convertView.getTag();
        }

        H.icon.setImageResource(R.drawable.computer_icon);
        H.name.setText(getItem(position).first);
        H.ip.setText(getItem(position).second);

        return (convertView);
    }
}
