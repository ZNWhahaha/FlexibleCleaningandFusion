package com.znwhahaha;

import java.io.IOException;
import java.security.Key;

public class ConfigInit {
    //比较参数
    static String Compare = null;
    //主关联表关联主键
    static String Key = null;
    //关联副表的值
    static String Bullet = null;
    //关联目标表
    static String Targettable = null;
    //关联副表
    static String Bullettable = null;
    //输出表
    static String Outputtable = null;

    public static void initIndexRule(String filepath,String tablename1,String tablename2) throws IOException {
        Targettable = tablename1;
        Bullettable = tablename2;
        Key = ReadConfigFile.ReadConfigItem(filepath,tablename1+","+tablename2+"关联参数key=").get(0);
        Compare = ReadConfigFile.ReadConfigItem(filepath,tablename1+","+tablename2+"关联参数compare=").get(0);
        Bullet = ReadConfigFile.ReadConfigItem(filepath,tablename1+","+tablename2+"关联参数bullet=").get(0);
    }
}
