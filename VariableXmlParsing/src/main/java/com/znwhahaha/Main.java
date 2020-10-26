package com.znwhahaha;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String filepath = args[0];
//        String filepath = "/Users/znw_mac/GitRepository/FlexibleCleaningAndFusion/FlexibleCleaningandFusion/VariableXmlParsing/xml.conf";
        //获取配置文件中各个信息
        String hbaseip = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[0];
        String hbaseport = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[1];
        String xmlpath = ReadConfigFile.ReadConfigItem(filepath,"XML文件夹路径=").get(0);
//        String xmlpath = "/Users/znw_mac/fsdownload/files";
        ArrayList<String> hbasetables = ReadConfigFile.ReadConfigItem(filepath,"xml导入hbase表名=");
        
        SetHbase sethbase = new SetHbase();
        XMLAnalysis xmlanalysis = new XMLAnalysis();
        xmlanalysis.conffilepath = filepath;
        SetHbase.conffilepath = filepath;
        //根据配置信息初始化hbase
        sethbase.initHbase(hbaseip,hbaseport);
        String[] array = xmlanalysis.XMLFilePath(xmlpath);
        for (String a:array){
            for (String table:hbasetables){
                sethbase.FileToHbase(table,xmlanalysis.FilexmlAnalysis(xmlpath+"/"+a,table));
            }
        }
        sethbase.close();

    }
}
