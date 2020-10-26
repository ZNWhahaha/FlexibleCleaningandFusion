package com.znwhahaha;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigInit implements Serializable {
    static ArrayList<String> tableindex = null;
    static ArrayList<String> indexcleanrule =null;
    static String outputtable = null;
    static HashMap<String,String> oldIndexToNew = new HashMap<>();
    static int ID = 0;
    
    public static void initIndexRule(String filepath,String tablename) throws IOException {

        tableindex=new ArrayList<String>();
        indexcleanrule=new ArrayList<String>();

        tableindex = ReadConfigFile.ReadConfigItem(filepath,tablename+"列族信息=");
        indexcleanrule = ReadConfigFile.ReadConfigItem(filepath,tablename+"表属性清洗规则=");
        for (String indexs: ReadConfigFile.ReadConfigItem(filepath,tablename+"表旧属性与新属性对应关系=")){
            String oldindex = indexs.split("<=>")[0];
            String newindex = indexs.split("<=>")[1];
            oldIndexToNew.put(oldindex,newindex);
        }
        ID = 0;
    }

}
