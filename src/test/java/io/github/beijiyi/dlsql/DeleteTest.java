package io.github.beijiyi.dlsql;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dl on 2017/9/3.
 */
public class DeleteTest {
    public static void main(String[] args) {
        Map<String, Object> map =new HashMap<String, Object>();
        String value="查询";
        map.put("val", value);
        map.put("val2", value);


        Sql delete = Sql.create();
        delete.fromTable("demo_test");
            delete.eq("id",1);
            delete.or();
            delete.likeForMap(map);
        System.out.println(delete.sqlDelete());
        System.out.println(delete.sqlDeleteAll());
    }
}
