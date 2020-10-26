package com.znwhahaha;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigInit implements Serializable {
    static String Fusionindex = null;
    static String Rowkey =null;
    static String Compareindex = null;
    static String Outputtable = null;
    static ArrayList<String> Outputtableindex = null;
    static int ID = 0;
    public static void initIndexRule(String filepath,String tablename) throws IOException {
        Fusionindex = ReadConfigFile.ReadConfigItem(filepath,tablename+"表融合参数=").get(0).split("<=>")[0];
        Rowkey = ReadConfigFile.ReadConfigItem(filepath,tablename+"表融合参数=").get(0).split("<=>")[1];
        Compareindex = ReadConfigFile.ReadConfigItem(filepath,tablename+"表融合参数=").get(0).split("<=>")[2];
        Outputtableindex = ReadConfigFile.ReadConfigItem(filepath,tablename+"表融合完毕表qualifier=");
    }
}
