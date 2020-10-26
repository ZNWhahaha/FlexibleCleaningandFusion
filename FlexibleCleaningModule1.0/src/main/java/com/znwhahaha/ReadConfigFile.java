package com.znwhahaha;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ReadConfigFile {
    /**
     * @ClassName : XMLAnalysis
     * @Description : 获取配置文件中设置的属性值
     * @param filepath  配置文件路径
     * @param itemname  属性标识字段
     * @Return : boolean
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
     */
    public static ArrayList<String> ReadConfigItem(String filepath, String itemname)throws IOException {
        ArrayList<String> arrayList = new ArrayList();
        //创建读取文本字符流
        InputStreamReader isr = new InputStreamReader(new FileInputStream(filepath), "UTF-8");
        BufferedReader in = new BufferedReader(isr);
        //行对象
        String line = "";
        //保存需要的数据
//        System.out.println(itemname.length());
        //循环遍历每行内容，截取需要的数据
        while((line = in.readLine())!=null)
        {
            if(line.indexOf(itemname)>-1 && !line.startsWith("##")) {
                arrayList.add(line.substring(line.indexOf(itemname)+itemname.length()));
            }
        }
        if (arrayList.isEmpty()){
            System.out.println("配置文件中没有配置参数");
        }
        isr.close();
        return arrayList;
    }
}
