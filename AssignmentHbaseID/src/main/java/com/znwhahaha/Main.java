package com.znwhahaha;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String filepath = args[0];
        ConfigInit.initIndexRule(filepath);
        AboutHbase.wakeupHbase();
        for (String tablename: ConfigInit.HbaseTablename){
            AboutHbase.SetHbaseID(tablename);
        }
        AboutHbase.close();
    }
}
