package com.ruet_cse_1503050.ragib.localcommandcilent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SentCommandAdapter extends ArrayAdapter<String> {

    private static class Holder{
        TextView cmd;
    }

    public SentCommandAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.sent_cmd,null);
            holder=new Holder();
            holder.cmd=convertView.findViewById(R.id.cmd);
            convertView.setTag(holder);
        } else {
            holder=(Holder)convertView.getTag();
        }

        holder.cmd.setText(getItem(position));

        return convertView;
    }
}
