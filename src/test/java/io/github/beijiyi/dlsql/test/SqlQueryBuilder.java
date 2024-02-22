package io.github.beijiyi.dlsql.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class SqlQueryBuilder {

    private StringBuilder query;

    public SqlQueryBuilder() {
        this.query = new StringBuilder();
    }

    public SqlQueryBuilder appendCondition(String condition) {
        if (query.length() > 0) {
            query.append(" AND ");
        }
        query.append(condition);
        return this;
    }

    public SqlQueryBuilder appendOrCondition(String condition) {
        if (query.length() > 0) {
            query.append(" OR ");
        }
        query.append(condition);
        return this;
    }

    public SqlQueryBuilder appendParenthesesConditions(List<String> conditions, String logicalOperator) {
        if (query.length() > 0) {
            query.append(" AND ");
        }

        if (!conditions.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" " + logicalOperator + " ", "(", ")");
            conditions.forEach(joiner::add);
            query.append(joiner);
        }

        return this;
    }

    public String build() {
        return query.toString();
    }

    public static void main(String[] args) {
        SqlQueryBuilder builder = new SqlQueryBuilder();

        // 示例：AND条件
        String condition1 = "column1 = 'value1'";
        String condition2 = "column2 = 'value2'";
        builder.appendCondition(condition1).appendCondition(condition2);

        System.out.println("AND条件: " + builder.build());

        // 为下一个示例重置构建器
        builder = new SqlQueryBuilder();

        // 示例：OR条件
        String orCondition1 = "column3 = 'value3'";
        String orCondition2 = "column4 = 'value4'";
        builder.appendOrCondition(orCondition1).appendOrCondition(orCondition2);

        System.out.println("OR条件: " + builder.build());

        // 为下一个示例重置构建器
        builder = new SqlQueryBuilder();

        // 示例：带括号和OR的条件
        String nestedCondition1 = "column5 = 'value5'";
        String nestedCondition2 = "column6 = 'value6'";
        builder.appendParenthesesConditions(new ArrayList<>(Arrays.asList(nestedCondition1, nestedCondition2)), "OR");

        System.out.println("带括号的条件: " + builder.build());
    }
}
