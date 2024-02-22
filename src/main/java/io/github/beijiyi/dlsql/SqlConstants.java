package io.github.beijiyi.dlsql;


@SuppressWarnings({"rawtypes", "unchecked"})
/**
 * 常量类
 */
public class SqlConstants{
	public static final String SQL_TYPE_WHERE = "whereSql";
	public static final String SQL_TYPE_LEFT = "leftWhereSql";
	public static final String SQL_TYPE_HAVING = "havingSql";

	/**
	 * like查询的类型
	 * 有三种
	 * 1.全模糊查询
	 * 2.左模糊
	 * 3.右模糊
	 */
	public static final int LIKE_TYPE_DEFAULT=-1;
	public static final int LIKE_TYPE_LEFT=0;
	public static final int LIKE_TYPE_RIGHT=1;


	/**
	 * 两个条件之间使用的连接符
	 */
	public static final String LINK_WAY_TYPE_AND="and";
	public static final String LINK_WAY_TYPE_OR="or";
	/**
	 *第一个条件（通常是表字段），默认为带前缀的字段名称
	 */
	public  static final  int LINK_FIELD1_DEFAULT=1;//默认为带前缀的字段名称
	public  static final  int LINK_FIELD1_ORIGINAL=2;//原样输出，没有前缀
	/**
	 * 第二个条件（通常是条件内容)， 此字段为后生成模式，即调用方法时才动态生成
	 */
	public  static final  int LINK_FIELD2_DEFAULT=1;//默认为格式化内容（例如，name='张三'中的  '张三')
	public  static final  int LINK_FIELD2_PREFIX=3;//带前缀的字段名称
	public  static final  int LINK_FIELD2_ORIGINAL=2;//原样输出，没有前缀


}
