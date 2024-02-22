package io.github.beijiyi.dlsql;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息类
 * 1.管理本次的别名集合
 * 2.别名和序号是必须的，表名是非必须的。
 * 3.只要创建实例就会默认给出t1..tn的别名
 */
public class AsTableNames {
    private List<TableAlias> tableAliases;

    public AsTableNames() {
        this.tableAliases = new ArrayList<>();
        add(new TableAlias("t1"));
        add(new TableAlias("t2"));
        add(new TableAlias("t3"));
        add(new TableAlias("t4"));
        add(new TableAlias("t5"));
        add(new TableAlias("t6"));
        add(new TableAlias("t7"));
        add(new TableAlias("t8"));
        add(new TableAlias("t9"));
        add(new TableAlias("t10"));
        add(new TableAlias("t11"));
        add(new TableAlias("t12"));
        add(new TableAlias("t13"));
        add(new TableAlias("t14"));
        add(new TableAlias("t15"));
        add(new TableAlias("t16"));
        add(new TableAlias("t17"));
        add(new TableAlias("t18"));
        add(new TableAlias("t19"));
        add(new TableAlias("t20"));
        add(new TableAlias("t21"));
        add(new TableAlias("t22"));
        add(new TableAlias("t23"));
        add(new TableAlias("t24"));
        add(new TableAlias("t25"));
    }

    /**
     * 自定义别名
     * @param asName
     */
    public void add(String... asName){

    }

    /**
     * 自定义别名
     * @param i
     * @param asName
     */
    public  AsTableNames  set(int i,String asName){
       if(tableAliases.size()<=i)return this;//确保下标存在

       TableAlias tableAlias= this.tableAliases.get(i);
       tableAlias.alias=asName;
        return this;
    }

    public  void add(TableAlias tableAlias){
        tableAliases.add(tableAlias);
    }

//    // 添加表和别名
//    public void addTableAlias(String tableName, String alias) {
//        TableAlias tableAlias = new TableAlias(tableName, alias);
//        tableAliases.add(tableAlias);
//    }
//
//    // 获取表的别名
//    public String getAlias(String tableName) {
//        for (TableAlias tableAlias : tableAliases) {
//            if (tableAlias.getTableName().equals(tableName)) {
//                return tableAlias.getAlias();
//            }
//        }
//        return null;  // 表不存在时返回null，你可以根据实际需求进行处理
//    }

    /**
     * 获取全部表信息
     * @return
     */
    public List<TableAlias> getAll(){
        return tableAliases;
    }

    public  TableAlias get(int i){
        return tableAliases.get(i);
    }

    /**
     * 根据下标获取别名
     * @param i
     * @return
     */
    public  String getAlias(int i){
        return  tableAliases.get(i).getAlias();
    }


    /**
     * 表和别名的内部类
     */
    public static class TableAlias {
        /**
         * 表名称（非必须）
         */
        private String tableName;
        /**
         * 表别名（必须项）默认为  t1...tn
         */
        private String alias;

        public TableAlias(String tableName, String alias) {
            this.tableName = tableName;
            this.alias = alias;
        }

        public TableAlias(String alias) {
            this.alias = alias;
        }

        public String getTableName() {
            return tableName;
        }

        public String getAlias() {
            return alias;
        }
    }
}
