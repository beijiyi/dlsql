package io.github.beijiyi.dlsql;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dl on 2017/9/3.
 */
public class InsertTest {
    public static void main(String[] args) {
        Map<String, Object> map =new HashMap<String, Object>();
        String value="查询";
        map.put("val", value);
        map.put("t2", value);


        Sql insert= Sql.create();
        insert.fromTable("demo_test");
        insert.insertValue(map);
        System.out.println(insert.sqlInsert());
        System.out.println(insert.sqlInsertAll());
    }
}
