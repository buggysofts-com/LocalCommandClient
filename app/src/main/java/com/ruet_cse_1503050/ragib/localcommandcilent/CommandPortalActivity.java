package com.ruet_cse_1503050.ragib.localcommandcilent;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class CommandPortalActivity extends AppCompatActivity {

    private ListView cmd_list;
    private EditText cmd_txt;
    private ImageButton send_cmd;
    private TextView target_desc;
    private ImageView connection_indicator;

    private DatagramSocket CMD_SenderSocket;
    private List<String> adapter_storage;
    private SentCommandAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_portal);

        cmd_list=findViewById(R.id.cmd_list);
        cmd_txt=findViewById(R.id.cmd_txt);
        send_cmd=findViewById(R.id.send_cmd);
        target_desc=findViewById(R.id.target_desc);
        connection_indicator=findViewById(R.id.connection_indicator);

        registerForContextMenu(cmd_list);

        target_desc.setText("Connected to: "+UtilCollections.TargetName+"("+UtilCollections.TargetAddr+")");

        InitializeData();
        StartConnectionCheckupModule();

        try {
            CMD_SenderSocket=new DatagramSocket(1919);
            CMD_SenderSocket.setSoTimeout(3000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        cmd_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) { send_cmd.setEnabled(s.length()>0); }
        });

        send_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String actual_command=cmd_txt.getText().toString();
                final byte[] connectionData=
                        (UtilCollections.ID+'|'+UtilCollections.MyDeviceName+'|'+UtilCollections.MyDeviceAddr+'|'+actual_command).getBytes();

                adapter.add(actual_command);
                adapter.notifyDataSetChanged();

                cmd_txt.setText(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            CMD_SenderSocket.send(new DatagramPacket(connectionData,connectionData.length,InetAddress.getByName(UtilCollections.TargetAddr),UtilCollections.CONNECTION_PORT_NOT_OWN));
                        }catch (Exception e){ e.printStackTrace(); }
                    }
                }).start();
            }
        });

        cmd_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cmd_list.showContextMenuForChild(view);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.portal_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                adapter.clear();
                return true;
            }
        });
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        final String cmd_str=((TextView)v.findViewById(R.id.cmd)).getText().toString();

        getMenuInflater().inflate(R.menu.cmd_context_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,cmd_str);
                startActivity(Intent.createChooser(sharingIntent,"Send Command Using..."));
                return true;
            }
        });
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                cmd_txt.setText(cmd_str);
                send_cmd.callOnClick();
                return true;
            }
        });
    }

    private void InitializeData() {
        adapter_storage=new ArrayList<>(0);
        adapter=new SentCommandAdapter(CommandPortalActivity.this,R.layout.sent_cmd,adapter_storage);
        cmd_list.setAdapter(adapter);
    }

    private void StartConnectionCheckupModule() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for(;;){

                    if(isConnectedToWifi()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cmd_list.setEnabled(true);
                                send_cmd.setEnabled(true);
                                connection_indicator.setImageDrawable(getResources().getDrawable(R.drawable.connected_icon));
                                cmd_txt.setEnabled(true);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                send_cmd.setEnabled(false);
                                cmd_list.setEnabled(false);
                                connection_indicator.setImageDrawable(getResources().getDrawable(R.drawable.connection_lost));
                                cmd_txt.setEnabled(false);
                                cmd_txt.setText(null);
                            }
                        });
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

        }).start();
    }

    private boolean isConnectedToWifi(){
        NetworkInfo info=getNetworkInfo();
        return (info!=null && info.isConnected() && info.getType()==ConnectivityManager.TYPE_WIFI);
    }

    private NetworkInfo getNetworkInfo(){
        ConnectivityManager connectivityManager=null;
        try{
            connectivityManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        }catch (Exception e){
            e.printStackTrace();
        }
        return connectivityManager.getActiveNetworkInfo();
    }

}
