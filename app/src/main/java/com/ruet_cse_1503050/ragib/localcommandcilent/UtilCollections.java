package com.ruet_cse_1503050.ragib.localcommandcilent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class UtilCollections {

    public static final int ACTIVITY_SCANNER_PORT=7181;
    public static final int CONNECTION_PORT_OWN=1294;
    public static final int CONNECTION_PORT_NOT_OWN=4921;

    public static File DeviceNameDataFile=null;
    public static File ID_Storeage=null;
    public static  String ID=null;
    public static String MyDeviceName=null;
    public static String MyDeviceAddr=null;

    public static String TargetName=null;
    public static String TargetAddr=null;

    public static void WriteToFile(File file,byte[] data,boolean append){
        FileOutputStream outputStream=null;
        try {
            outputStream=new FileOutputStream(file,append);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String ReadFile(File file){
        FileInputStream inputStream=null;
        byte[] data=new byte[((int) file.length())];
        try {
            inputStream=new FileInputStream(file);
            inputStream.read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(data);
    }

}
