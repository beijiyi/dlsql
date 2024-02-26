package io.github.beijiyi.dlsql;


import java.util.*;

/**
 * Created by dl on 2017/9/3.
 */
public class SelectTest {

    public static  void temSql(){
        AsList asList =new AsList()
                .set(0,"ot")
                .set(1,"slvm")
                .set(2,"som")
                .set(3,"st")
        ;
        Sql sql= Sql.db(0, asList);

        //返回字段
        sql.tno().select("*");
        sql.t2().select("som_zzqk");
        sql.t1().select("lvm_low_voltage_status");


        sql.selectSql("appeal_num",
            Sql.db(0,new AsList().set(0,"sdpsq").set(1,"ot"))
            .selectPlain("count(*)")
            .fromTable("sc_demand_power_supply_quality")
            .f2tPrefix()
            .t1()
            .eq("trans_gis_id","sc_gisid")
            .f2tDefault()
            .t0()
            .between("sc_appeal_time","2023-12-02 00:00:00","2024-01-02 23:59:59")
            .sqlAll()
        );
        sql.fromTable("org_transformer");//主表
        sql.leftwhere();
        sql.t0();
        sql.leftJoinTable("sc_low_voltage_manage").link("lvm_transformer_gisId","trans_gis_id");
        sql.leftJoinTable("sc_overload_manage").link("som_gisId","trans_gis_id");
        sql.leftJoinTable("sc_tqfg").link("tqfg_gisId","trans_gis_id").between("tqfg_tbrq","2023-01-01","2024-01-01");

        sql.where().eq("trans_assert_type","公变");
        sql.in("trans_gis_id","378010542681801","378010323699401","378010542380801","378010542839901","378010544854301","378010434079501","378010323711201","378010545804301","378010542822001","378010541920001","378010323763201","378010544797501","378010542679701","378010545285001","378010542752601");
        sql.having().tno().eq("appeal_num",1);
        sql.tno().orderByDesc("appeal_num");

        System.out.println(sql.sqlAll());



    }


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
        temSql();
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