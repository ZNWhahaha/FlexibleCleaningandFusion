package com.znwhahaha;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class ConfigInit implements Serializable {
    static ArrayList<String> HbaseTablename = null;
    static int ID = 0;
    public static void initIndexRule(String filepath) throws IOException {

        HbaseTablename = ReadConfigFile.ReadConfigItem(filepath,"添加表ID属性的Hbase表名=");
    }
}
