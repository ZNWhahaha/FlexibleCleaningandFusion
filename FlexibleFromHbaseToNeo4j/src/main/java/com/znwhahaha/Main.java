package com.znwhahaha;

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.driver.v1.*;
import static org.neo4j.driver.v1.Values.parameters;
import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main( String[] args ) throws IOException {

        String filepath = args[0];
        ConfigInit.initIndexRule(filepath);
        ArrayList<String> tablerel = ConfigInit.indexRel;
        AboutHbase.wakeupHbase(filepath);
        ArrayList<String> tables = ReadConfigFile.ReadConfigItem(filepath,"转化表名=");
        for (String table : tables){
            AboutHbase.SetNeo4jNode(table);
        }
        for (String rel: tablerel){
            String tableA = rel.split("<=>")[0];
            String index = rel.split("<=>")[1];
            String tableB = rel.split("<=>")[2];
            String module = rel.split("<=>")[3];
            AboutHbase.SetNeo4jRel(tableA,index,tableB,module);

        }
        AboutHbase.close();
    }


}
