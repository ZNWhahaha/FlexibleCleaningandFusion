package com.znwhahaha;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class ConfigInit implements Serializable {
    static ArrayList<String> tableindex = null;
    static ArrayList<String> indexcleanrule =null;
    static String outputtable = null;
    static int ID = 0;
    
    public static void initIndexRule(String filepath,String tablename) throws IOException {

        tableindex=new ArrayList<String>();
        indexcleanrule=new ArrayList<String>();

        tableindex = ReadConfigFile.ReadConfigItem(filepath,tablename+"列族信息=");
        for (String index:tableindex){
            indexcleanrule = ReadConfigFile.ReadConfigItem(filepath,tablename+"表属性清洗规则=");
        }
    }

}
