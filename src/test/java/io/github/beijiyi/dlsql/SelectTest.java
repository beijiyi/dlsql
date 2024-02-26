package io.github.beijiyi.dlsql;


import java.util.*;

/**
 * Created by dl on 2017/9/3.
 */
public class SelectTest {

    /**
     * 单表查询
     */
    public  static  void oneTableSelect(){
        Sql sqlUtil= Sql.db();
        sqlUtil.fromTable("org_transformer").eq("trans_assert_type","公变");
        System.out.println(sqlUtil.sqlAll());
    }

    /**
     * 多表
     */
    public static void testMultipleTable(){
        Sql select = Sql.db();
        select.fromTable("org_transformer");
        select.eq("trans_assert_type","公变");
        select.having();
        select.ge("trans_id",32323);
        select.orderByDesc("trans_id");

        System.out.println(select.sqlAll());
    }


    public static void basicTable(Map<String, Object> map){
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
        Sql select = Sql.create();
        select.select(" t1.*");
//        select.selectSql("dsf",DlSqlUtil.create().fromTable("sys_user").eq("userid",12).sql());
        select.fromTable("sys_role");

        select.leftwhere();

        select.leftJoinTable("sys_role_menu");
            select.link("sr_id","id").eq("id",22222222);

        select.leftJoinTable("table3");
            select.link("id").eq("id",3333333);
//
        select.where();
        select.t2().eq("save",23)
                .or()
                .eq("b",23)
                .and()
                .eq("c",23333);

//        select.t1().in("sr_sys_grade", 1,2);
        select.t1().likeForMap(map);
        select.t1().likeForMapAnd(map);

        select.t0().groupBy("b");

//        select.writeSqlByWhere("");

//        select.t1().delEndAndOr();

//        System.out.println(select.sql());
        System.out.println(select.sqlAll());
//        System.out.println(select.sqlFromByCount());


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

    public static void main(String[] args) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        String value = "查询";
//        map.put("sr_name", value);
//        map.put("sr_remark", value);
//
//        System.out.println("------------------");
//        testMultipleTable();
//        System.out.println("------------------");
//        basicTable(map);
//        System.out.println("------------------");
//        temSql();
//        System.out.println("------------------");
//        oneTableSelect();


//        String sql= Sql.db().fromTable("employees")
//                .in("employee_name","张三","李四")
//                .eq("department_id ",1001)
//                .gt("salary",5000)
//                .having().gt("employee_id",10)
//                .orderByDesc("salary").sqlAll();
//        System.out.println(sql);
//
//
//        String sql2= Sql.db(1,new AsList().set(0,"e").set(1,"d")).fromTable("employees")
//                .select("employee_id","employee_name","department_name")
//                .leftJoinTable("departments").link("department_id")
//                .inSql("employee_id",
//                    Sql.db(1,new AsList().set(0,"ee").set(1,"e"))
//                            .fromTable("employees")
//                            .select("department_id")
//                            .f2tPrefix()
//                            .eq("employee_id","department_id")
//                            .sqlAll()
//                )
//                .sqlAll();
//        System.out.println(sql2);

    }
}