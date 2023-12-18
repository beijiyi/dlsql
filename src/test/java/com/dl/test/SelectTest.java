package com.dl.test;


import com.dl.sql.DateFormatUtil;
import com.dl.sql.DlSqlUtil;

import java.util.*;

/**
 * Created by dl on 2017/9/3.
 */
public class SelectTest {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        String value = "查询";
        map.put("sr_name", value);
        map.put("sr_remark", value);

        System.out.println("------------------");

/**
 *  select * from  A a
 *  left join B b
 *  on a.id=b.aid
 *  where a.id=1
 *
 *  select * from A a,B b where a.id=b.aid and a.id=1
 *
 *
 *  select * from A a left join B b on a.id=b.aid
 *
 *
 */
        DlSqlUtil select = DlSqlUtil.create();
        select.fromTable("sys_role");

        select.leftJoinTable("sys_role_menu");
            select.link("sr_id","id");

        select.leftJoinTable("table3");
            select.link("id");

        select.t2().eq("save",23);
        select.t1().in("sr_sys_grade", 1,2);
        select.t3().likeForMap(map);

        select.t3().or();
        select.rightMark();
//        select.t1().delEndAndOr();

//        System.out.println(select.sql());
        System.out.println(select.sqlAll());


//当前语句位置   select *     from   left  on    where


//        System.out.println(DateFormatUtil.showFormatDate(new Date().getTime()));
//        Timer timer = new Timer();//实例化Timer类
//        timer.schedule(new TimerTask() {
//            public void run() {
//                System.out.println("退出");
//                this.cancel();
//                System.out.println(DateFormatUtil.showFormatDate(new Date().getTime()));
//            }
//        }, 1000*6
//        );//五百毫秒

    }
}
package com.dl.test;


import com.dl.sql.DateFormatUtil;
import com.dl.sql.DlSqlUtil;

import java.util.*;

/**
 * Created by dl on 2017/9/3.
 */
public class SelectTest {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        String value = "查询";
        map.put("sr_name", value);
        map.put("sr_remark", value);

        System.out.println("------------------");

/**
 *  select * from  A a
 *  left join B b
 *  on a.id=b.aid
 *  where a.id=1
 *
 *  select * from A a,B b where a.id=b.aid and a.id=1
 *
 *
 *  select * from A a left join B b on a.id=b.aid
 *
 *
 */
        DlSqlUtil select = DlSqlUtil.create();
        select.fromTable("sys_role");

        select.leftJoinTable("sys_role_menu");
            select.link("sr_id","id");

        select.leftJoinTable("table3");
            select.link("id");

        select.t2().eq("save",23);
        select.t1().in("sr_sys_grade", 1,2);
        select.t3().likeForMap(map);

//        System.out.println(select.sql());
        System.out.println(select.sqlAll());


//当前语句位置   select *     from   left  on    where


//        System.out.println(DateFormatUtil.showFormatDate(new Date().getTime()));
//        Timer timer = new Timer();//实例化Timer类
//        timer.schedule(new TimerTask() {
//            public void run() {
//                System.out.println("退出");
//                this.cancel();
//                System.out.println(DateFormatUtil.showFormatDate(new Date().getTime()));
//            }
//        }, 1000*6
//        );//五百毫秒

    }
}
