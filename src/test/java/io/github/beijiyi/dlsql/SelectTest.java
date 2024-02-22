package io.github.beijiyi.dlsql;


import java.util.*;

/**
 * Created by dl on 2017/9/3.
 */
public class SelectTest {

    public static  void temSql(){
        AsTableNames asTableNames=new AsTableNames()
                .set(0,"ot")
                .set(1,"slvm")
                .set(2,"som")
                .set(3,"st")
        ;
        Sql sql= Sql.db(0,asTableNames);

        //返回字段
        sql.t0().select("*");
        sql.t3().select("som_zzqk");
        sql.t2().select("lvm_low_voltage_status");


        sql.selectSql("appeal_num",
            Sql.db(0,new AsTableNames().set(0,"sdpsq").set(1,"ot"))
            .selectOriginal("count(*)")
            .fromTable("sc_demand_power_supply_quality")
            .f2tPrefix()
            .t2()
            .eq("trans_gis_id","sc_gisid")
            .f2tDefault()
            .t1()
            .between("sc_appeal_time","2023-12-02 00:00:00","2024-01-02 23:59:59")
            .sqlAll()
        );
        sql.fromTable("org_transformer");//主表
        sql.leftwhere();
        sql.t1();
        sql.leftJoinTable("sc_low_voltage_manage").link("trans_gis_id","lvm_transformer_gisId");
        sql.leftJoinTable("sc_overload_manage").link("trans_gis_id","som_gisId");
        sql.leftJoinTable("sc_tqfg").link("trans_gis_id","tqfg_gisId").between("tqfg_tbrq","2023-01-01","2024-01-01");

        sql.where().eq("trans_assert_type","公变");
        sql.in("trans_gis_id","378010542681801","378010323699401","378010542380801","378010542839901","378010544854301","378010434079501","378010323711201","378010545804301","378010542822001","378010541920001","378010323763201","378010544797501","378010542679701","378010545285001","378010542752601");
        sql.having().t0().eq("appeal_num",1);
        sql.t0().orderByDesc("appeal_num");

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
        Map<String, Object> map = new HashMap<String, Object>();
        String value = "查询";
        map.put("sr_name", value);
        map.put("sr_remark", value);

        System.out.println("------------------");
        testMultipleTable();
        System.out.println("------------------");
        basicTable(map);
        System.out.println("------------------");
        temSql();
        System.out.println("------------------");
        oneTableSelect();
    }
}