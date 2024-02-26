
# Dlsql概述
Dlsql是一款用于对象化 SQL 自动生成的工具类，旨在提供方便易用、代码简洁、符合 SQL 语义的解决方案。它支持单表和简单多表查询，旨在为非复杂 SQL 应用场景提供快速研发的解决方案。

# Dlsql印象
## 单表查询（员工信息表）
```sql
CREATE TABLE employees (
    employee_id INT PRIMARY KEY COMMENT '员工ID',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_id INT COMMENT '部门ID',
    salary DECIMAL(10, 2) COMMENT '薪资'
);
```
### 全表查询(简单查询)
> sql原生写法
```java
Sql.db().fromTable("employees");
```
> jfinal深度结合
```java
Employees.db().findModel();
```

### 查询某部门下部分员工薪资大于5000的员工信息，以薪资降序排序，在结果基础上只要员工编号大于10的员工。（复杂查询）
> sql原生写法
```java
Sql.db().fromTable("employees")
    .in("employee_name","张三","李四")
    .eq("department_id ",1001)
    .gt("salary",5000)
    .having().gt("employee_id",10)
    .orderByDesc("salary")
```
> jfinal深度结合
```java
Employees.db().in(Employees.T.femployee_name,)
    .in(Employees.T.fdepartment_id,"张三","李四")
    .eq(Employees.T.fdepartment_id,1001)
    .gt(Employees.T.fsalary,5000)
    .having().gt(Employees.T.femployee_id,10)
    .orderByDesc(Employees.T.fsalary);
    .findModel();
```
## 多表查询
```sql
CREATE TABLE employees (
    employee_id INT PRIMARY KEY COMMENT '员工ID',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_id INT COMMENT '部门ID',
    salary DECIMAL(10, 2) COMMENT '薪资'
);

CREATE TABLE departments (
    department_id INT PRIMARY KEY COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称'
);
```
### 查询员工及其所在部门信息(简单查询)
> sql原生写法
```java
Sql.db().fromTable("employees").select("employee_id","employee_name","department_name").leftJoinTable("departments").link("department_id")
```
> jfinal深度结合
```java
Employees.db()
    .select(Employees.T.femployee_id,Employees.T.femployee_name,Departments.T.fdepartment_name)
    .leftJoinTable(Departments.T.name).link(Employees.T.femployee_id)
    .findModel();
```
### 查询部门领导及其部门名称（复杂查询）
> sql原生写法
```java
Sql.db(0,new AsList().set(0,"e").set(1,"d")).fromTable("employees")
                .select("employee_id","employee_name","department_name")
                .leftJoinTable("departments").link("department_id")
                .inSql("employee_id",
                    Sql.db(0,new AsList().set(0,"ee").set(1,"e"))//子查询自定义别名
                            .fromTable("employees")
                            .select("department_id")
                            .f2tPrefix()
                            .eq("department_id","employee_id")//第一个字段是当前表字段、第二个字段是主表字段
                            .sqlAll()
                )
```
> jfinal深度结合
```java
Employees.db().as(0,new AsList().set(0,"e").set(1,"d"))
                .select(Employees.T.femployee_id,Employees.T.femployee_name,Department.T.fdepartment_name)
                .leftJoinTable(Department.T.name).link(Department.T.fdepartment_id)
                .inSql(Employees.T.femployee_id,
                    Department.db(0,new AsList().set(0,"ee").set(1,"e"))//子查询自定义别名
                            .select(Department.T.fdepartment_id)
                            .f2tPrefix()
                            .eq(Employees.T.femployee_id,Department.T.fdepartment_id)//第一个字段是当前表字段、第二个字段是主表字段
                            .sqlAll()
                )
```
# Dlsql能做什么
## 单表查询
支持自定义返回字段  
支持条件过滤（like模糊、eq等于、ne不等于、gt大于、lt小于、le小于等于、ge大于等于、in、isNull、isNotNull、between、betweenForNot等等）  
支持orderby子句  
支持groupby子句  
支持having子句（支持having条件过滤）  
## 非查询支持  
支持删除语句生成  
支持更新语句生成  
支持保存语句生成  
## 简单多表查询
支持左关联多表关联查询  
支持左关联多表条件查询  
支持返回字段子句查询  
支持条件过滤子句查询  
## 扩展结合其他框架使用
通过扩展，可支持与其它框架深度结合，例如jfinal、spring boot（ibatis）等。  
## 与jfinal深度结合
### 例子一（查询没有删除的并且是某指定用户的云记与标签关系数据）
```java
List<LableNote> list=LableNote.db()
    .ne(LableNote.T.is_del,2)
    .eq(LableNote.T.user_id,getId())
    .findByModel();
```
### 例子二(根据不同查询条件，以分页形式查询标签树数据)
```java
Page<LableTree> page=LableTree.db()
        .select(
                LableTree.T.id,
                LableTree.T.u_id,
        )
        .like(LableTree.T.u_id_names,unames)
        .ge(LableTree.T.level,us.length)
        .paginate(getPageNumber(),getPageSize(20));
```
### 与spring boot（ibatis深度结合）
待补充...
### 与其它框架深度结合
期待您的补充增加
# Dlsql设计思路
待补充...
# Maven方式获取
```xml
<dependency>
    <groupId>io.github.beijiyi</groupId>
    <artifactId>dlsql</artifactId>
    <version>1.0.16</version>
</dependency>
```
# Dlsql文档
1. public static Sql create();    
创建一个新的Sql对象实例。  
1. public static Sql build();    
同create()，创建一个新的Sql对象实例。
1. public static Sql db();    
创建一个新的Sql对象实例，不设置主表索引和别名管理对象。
1. public Sql as(Integer mainTableIndex, AsList asList);    
设置当前操作的主表索引和别名管理对象。
1. public Sql as(AsList asList);     
设置别名管理对象。
1. public Sql as(Integer mainTableIndex);    
设置当前操作的主表索引。
1. public T asR(int i);    
使用随机别名，避免别名重复。
1. public String getMainTableAsName();    
获取主表的别名。
1. public String getWhereSql();    
获取WHERE部分的SQL语句。
1. public String getOrderAndGroupSql();    
获取ORDER BY或GROUP BY部分的SQL语句。
1. public String getGroupSql();    
获取GROUP BY部分的SQL语句。
1. public String sqlFromByCount();    
获取用于数量统计的SQL语句（不包括ORDER BY部分）。
1. public String sqlByCount();    
获取用于统计数量的SQL语句（COUNT(*)）。
1. public String sql();    
构建并获取完整的SQL语句。
1. public String sqlAll();    
构建并获取完整的SQL语句，替换参数占位符。
1. public List<SqlParam> paramList();    
获取参数列表。
1. public Object[] paramArrs();    
获取参数数组。
1. public T select(String... column);    
设置查询的列。
1. public T selectPlain(String... column);    
设置查询的列（字段原样输出）。  
1. public T selectSql(String asName, String columnString);    
追加子查询语句。
1. public T fromTable(String tables);    
设置查询的表名。
1. public T fromTableView(String tables);    
设置查询的视图。
1. public T leftJoinTable(String tableName);    
添加左连接表。
1. public T link(String field1, String field2);    
设置表之间的连接条件。
1. public T link(String field);        
设置表之间的连接条件（字段相同）。
1. public T t(int i);    
设置当前操作表的下标。
1. public T tno();  
不使用前缀。
1. public T t0();   到 public T t24();  
设置当前操作表的下标（0到24）。
1. public T setWY(String sqlType);  
设置当前构建的SQL条件类型。
1. public T where();  
开始构建WHERE条件。
1. public T having();  
开始构建HAVING条件。
1. public T leftwhere();  
开始构建左右内关联条件。
1. public T f1t(int type);  
设置第一个字段的格式化模式。
1. public T f1tDefault();  
设置第一个字段为默认格式化。
1. public T f1tOriginal();  
设置第一个字段原样输出。
1. public T f2t(int type);  
设置第二个字段的格式化模式。
1. public T f2tDefault();  
设置第二个字段为默认格式化。
1. public T f2tOriginal();  
设置第二个字段原样输出。
1. public T f2tPrefix();  
设置第二个字段带前缀。
1. public T and();  
添加AND连接符。
1. public T or();  
添加OR连接符。
1. public T leftMark();  
添加左括号。
1. public T rightMark();  
添加右括号。
1. private T autoWhere();  
自动添加WHERE关键字。
1. public String paramFormat(SqlParam param);  
格式化SQL参数。
1. public String unitFormat(Object value);  
格式化字段值。
1. public String unitFormatPropertyName(String propertyName);  
格式化字段名称。
1. public String eqByType(String propertyName, Object value, String type);  
构建等值条件SQL。
1. public T addQueryItem(String sqlItem);  
添加查询条件SQL片段。
1. public T insertValue(Map<String, Object> saveMap);  
添加插入语句的值部分。
1. public String sqlInsert();  
获取插入语句。
1. public String sqlInsertAll();  
获取完整的插入语句。
1. public T updateSet(Map<String, Object> updateMap);  
添加更新语句的设置部分。
1. public String sqlUpdate();  
获取更新语句。
1. public String sqlUpdateAll();  
获取完整的更新语句。
1. public String sqlDelete();  
获取删除语句。
1. public String sqlDeleteAll();  
获取完整的删除语句。
1. public T orderBy(String... column);  
添加升序排序的ORDER BY语句。
1. public T orderByDesc(String... column);  
添加降序排序的ORDER BY语句。
1. public T orderByAsc(String... column);  
添加升序排序的ORDER BY语句。
1. public T orderBy(String orderbyHql);  
添加ORDER BY语句。
1. public T orderBy(Boolean isDesc, String... column);  
添加排序语句，根据参数决定升序或降序。
1. public T groupBy(String... column);  
添加GROUP BY语句。
1. public T eq(String propertyName, Object value);  
添加等值条件。
1. public T eq(String propertyName, Object... value);  
添加多个等值条件。
1. public T eqForMap(Map<String, Object> map);  
添加多个等值条件，通过Map传递。
1. public T gt(String propertyName, Object value);  
添加大于条件。
1. public T ne(String propertyName, Object value);  
添加不等于条件。
1. public T ge(String propertyName, Object value);  
添加大于等于条件。
1. public T lt(String propertyName, Object value);  
添加小于条件。
1. public T le(String propertyName, Object value);  
添加小于等于条件。
1. private T likeByType(String propertyName, Object value, int... type);  
构建模糊查询条件。
1. public T like(String propertyName, Object value);  
添加模糊查询条件（全模糊）。
1. public T likeForStart(String propertyName, Object value);  
添加左模糊查询条件。
1. public T likeForEnd(String propertyName, Object value);  
添加右模糊查询条件。
1. private T likeForMapByType(Map<String, Object> map, boolean isAndLink, int... type);  
添加多个模糊查询条件，通过Map传递。
1. public T likeForMapAnd(Map<String, Object> map);    
添加多个模糊查询条件（AND连接）。
1. public T likeForStartForMapAnd(Map<String, Object> map);    
添加左模糊查询条件（AND连接）。
1. public T likeForEndForMapAnd(Map<String, Object> map);    
添加右模糊查询条件（AND连接）。
1. public T likeForMap(Map<String, Object> map);    
添加多个模糊查询条件（OR连接）。
1. public T likeForStartForMap(Map<String, Object> map);    
添加左模糊查询条件（OR连接）。
1. public T likeForEndForMap(Map<String, Object> map);    
添加右模糊查询条件（OR连接）。
1. public T in(String propertyName, Object... value);    
添加IN查询条件。
1. public T in(String propertyName, String value);    
添加IN查询条件（字符串形式）。
1. public T in(String propertyName, Collection value);    
添加IN查询条件（集合形式）。
1. public T inSql(String propertyName, String sql);    
添加IN查询条件（SQL子句形式）。
1. public T isNull(String propertyName);    
添加检查字段是否为NULL的条件。
1. public T isNotNull(String propertyName);    
添加检查字段不为NULL的条件。
1. public String isNotEmpty(String propertyName);    
获取检查字段非空（不为NULL且内容不为空）的SQL语句。
1. public String isEmpty(String propertyName);    
获取检查字段为空（为NULL或内容为空）的SQL语句。
1. public T between(String propertyName, Object lo, Object hi);    
添加BETWEEN查询条件，用于指定字段值在两个值之间。
1. public T betweenForNot(String propertyName, Object lo, Object hi);    
添加NOT BETWEEN查询条件，用于指定字段值不在两个值之间。
1. public T writeSqlByWhere(String sql);    
直接在WHERE子句中写入SQL语句。
private T writeSql(String sql);    
私有方法，用于添加SQL语句到查询条件中。
1. public static String getDbType();    
获取当前数据库类型。
1. public T setDbType(String dbType);    
设置数据库类型。
@Override 1. public String toString();    
重写toString方法，返回SQL语句。
1. public StringBuffer getOrderbySql();    
获取ORDER BY子句的StringBuffer。
1. public StringBuffer getSelectColumnBuffer();    
获取选择列的StringBuffer。
1. public StringBuffer getFromTableBuffer();    
- 获取FROM子句的StringBuffer。
1. public static boolean isEmpty(Object pObj);    
- 检查对象是否为空，包括null、空字符串、空集合、空Map或空数组。
1. public static boolean isNotEmpty(Object pObj);    
- 检查对象是否非空，与isEmpty方法相反。