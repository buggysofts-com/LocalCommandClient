package com.ruet_cse_1503050.ragib.localcommandcilent;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Process;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnlineDeviceListActivity extends AppCompatActivity {

    private ListView OnlineDeviceList;
    private TextView ConnectionStatus;
    private ProgressBar ConnectionProgress;

    private DatagramSocket activity_scanner_socket=null;
    private DatagramSocket connectionSoc=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_online_device_list);



        OnlineDeviceList=findViewById(R.id.OnlineDeviceList);
        ConnectionStatus=findViewById(R.id.ConnectionStatus);
        ConnectionProgress=findViewById(R.id.ConnectionProgress);

        InitalizeData();

        new Thread(new Runnable() {
            @Override
            public void run() {

                for(;;){

                    if(isConnectedToWifi()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                OnlineDeviceList.setEnabled(true);
                                ConnectionStatus.setText("Searching for online devices...");
                                ConnectionProgress.setIndeterminate(true);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                OnlineDeviceList.setEnabled(false);
                                ConnectionStatus.setText("Searching for network...");
                                ConnectionProgress.setIndeterminate(true);
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

        new Thread(new Runnable() {

            private List<Pair<String,String>> datas=new ArrayList<>(0);
            private OnlineListAdapter adapter=new OnlineListAdapter(OnlineDeviceListActivity.this,R.layout.online_list_node,datas);
            @Override
            public void run() {


                try {
                    activity_scanner_socket=new DatagramSocket(UtilCollections.ACTIVITY_SCANNER_PORT);
                    activity_scanner_socket.setSoTimeout(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] packet_data=new byte[128];
                DatagramPacket packet=new DatagramPacket(packet_data,packet_data.length);
                OnlineDeviceList.setAdapter(adapter);

                for (;;){

                    for(int i=0;i<100;++i){
                        try {
                            activity_scanner_socket.receive(packet);
                        } catch (Exception e) {
                            datas.clear();
                            break;
                        }
                        AddToList(new Pair<String, String>(
                                new String(packet.getData(),packet.getOffset(),packet.getLength()),
                                packet.getAddress().getHostAddress()
                        ));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    datas.clear();
                }

            }

            private void AddToList(Pair<String,String> val){
                int start=0;
                int end=datas.size()-1;
                int mid=(start+end)/2;
                int res_val;
                while (start<=end){
                    res_val=datas.get(mid).second.compareTo(val.second);
                    if(res_val<0) end=mid-1;
                    else if(res_val>0) start=mid+1;
                    else return;
                    mid=(start+end)/2;
                }
                datas.add(mid,val);
            }

        }).start();

        OnlineDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            try{
                                connectionSoc=new DatagramSocket(UtilCollections.CONNECTION_PORT_OWN);
                                connectionSoc.setSoTimeout(10000);
                            }catch (Exception e){ e.printStackTrace(); }

                            final String TargetDeviceIP=((TextView)(view.findViewById(R.id.DeviceIP))).getText().toString();
                            final  String TargetDeviceName=((TextView)(view.findViewById(R.id.DeviceName))).getText().toString();

                            WifiManager wifiManager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            UtilCollections.MyDeviceAddr=Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OnlineDeviceListActivity.this, "Connecting to "+TargetDeviceName+" ("+TargetDeviceIP+")", Toast.LENGTH_SHORT).show();
                                }
                            });

                            byte[] connectionData=(UtilCollections.ID+'|'+UtilCollections.MyDeviceName+'|'+UtilCollections.MyDeviceAddr+'|'+"echo x").getBytes();
                            try{
                                connectionSoc.send(new DatagramPacket(connectionData,connectionData.length,InetAddress.getByName(TargetDeviceIP),UtilCollections.CONNECTION_PORT_NOT_OWN));
                            }catch (Exception e){ e.printStackTrace(); }

                            DatagramPacket response_packet=null;
                            try {
                                connectionSoc.receive(response_packet=new DatagramPacket(new byte[5],5));
                            }catch (Exception e){
                                //e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(OnlineDeviceListActivity.this, "Connection timed out", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            if(new String(response_packet.getData(),response_packet.getOffset(),response_packet.getLength()).equals("_111_")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        // Todo go to next activity.i.e the command sender activity
                                        UtilCollections.TargetName=TargetDeviceName;
                                        UtilCollections.TargetAddr=TargetDeviceIP;
                                        startActivity(new Intent(OnlineDeviceListActivity.this,CommandPortalActivity.class));

                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(OnlineDeviceListActivity.this, "Connection denied!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    private void InitalizeData() {
        UtilCollections.ID_Storeage=new File(getFilesDir().getAbsolutePath()+File.separator+"ID_STORAGE");
        UtilCollections.DeviceNameDataFile=new File(getFilesDir().getAbsolutePath()+File.separator+"DEV_NAME");
        if(!UtilCollections.ID_Storeage.exists()) {
            UtilCollections.ID=Long.toString(new Date().getTime());
            UtilCollections.WriteToFile(
                    UtilCollections.ID_Storeage,
                    UtilCollections.ID.getBytes(),
                    false
            );
        }
        if(!UtilCollections.DeviceNameDataFile.exists()) {

            final AlertDialog.Builder builder=new AlertDialog.Builder(OnlineDeviceListActivity.this);
            final View view=getLayoutInflater().inflate(R.layout.device_name_specifier_layout,null);
            final EditText DevName=view.findViewById(R.id.dev_name);

            builder.setCancelable(false);
            builder.setView(view);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String DeviceName=DevName.getText().toString();
                    UtilCollections.WriteToFile(
                            UtilCollections.DeviceNameDataFile,
                            DeviceName.getBytes(),
                            false
                    );
                    UtilCollections.MyDeviceName=DeviceName;
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Process.killProcess(Process.myPid());
                }
            });

            final AlertDialog dialog=builder.create();
            DevName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length()>0) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    else dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            });

            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.online_device_finder_menu,menu);
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder=new AlertDialog.Builder(OnlineDeviceListActivity.this);
                builder.setTitle("About LocalCommand").
                setMessage("LocalCommand is an application to remotely execute command line instructions.\n\nPlease download the PC version of the program at:\nwww.[pc_version_addr].com/blablabla\n\nDeveloper: Nowrose Muhammad Ragib\nEmail: dev.ragib@gmail.com\nPhone: +8801723085831").
                setPositiveButton("Close",null).show();
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
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
