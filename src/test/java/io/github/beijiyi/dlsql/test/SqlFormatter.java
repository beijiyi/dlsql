package io.github.beijiyi.dlsql.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlFormatter {

    public static String formatSql(String sql) {
        // 移除多余的空白字符
        sql = sql.replaceAll("\\s+", " ");

        // 在每个逗号后面添加换行
        sql = sql.replaceAll(",", ",\n");

        // 使用正则表达式缩进SQL语句
        int indentLevel = 0;
        Pattern pattern = Pattern.compile("\\b(SELECT|FROM|WHERE|INNER JOIN|LEFT JOIN|RIGHT JOIN|JOIN|ON)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        StringBuilder formattedSql = new StringBuilder();
        while (matcher.find()) {
            String keyword = matcher.group(1);
            if (keyword.equalsIgnoreCase("FROM") || keyword.equalsIgnoreCase("JOIN") || keyword.equalsIgnoreCase("WHERE")) {
                indentLevel++;
            }
            if (keyword.equalsIgnoreCase("INNER JOIN") || keyword.equalsIgnoreCase("LEFT JOIN") || keyword.equalsIgnoreCase("RIGHT JOIN")) {
                indentLevel--;
            }
            formattedSql.append("\n").append(getIndent(indentLevel)).append(keyword);
        }

        return formattedSql.toString().trim();
    }

    private static String getIndent(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("    "); // 4个空格作为一个缩进
        }
        return indent.toString();
    }

    public static void main(String[] args) {
        String unformattedSql = "SELECT id, name FROM users INNER JOIN orders ON users.id = orders.user_id WHERE age > 25 ORDER BY name;";
        String formattedSql = SqlFormatter.formatSql(unformattedSql);
        System.out.println(formattedSql);
    }
}
