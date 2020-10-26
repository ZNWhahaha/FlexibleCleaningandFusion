package com.znwhahaha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigInit {
    static String url = null;
    static String username = null;
    static String passwd = null;
    static ArrayList<String> tablename = null;
    static ArrayList<String> indexRel = null;
    static ArrayList<String> globalkeyvalue = null;
    public static void initIndexRule(String filepath) throws IOException {
        url = ReadConfigFile.ReadConfigItem(filepath,"Neo4j数据库url=").get(0);
        username = ReadConfigFile.ReadConfigItem(filepath,"Neo4j数据库用户名=").get(0);
        passwd = ReadConfigFile.ReadConfigItem(filepath,"Neo4j数据库密码=").get(0);
        tablename = ReadConfigFile.ReadConfigItem(filepath,"转化表名=");
        indexRel = ReadConfigFile.ReadConfigItem(filepath,"表属性关系=");
        globalkeyvalue = ReadConfigFile.ReadConfigItem(filepath,"Neo4j节点添加全局属性=");
    }
}
