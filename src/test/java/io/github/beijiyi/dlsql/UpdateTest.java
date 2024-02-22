package io.github.beijiyi.dlsql;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dl on 2017/9/3.
 */
public class UpdateTest {
    public static void main(String[] args) {
        Map<String, Object> map =new HashMap<String, Object>();
        String value="查询";
        map.put("val", value);
        map.put("t2", value);


        Sql update = Sql.create();
        update.fromTable("demo_test");
        update.updateSet(map);
        update.eq("id",1);
        System.out.println(update.sqlUpdate());
        System.out.println(update.sqlUpdateAll());
    }
}
