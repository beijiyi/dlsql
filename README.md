
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
Sql.db(1,new AsList().set(0,"e").set(1,"d")).fromTable("employees")
                .select("employee_id","employee_name","department_name")
                .leftJoinTable("departments").link("department_id")
                .inSql("employee_id",
                    Sql.db(1,new AsList().set(0,"ee").set(1,"e"))//子查询自定义别名
                            .fromTable("employees")
                            .select("department_id")
                            .f2tPrefix()
                            .eq("department_id","employee_id")//第一个字段是当前表字段、第二个字段是主表字段
                            .sqlAll()
                )
```
> jfinal深度结合
```java
Employees.db().as(1,new AsList().set(0,"e").set(1,"d"))
                .select(Employees.T.femployee_id,Employees.T.femployee_name,Department.T.fdepartment_name)
                .leftJoinTable(Department.T.name).link(Department.T.fdepartment_id)
                .inSql(Employees.T.femployee_id,
                    Department.db(1,new AsList().set(0,"ee").set(1,"e"))//子查询自定义别名
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

# Maven方式获取
```xml
<dependency>
    <groupId>io.github.beijiyi</groupId>
    <artifactId>dlsql</artifactId>
    <version>1.0.16</version>
</dependency>
```
# Dlsql使用
