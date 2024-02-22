package io.github.beijiyi.dlsql;

/**
 * 参数对象
 */
public class SqlParam {
    /**
     * 参数本身
     */
    public Object value;
    /**
     * 参数的类型
     * 	public  static final  int LINK_FIELD2_DEFAULT=1;//默认为格式化内容（例如，name='张三'中的  '张三')
     * 	public  static final  int LINK_FIELD2_PREFIX=3;//带前缀的字段名称
     * 	public  static final  int LINK_FIELD2_ORIGINAL=2;//原样输出，没有前缀
     */
    public int link_field_type;
}
