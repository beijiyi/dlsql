
# Dlsql概述
Dlsql是一款用于对象化 SQL 自动生成的工具类，旨在提供方便易用、代码简洁、符合 SQL 语义的解决方案。它支持单表和简单多表查询，旨在为非复杂 SQL 应用场景提供快速研发的解决方案。
# Dlsql能做什么
2.1.单表查询
支持自定义返回字段
支持条件过滤（like模糊、eq等于、ne不等于、gt大于、lt小于、le小于等于、ge大于等于、in、isNull、isNotNull、between、betweenForNot等等）
支持orderby子句
支持groupby子句
支持having子句（支持having条件过滤）
2.2.非查询支持
支持删除语句生成
支持更新语句生成
支持保存语句生成
2.3.简单多表查询
支持左关联多表关联查询
支持左关联多表条件查询
支持返回字段子句查询
支持条件过滤子句查询
2.4.扩展结合其他框架使用
通过扩展，可支持与其它框架深度结合，例如jfinal、spring boot（ibatis）等。
2.4.1.与jfinal深度结合
例子一（查询没有删除的并且是某指定用户的云记与标签关系数据）
List<LableNote> list=LableNote.db()
.ne(LableNote.T.is_del,2)
.eq(LableNote.T.user_id,getId())
.findByModel();
例子二(根据不同查询条件，以分页形式查询标签树数据)
Page<LableTree> page=LableTree.db()
        .select(
                LableTree.T.id,
                LableTree.T.u_id,
        )
        .like(LableTree.T.u_id_names,unames)
        .ge(LableTree.T.level,us.length)
        .paginate(getPageNumber(),getPageSize(20));
2.4.2.与spring boot（ibatis深度结合）
待补充...
# Dlsql设计思路

# Maven方式获取
<dependency>
    <groupId>io.github.beijiyi</groupId>
    <artifactId>dlsql</artifactId>
    <version>1.0.16</version>
</dependency>

# Dlsql使用
