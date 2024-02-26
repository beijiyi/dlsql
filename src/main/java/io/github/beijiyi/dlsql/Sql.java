package io.github.beijiyi.dlsql;

import java.sql.Timestamp;
import java.util.*;

/**
 * 【第三版本的sql工具类，主要是处理sql注入问题（条件参数化）】
 * -------------------------------------------------------------<br/>
 * 定位是快速构建数据库中基本数据类型的简单查询语句<br/>
 * 【基本数据类型】：与java对应的基本类型，特殊类型如附件类型的，不支持。<br/>
 * 【简单查询语句】：一般系统中存在大量基本查询，如查询多个字段是否匹配某值、模糊查询、in查询、大于等于小于查询、between、等等<br/>
 * -----------------
 * 简单查询语句范例：select 【(1)*】   from  【(2)表名1,表名2】   where 【(3)消除关联条件】    and  【(4)其他条件】      【(5)order  by 或  (6)group By】 <br/>
	 * 本工具就是控制上图中1到6的具体内容；<br/>
	 * 1，默认为 *<br/>
	 * 2，默认为空【需自行传递】<br/>
	 * 3，默认为空<br/>
	 * 4，默认为空<br/>
	 * 5，默认为空<br/>
	 * 6，默认为空<br/>
 * ----------------<br/>
 * 数据库类型：dbType，默认为MySql，用于控制日期型格式问题。<br/>
 * --------使用---------<br/>
 * System.out.println(<br/>
		DlSqlUtil.getInstance()<br/>
		.selectColumn("id,name,age").fromTable("SysUser")   //不编写此段代码则只生成 【where...】<br/>
		.eqForMap(map)<br/>
		.likeForMap(map)<br/>
		.in("age", idsObjects)<br/>
		.orderBy(" id desc")<br/>
		.sql()												//生成sql的唯一方法，必须使用。<br/>
	);<br/>
 * @author dl
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Sql<T extends Sql>{
	protected static String DB_TYPE= DLDbDialectType.MYSQL;//数据库类型

	protected StringBuffer fromTableBuffer=new StringBuffer();//(2) 主表的定义
	protected StringBuffer selectColumnBuffer=new StringBuffer();//本次查询返回列 (1) 列的定义（可能是子查询）



	protected StringBuffer groupSqlSql=new StringBuffer();//group部分的sql
	protected StringBuffer orderbySql=new StringBuffer();//orderby部分的sql

	/**
	 * 对于没有子查询的情况下，一共有三种查询条件片段，分别为：1.whereSql;2.leftLinkWhereSql;3.havingSql
	 *
	 */
	protected StringBuffer whereSql=new StringBuffer();//where部分的sql
//	private StringBuffer leftWhereSql =new StringBuffer();//left join link 语句
	protected List<StringBuffer> leftSqls=new ArrayList<>();//所有关联语句，包含查询条件
	protected StringBuffer havingSql=new StringBuffer();//having子句

	protected List<SqlParam> paramList=new ArrayList<>();//参数

	protected StringBuffer insertSql=new StringBuffer();//插入sql   例子：(key1,key2,key3) values(value1,value2,value3) 或 (key1,key2,key3) values(?,?,?)
	protected StringBuffer updateSql=new StringBuffer();//插入sql   例子：(key1,key2,key3) values(value1,value2,value3) 或 (key1,key2,key3) values(?,?,?)


	protected String WHERE_SQL_TYPE=SqlConstants.SQL_TYPE_WHERE;//当前构建的sql条件类型  三种 1.whereSql;2.leftLinkWhereSql;3.havingSql
	protected int linkTableIndex =0;//当前操作的表位置标记[默认是主表]  -1 时表示不使用别名

	protected String linkWay=SqlConstants.LINK_WAY_TYPE_AND;//条件之间的链接符号  and  或  or  默认为and
	protected int linkField1Type=SqlConstants.LINK_FIELD1_DEFAULT;//默认为带前缀的字段名称
	protected int linkField2Type=SqlConstants.LINK_FIELD2_DEFAULT;//默认为格式化内容（例如，name='张三'中的  '张三')

	protected int linkMainTableIndex=0;//主表标记，在多表查询中，目前的设计是采用子查询方式，主表标记不一定是第一个，需要整体考虑，提前设置。

	protected AsList asList;//表名及别名



	public Sql(){
	}


	/**
	 * 获取一个新的sql。
	 * 每次调用此方法都会先清空一次sql语句。
	 * @return
	 */
	public static Sql create(){
		return db();
	}

	/**
	 * 获取一个新的sql。
	 * 每次调用此方法都会先清空一次sql语句。
	 * @return
	 */
	public static Sql build(){
		return db();
	}

	/**
	 * 获取一个新的sql。
	 * 每次调用此方法都会先清空一次sql语句。
	 * @return
	 */
	public static Sql db(){
		return db(0,new AsList());
	}

//	/**
//	 * 获取一个新的sql。
//	 * 每次调用此方法都会先清空一次sql语句。
//	 * @return
//	 */
//	public static Sql db(String... asTableName){//第一个是主表别名
//		return create();
//	}

	/**
	 * 获取一个新的sql。
	 * 每次调用此方法都会先清空一次sql语句。
	 * @param mainTableIndex		主表下标
	 * @param asList			别名管理对象
	 * @return
	 */
	public  static  Sql db(int mainTableIndex, AsList asList){
		Sql sql=new Sql();
		sql.linkMainTableIndex=mainTableIndex;
		sql.asList = asList;
		return  sql;
	}

	public T as(Integer mainTableIndex, AsList asList){
		if(mainTableIndex!=null)this.linkMainTableIndex=mainTableIndex;
		if(asList!=null)this.asList = asList;
		return (T)this;
	}

	public T as(AsList asList){
		as(null,asList);
		return (T)this;
	}

	public T as(Integer mainTableIndex){
		as(mainTableIndex,null);
		return (T)this;
	}

	/**
	 * 使用随机别名
	 *
	 * 应用场景：在子查询与主查询毫不相关时使用，避免别名重复导致语义不明
	 * @return
	 */
	public T asR(int i){
		List<AsList.TableAlias> aliases= asList.getAll();
		String prefix=AsList.randomGetAsPrefix();//别名前缀
		String as=AsList.randomGetAlias(2);
		for (int i1 = 0; i1 < aliases.size(); i1++) {
			asList.set(i1,prefix+"_"+i+as+i1);
		}
		return (T)this;
	}


	/**
	 * 获得主表别名
	 * @return
	 */
	private String getMainTableAsName(){
		return  this.asList.getAlias(linkMainTableIndex);
	}


	/**
	 * 获取where 部分语句 （不包括 order by部分）
	 * @return
	 */
	public  String getWhereSql(){
		String temsql="";
		if(Uitl.isNotEmpty(whereSql.toString().trim())){
			autoWhere();
			temsql=" "+ whereSql +" ";
		}
		return temsql;
	}



	/**
	 * 获取 order by 或 group by 部分语句
	 * @return
	 */
	public  String getOrderAndGroupSql(){
		String temsql="";
		if(Uitl.isNotEmpty(orderbySql.toString().trim())){
			temsql+=" "+orderbySql.toString()+" ";
		}
		return temsql;
	}

	/**
	 * 获得group by部分的sql
	 * @return
	 */
	public  String getGroupSql(){
		return  this.groupSqlSql.toString();
	}




	/**
	 * 返回数量统计sql（主表from及后面的sql）
	 * 返回样例：
	 * 原始sql:select  t1.*,(select * from table2 where id=2)  from sys_role t1
	 * 返回：from sys_role t1   left join sys_role_menu
	 * @return  sql
	 */
	public  String sqlFromByCount(){
		String rSql="";
//		//没有填写表名，只返回where部分语句
//		if(Uitl.isEmpty(fromTableBuffer.toString().trim())){
//			rSql=getWhereSql()+getOrderAndGroupSql();
//		}else{
//			rSql+=" from "+fromTableBuffer.toString()+" ";//from   表名
//			rSql+= leftWhereSql.toString();
//			rSql+=getWhereSql();//where
//		}

		return rSql;
	}

	/**
	 * 在分页的情况下只需要统计条数时使用。
	 * 返回select  count(*) from  tablename的语句
	 * @return
	 */
	public String sqlByCount(){
		return  " select count(*) "+sqlFromByCount();
	}

	/**
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumn+fromTable+sql
	 * @return
	 */
	public String sql(){
//		select 【(1)*】   from  【(2)表名1,表名2】  左连接或右连接语句  where 【(3)消除关联条件】    and  【(4)其他条件】      【(5)order  by 或  (6)group By】
		 StringBuffer rSql=new StringBuffer();
		//没有填写表名，只返回where部分语句
		if(Uitl.isEmpty(fromTableBuffer.toString().trim())){
			rSql.append(getWhereSql()+getOrderAndGroupSql());
			return rSql.toString();
		}

//1.处理返回字段
		if(Uitl.isEmpty(selectColumnBuffer.toString().trim())){
			rSql.append(" select * ");//默认返回  *  全部
		}else{
			rSql.append(" select "+ selectColumnBuffer.toString()+" ");//select  列
		}
//2.处理表名
		rSql.append(" from "+ fromTableBuffer.toString()+" "+ getMainTableAsName()+" ");//from   表名
//3.处理连接表   左右内连接及查询条件
		StringBuffer finalRSql = rSql;
		leftSqls.forEach(o->{
			finalRSql.append(o.toString());
		});
//4.处理主表条件部分
		rSql.append(getWhereSql());//where
//5.处理group部分
		rSql.append(this.groupSqlSql.toString());//group by
//6.处理having部分
		if(this.havingSql.length()>0)rSql.append(" having "+this.havingSql.toString());//havingSql
//7.处理orderby部分
		rSql.append(this.orderbySql.toString());//order by

		return rSql.toString();
	}

	/**
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumnBuffer+fromTableBuffer+sql
	 * @return
	 */
	public String sqlAll(){
		String rSql=sql();
		//替换?
		for (SqlParam object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}



	/**
	 * 返回参数list
	 * @return
	 */
	public List<SqlParam> paramList(){
		return  paramList;
	}


	/**
	 * 返回参数list
	 * @return
	 */
	public Object[] paramArrs(){
		List<Object> a=new ArrayList<>();
		paramList.forEach(o->{
			a.add(o.value);
		});
		return  a.toArray();
	}

	/**
	 * 保留多个返回字段。
	 * @param column
	 * @return
	 */
	public T select(String... column){
		if(column==null||column.length<=0)return (T)this;//为空或没有传入参数直接返回。

		String prefix=linkTableIndex==-1?"":this.asList.get(linkTableIndex).getAlias()+".";

		for (String s : column) {
			if(this.selectColumnBuffer.length()>0){//已有条件
				this.selectColumnBuffer.append(","+prefix+s);
			}else{
				this.selectColumnBuffer.append(prefix+s);
			}
		}

		return (T)this;
	}

	/**
	 * 保留多个返回字段(字段原样输出)
	 * @return
	 */
	public T selectPlain(String... column){
		int i=linkTableIndex;
		tno();
		select(column);
		t(i);
		return (T)this;
	}

	/**
	 * 追加查询时保留的字段（多个)
	 * 追加的是子查询的情况下，自动增加前后的()及别名
	 * @param asName		字段别名
	 * @param columnString	子查询语句
	 * @return
	 */
	public T selectSql(String asName,String columnString){
		if(Uitl.isEmpty(columnString))return (T)this;//如果是空，则直接返回。
		columnString="("+columnString+") as "+asName+" ";
		selectColumnAppend(columnString);
		return (T)this;
	}

	/**
	 * 追加查询时保留的字段（多个)
	 * @return
	 */
	private T selectColumnAppend(String columnString){
		if(Uitl.isEmpty(columnString))return (T)this;//如果是空，则直接返回。

		if(!columnString.trim().startsWith(","))columnString=","+columnString;//如果没有,号则自动追加。

		this.selectColumnBuffer.append(columnString);
		return (T)this;
	}


	/**
	 * 设置查询的表名【或多个，自己管理，如多张表，请在where中自行添加关联消除的条件】
	 * @return
	 */
	public T fromTable(String tables){
		if(Uitl.isEmpty(tables)||tables.trim().equals("")){
			return (T)this;
		}
		fromTableBuffer=new StringBuffer(tables);
		return (T)this;
	}

	/**
	 * 设置查询的视图【或多个，自己管理，如多张表，请在where中自行添加关联消除的条件】
	 * @return
	 */
	public T fromTableView(String tables){
		if(Uitl.isEmpty(tables)||tables.trim().equals("")){
			return (T)this;
		}
		//设置默认视图  view+表名
		if(!tables.trim().startsWith("view_")){
			tables="view_"+tables;
		}
		fromTableBuffer=new StringBuffer(tables+ " t ");
		return (T)this;
	}

	/**
	 * 左连接
	 * @return
	 */
	public T leftJoinTable(String tableName){
		if(Uitl.isEmpty(tableName)){
			return (T)this;
		}
//
		++linkTableIndex;//当前操作表标记加1
		if(linkTableIndex==linkMainTableIndex)++linkTableIndex;//(需要避开主表，其他还是按顺序递进）
		StringBuffer left=new StringBuffer();
		left.append(" left join "+tableName  +"  " + this.asList.get(linkTableIndex).getAlias());
		leftSqls.add(left);
		return (T)this;
	}

	/**
	 * 内关联（相等于=)
	 * @param tableName
	 * @return
	 */
//	public T innerJoinTable(String tableName) {
//		if (Uitl.isEmpty(tableName)) {
////			this.thisT = "";
//			return (T)this;
//		} else {
//			if (this.linkTableIndex == 0) {
//				this.linkTableIndex = 1;
//			}
//
////			this.leftSql.append(" inner join " + tableName + "  " + this.linkTableAsNames[this.linkTableIndex]);
//			return (T)this;
//		}
//	}

	/**
	 * 左右内关联时两张表之间的连接字段
	 * 当字段在两张表中一样时，位置没有区别。
	 * 当字段在两张表中不同时，第二个字段必须是主表字段
	 */
	public  T link(String field1,String field2){
		StringBuffer leftSql=leftSqls.get(leftSqls.size()-1);//获取当前操作表标记的对象
		leftSql.append(" on "+ this.asList.getAlias(linkTableIndex)+"."+field1+"="+ getMainTableAsName()+"."+field2+" ");
		return (T)this;
	}

    /**
     * 两张表之间的连接
     */
    public  T link(String field){
        link(field,field);
        return (T)this;
    }


	/**
	 * 设置当前操作表的下标
	 * @param i
	 * @return
	 */
	public T t(int i){linkTableIndex=i;return (T)this;}
	/**
	 * 不使用前缀
	 * @return
	 */
	public T tno(){linkTableIndex=-1;return (T)this;}

	/**
	 * 设置当前操作表为第一个1
	 * @return
	 */
	public T t0(){linkTableIndex=0;return (T)this;}
	public T t1(){linkTableIndex=1;return (T)this;}
	public T t2(){linkTableIndex=2;return (T)this;}
	public T t3(){linkTableIndex=3;return (T)this;}
	public T t4(){linkTableIndex=4;return (T)this;}
	public T t5(){linkTableIndex=5;return (T)this;}
	public T t6(){linkTableIndex=6;return (T)this;}
	public T t7(){linkTableIndex=7;return (T)this;}
	public T t8(){linkTableIndex=8;return (T)this;}
	public T t9(){linkTableIndex=9;return (T)this;}
	public T t10(){linkTableIndex=10;return (T)this;}
	public T t11(){linkTableIndex=11;return (T)this;}
	public T t12(){linkTableIndex=12;return (T)this;}
	public T t13(){linkTableIndex=13;return (T)this;}
	public T t14(){linkTableIndex=14;return (T)this;}
	public T t15(){linkTableIndex=15;return (T)this;}
	public T t16(){linkTableIndex=16;return (T)this;}
	public T t17(){linkTableIndex=17;return (T)this;}
	public T t18(){linkTableIndex=18;return (T)this;}
	public T t19(){linkTableIndex=19;return (T)this;}
	public T t20(){linkTableIndex=20;return (T)this;}
	public T t21(){linkTableIndex=21;return (T)this;}
	public T t22(){linkTableIndex=22;return (T)this;}
	public T t23(){linkTableIndex=23;return (T)this;}
	public T t24(){linkTableIndex=24;return (T)this;}

	/**
	 * 设置条件类型
	 * @return
	 */
	public T setWY(String sqlType){
		WHERE_SQL_TYPE=sqlType;
		return (T)this;
	}

	/**
	 * 切换到where条件中。
	 * @return
	 */
	public T where(){
		WHERE_SQL_TYPE=SqlConstants.SQL_TYPE_WHERE;
		linkTableIndex=0;
		return (T)this;
	}

	/**
	 * 切换到having条件中。
	 * @return
	 */
	public T having(){
		WHERE_SQL_TYPE=SqlConstants.SQL_TYPE_HAVING;
		return (T)this;
	}

	/**
	 * 切换到左右内关联条件中。
	 * @return
	 */
	public T leftwhere(){
		WHERE_SQL_TYPE=SqlConstants.SQL_TYPE_LEFT;
		return (T)this;
	}

	/**
	 * 第一个字段格式化模式设置
	 * @param type
	 * @return
	 */
	public T f1t(int type){
		linkField1Type=type;
		return (T)this;
	}
	/**
	 * 第一个字段默认（默认为格式化内容（例如，name='张三'中的  '张三')）
	 * @return
	 */
	public T f1tDefault(){
		linkField1Type=SqlConstants.LINK_FIELD1_DEFAULT;
		return (T)this;
	}
	/**
	 * 条件第一个字段原样输出
	 * @return
	 */
	public T f1tOriginal(){
		linkField1Type=SqlConstants.LINK_FIELD1_ORIGINAL;
		return (T)this;
	}

	/**
	 * 第二个字段格式化模式设置
	 * @param type
	 * @return
	 */
	public T f2t(int type){
		linkField2Type=type;
		return (T)this;
	}

	/**
	 * 第二个字段默认（默认为格式化内容（例如，name='张三'中的  '张三')）
	 * @return
	 */
	public T f2tDefault(){
		linkField2Type=SqlConstants.LINK_FIELD2_DEFAULT;
		return (T)this;
	}

	/**
	 * 条件第二个字段原样输出
	 * @return
	 */
	public T f2tOriginal(){
		linkField2Type=SqlConstants.LINK_FIELD2_ORIGINAL;
		return (T)this;
	}

	/**
	 * 条件第二个字段带前缀
	 * @return
	 */
	public T f2tPrefix(){
		linkField2Type=SqlConstants.LINK_FIELD2_PREFIX;
		return (T)this;
	}

	/**
	 * 获取当前查询条件的类型
	 * @return
	 */
	private StringBuffer getSqlByType(){
		if(SqlConstants.SQL_TYPE_WHERE.equals(WHERE_SQL_TYPE)){//添加在主条件中
			return this.whereSql;
		}else if(SqlConstants.SQL_TYPE_LEFT.equals(WHERE_SQL_TYPE)){//左右内外关联条件中
			if(!leftSqls.isEmpty())
			return leftSqls.get(leftSqls.size()-1);
		}else if(SqlConstants.SQL_TYPE_HAVING.equals(WHERE_SQL_TYPE)){//havingSql条件中
			return this.havingSql;
		}
		return null;
	}

	/**
	 * 添加条件sql后缀的and
	 * @return
	 */
	public  T and(){
		linkWay=SqlConstants.LINK_WAY_TYPE_AND;
		return (T)this;
	}

	/**
	 * 添加or连接符
	 * @return
	 */
	public  T or(){
		linkWay=SqlConstants.LINK_WAY_TYPE_OR;
		return (T)this;
	}

	/**
	 * 添加左括号
	 * @return
	 */
	public  T leftMark(){
		addQueryItem(" ( ");
		return (T)this;
	}

	/**
	 * 添加右括号
	 * @return
	 */
	public  T rightMark(){
		StringBuffer whereSql=getSqlByType();
		if(whereSql==null)return (T)this;

		whereSql.append(" ) ");
		return (T)this;
	}

	/**
	 * 当前sql，是否需要添加where
	 * @return
	 */
	private  T autoWhere(){
		String retTerm=whereSql.toString();
		if(Uitl.isEmpty(retTerm)){
			return (T)this;
		}else{
			whereSql.setLength(0);
			if(!retTerm.trim().contains("where")){
				whereSql.append(" where "+retTerm);
			}else{
				//是否存在子查询   select * from User u where u.name=(select u.name Acc a where a.id=2) and u.id=2
				if(retTerm.trim().indexOf("where")==0){
					 whereSql.append(" "+retTerm);
				}else{//在前面增加where
					 whereSql.append( " where "+retTerm);
				}
			}
		}
		return (T)this;
	}


	/**
	 * 根据不同类型，返回相应的单元格式化对象  <br>
	 * 字符串		"2344" 返回  '2344'<br>
	 * 数值型		2344	返回  2344<br>
	 * 日期类型 		date对象	根据数据库类型返回不同内容    格式统一为"yyyy-MM-dd HH:mm:ss"<br>
	 * @param param
	 * @return
	 */
	public String paramFormat(SqlParam param){
		if(Uitl.isEmpty(param)){
			return "";
		}

		if(param.link_field_type==SqlConstants.LINK_FIELD2_DEFAULT){//默认为格式化内容（例如，name='张三'中的  '张三')
			if(param.value instanceof String){//处理字符串类型   防注入处理
				String tempString=(String)param.value;
				tempString = tempString.replaceAll("'", "''");//防止单引号注入sql
				return "'"+tempString+"'";
			}else if(param.value instanceof Date||param.value instanceof Timestamp||param.value instanceof Calendar){//处理日期类型
				Date date=null;
				if(param.value instanceof Date){
					date=(Date)param.value;
				}else if (param.value instanceof Timestamp) {
					Timestamp timestamp=(Timestamp)param.value;
					date=new Date(timestamp.getTime());
				}else if (param.value instanceof Calendar) {
					Calendar calendar=(Calendar)param.value;
					date=new Date(calendar.getTimeInMillis());
				}
				//根据不同数据库设置
				switch (DB_TYPE) {
					case DLDbDialectType.MYSQL:
						return "'"+ DateFormatUtil.DtoS(date, "yyyy-MM-dd HH:mm:ss")+"'";
					case DLDbDialectType.ORACLE:
						return "to_date('"+ DateFormatUtil.DtoS(date, "yyyy-MM-dd HH:mm:ss")+"','yyyy-MM-dd HH24:mi:ss')";
					default:
						return "";
				}
			}
		}

		return  param.value.toString();
	}
	
	
	/**
	 * (第二个字段）
	 * 根据不同类型，返回相应的单元格式化对象  <br>
	 * 字符串		"2344" 返回  '2344'<br>
	 * 数值型		2344	返回  2344<br>
	 * 日期型		date对象	根据数据库类型返回不同内容  MYSQL  ORACLE<br>
	 * ------------------------------------------------<br>
	 * @param value<br>
	 * @return<br>
	 */
	public String unitFormat(Object value){
		if(linkField2Type==SqlConstants.LINK_FIELD2_PREFIX){//带前缀的字段
			String s=this.asList.getAlias(linkMainTableIndex)+ "."+value;//第二个字段默认为主表字段
			value=s;
		}

		SqlParam param=new SqlParam();
		param.value=value;
		param.link_field_type=linkField2Type;

		paramList.add(param);
		return "?";
	}

	/**
	 * (第一个字段）
	 * 格式化字段名称<br>
	 * 根据上下文决定字段前缀   例如传入  name转化成  t1.name
	 * ------------------------------------------------<br>
	 * @return<br>
	 */
	public String unitFormatPropertyName(String propertyName){
		String ret="";
		if(propertyName.contains(".")){//已经指定了别名
			return propertyName;
		}

		if(linkField1Type==SqlConstants.LINK_FIELD1_DEFAULT){//默认模式   带前缀字段
			if(linkTableIndex==-1){//不需要前缀（字段是动态产生的)
				ret=propertyName;
			}else{//需要前缀
				ret=this.asList.getAlias(linkTableIndex)+ "."+propertyName;//使用的是当前操作表作为前缀
			}
		}else{//原样输出
			ret=propertyName;
		}


		return ret;
	}
	
	
	
	/**
	 * 根据查询类型组合语句
	 * eqByType("name", "233", "=")	返回	name='233'
	 * eqByType("name", "233", ">")	返回	name>'233'
	 * @return
	 */
	public  String eqByType(String propertyName,Object value,String type){
		if(Uitl.isEmpty(propertyName)|| Uitl.isEmpty(value)|| Uitl.isEmpty(type)){
			return "";
		}
		return unitFormatPropertyName(propertyName)+type+unitFormat(value);
	}


	/**
	 * 给现有查询条件增加新的链接项目
	 * @param sqlItem	已生成好的条件sql段
	 * @return
	 */
	public T addQueryItem(String sqlItem){
		StringBuffer sql=getSqlByType();

		if(Uitl.isEmpty(sqlItem))return (T)this;//条件为空直接返回

		//情况一，sql为空
		if(Uitl.isEmpty(sql.toString())){
			sql.append(sqlItem);
		}else{//情况二，sql不为空
			if(sql.toString().trim().endsWith("(")){//前面是小括号的情况，即当前是局部的第一个条件
				sql.append(sqlItem);
			}else{//其他情况，非首个条件。
				sql.append(" "+linkWay+" "+sqlItem);
			}
		}

		return (T)this;
	}

	/**
	 * 	INSERT INTO 语句<br>
	 *  组织插件语句<br>
	 *     例子：(key1,key2,key3) values(value1,value2,value3) 或 (key1,key2,key3) values(?,?,?)
	 * @param saveMap
	 * @return
	 */
	public T insertValue(Map<String,Object> saveMap){
		if(Uitl.isEmpty(saveMap)){
			return  (T)this;
		}

		StringBuffer tem1=new StringBuffer();//(key1,key2,key3)
		StringBuffer tem2=new StringBuffer();//values(?,?,?)


		for (String o : saveMap.keySet()){
			if(Uitl.isEmpty(tem1.toString().trim())){
				tem1.append("("+o);
			}else{
				tem1.append(","+o);
			}

			if(Uitl.isEmpty(tem2.toString().trim())){
				tem2.append("("+unitFormat(saveMap.get(o)));
			}else{
				tem2.append(","+unitFormat(saveMap.get(o)));
			}
		}


		//添加 ）
		tem1.append(") ");
		tem2.append(") ");

		insertSql.append(tem1.toString() + " values"+tem2.toString());
		return  (T)this;
	}

	/**
	 * 构建插入语句，参数是？号形式。
	 * insert into from user +insersql(变量)
	 * @return
	 */
	public String sqlInsert(){
		String sql="";
		if(Uitl.isEmpty(fromTableBuffer.toString())){
			sql=insertSql.toString();
		}else{
			sql=" insert into " +fromTableBuffer.toString()+insertSql.toString();
		}


		return sql;
	}

	/**
	 * 构建插入语句，可直接执行。
	 * insert into from user +insersql(变量)
	 * @return
	 */
	public String sqlInsertAll(){
		String rSql=sqlInsert();
		//替换?
		for (SqlParam object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}


	/**
	 * 	UPDATE 语句<br>
	 *  组织插件语句<br>
	 *     例子：key1=value,key2=value2,key3=value3 或 key1=?,key2=?,key3=?
	 * @param updateMap
	 * @return
	 */
	public T updateSet(Map<String,Object> updateMap){
		if(Uitl.isEmpty(updateMap)){
			return  (T)this;
		}
		StringBuffer tem1=new StringBuffer();

		for (String o : updateMap.keySet()){
			if(Uitl.isEmpty(tem1.toString().trim())){
				tem1.append(o+"="+unitFormat(updateMap.get(o)));
			}else{
				tem1.append(","+o+"="+unitFormat(updateMap.get(o)));
			}

		}

		updateSql.append(tem1.toString());
		return  (T)this;
	}


	/**
	 * 构建更新语句
	 * update user set key1=value,key2=value2,key3=value3 where id=2
	 * @return
	 */
	public String sqlUpdate(){
		String sql="";
		if(Uitl.isEmpty(fromTableBuffer.toString())){
			sql=updateSql.toString();
		}else{
			sql=" update  " + fromTableBuffer.toString()+" "+ getMainTableAsName()+" set "+updateSql.toString();
		}

		sql+=getWhereSql();

		return sql;
	}

	/**
	 * insert into from user +insersql(变量)
	 * @return
	 */
	public String sqlUpdateAll(){
		String rSql=sqlUpdate();
		//替换?
		for (SqlParam object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}



	/**
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumnBuffer+fromTableBuffer+sql
	 * @return
	 */
	public String sqlDelete(){
//		select 【(1)*】   from  【(2)表名1,表名2】  左连接或右连接语句  where 【(3)消除关联条件】    and  【(4)其他条件】      【(5)order  by 或  (6)group By】
		String rSql="";
		//没有填写表名，只返回where部分语句
		if(Uitl.isEmpty(fromTableBuffer.toString().trim())){
			rSql=getWhereSql()+getOrderAndGroupSql();
		}else{
			if (DB_TYPE.equals(DLDbDialectType.MYSQL)){//mysql 下删除语法特殊处理
				rSql+=" delete t1 from "+ fromTableBuffer.toString()+" "+ getMainTableAsName()+" ";		//from   表名
			}else{
				rSql+=" delete t1  from "+ fromTableBuffer.toString()+" "+ getMainTableAsName()+" ";		//from   表名
			}
			rSql+=getWhereSql();								//where
			rSql+=getOrderAndGroupSql();								//order by 或 group by等
		}

		return rSql;
	}

	/**
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumnBuffer+fromTableBuffer+sql
	 * @return
	 */
	public String sqlDeleteAll(){
		String rSql=sqlDelete();
		//替换?
		for (SqlParam object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}

	/**
	 * 传入列名，生成默认升序排序的order  by语句
	 * @return
	 */
	public T orderBy(String... column){
		this.orderBy(false, column);
		return (T)this;
	}

	/**
	 * 传入列名，降序排序的order  by语句
	 * @return
	 */
	public T orderByDesc(String... column){
		this.orderBy(true, column);
		return (T)this;
	}

	/**
	 * 传入列名，升序排序的order  by语句
	 * @return
	 */
	public T orderByAsc(String... column){
		this.orderBy(false, column);
		return (T)this;
	}

	/**
	 * 传入order by-sql语句，判断是否需要增加order by语句
	 *
	 * @return
	 */
	public T orderBy(String orderbyHql){
		if(Uitl.isEmpty(orderbyHql)){
			return (T)this;
		}
		if(!orderbyHql.contains("order by")){
			this.orderbySql.append(" order by "+orderbyHql);
		}else{
			if(orderbyHql.trim().indexOf("order by")==0){
				this.orderbySql.append(orderbyHql);
			}else{//在前面增加order by
				this.orderbySql.append(" order by "+orderbyHql);
			}
		}
		return (T)this;
	}

	/**
	 * 传入列名，生成默认升序排序的order  by语句
	 * @param isDesc	是否降序
	 * @param column	列
	 * @return
	 */
	public T orderBy(Boolean isDesc,String... column){
		if(Uitl.isEmpty(column)){
			return (T)this;
		}

		if(Uitl.isEmpty(isDesc)){
			isDesc=false;
		}


		String tempSql="";
		for (String string : column) {
			if(isDesc){
				string+=" desc ";
			}

			if(tempSql.equals("")){
				tempSql=string;
			}else{
				tempSql+=","+string;
			}
		}

		String temOdSql=this.orderbySql.toString().trim();
		if(temOdSql.startsWith("order by")){//开头是order by
			//两种情况 只有order by 没有其他语句
			if(temOdSql.length()>"order by".length()){
				this.orderbySql.append(","+tempSql);
			}else{
				this.orderbySql.append(tempSql);
			}
		}else{
			this.orderbySql.append(" order by "+tempSql);
		}

		return (T)this;
	}

	/**
	 * 根据sql语句，判断是否需要增加groupBy语句
	 *
	 * @return
	 */
	public  T groupBy(String... column){
		if(Uitl.isEmpty(column)){//为空则直接返回。
			return (T)this;
		}

		String groupSqlSql=this.groupSqlSql.toString().trim();
		this.groupSqlSql.setLength(0);
		this.groupSqlSql.append(groupSqlSql);//确保前后没有空格。

		if(!groupSqlSql.startsWith("group by")){//保证开头是group by
			this.groupSqlSql.append(" group by ");
		}

		for (String s : column) {
			if(this.groupSqlSql.indexOf("group by")!=-1){//还没有任何字段
				this.groupSqlSql.append(linkTableIndex==-1?s:this.asList.getAlias(linkTableIndex)+"."+s);
			}else{//已有字段
				this.groupSqlSql.append(","+(linkTableIndex==-1?s:this.asList.getAlias(linkTableIndex)+"."+s));
			}
		}

		return (T)this;
	}



	/**
	 * ＝
	 * @return
	 */
	public  T eq(String propertyName,Object value){
		addQueryItem(eqByType(propertyName,value,"="));
		 return (T)this;
	}
	
	
	/**
	 * Object[]
	 * 拼接后的sql与外部链接默认为and   sql+  and + retTerm
	 * @return
	 */
	public  T eq(String propertyName,Object... value){
		if(Uitl.isEmpty(value)){
			return (T)this;
		}	
		
		for (Object object : value) {
			eq(propertyName,object);
		}
		
		return (T)this;
	}
	
	
	/**
	 * 利用Map来进行多个等于的限制
	 * 拼接后的sql与外部链接默认为and   sql+  and + retTerm
	 * @return
	 */
	public  T eqForMap(Map<String,Object> map){
		if(Uitl.isEmpty(map)){
			return (T)this;
		}	
		
		for (String key : map.keySet()) {
			eq(key,map.get(key));
		}
		return (T)this;
	}
	
	

	
	/**
	 * > 大于
	 * @return
	 */
	public  T gt(String propertyName,Object value){
		addQueryItem(eqByType(propertyName,value,">"));
		 return (T)this;
	}
	
	/**
	 * <> 不等于
	 * @return
	 */
	public  T ne(String propertyName,Object value){
		addQueryItem(eqByType(propertyName, value, "<>"));
		 return (T)this;
	}
	
	/**
	 * >= 大于等于
	 * @return
	 */
	public  T ge(String propertyName,Object value){
		addQueryItem(eqByType(propertyName, value, ">="));
		 return (T)this;
	}
	
	/**
	 *  < 小于
	 * @return
	 */
	public  T lt(String propertyName,Object value){
		addQueryItem(eqByType(propertyName, value, "<"));
		 return (T)this;
	}
	
	/**
	 * <= 小于等于
	 * @return
	 */
	public  T le(String propertyName,Object value){
		addQueryItem(eqByType(propertyName, value, "<="));
		 return (T)this;
	}
	
	
	/**
	 * like  相当于"like '%value%'"<br>
	 * 这是一个基础类，左模糊，右模糊，全模糊搜索都最终调用此方法
	 * @param propertyName
	 * @param value
	 * @param type			 (不传全模糊  0左模糊 1右模糊)
	 * @return
	 */
	private  T likeByType(String propertyName,Object value,int... type){
		if(Uitl.isEmpty(propertyName)|| Uitl.isEmpty(value)){
			return (T)this;
		}
		String temp="";
		if(type==null||type.length==0){//全模糊搜索
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat("%"+value+"%"));
		}else{
			if(type[0]==SqlConstants.LIKE_TYPE_LEFT){//0左模糊
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat("%"+value));
			}else if(type[0]==SqlConstants.LIKE_TYPE_RIGHT){//1右模糊
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat(value+"%"));
			}
		}

//		delEndAndOr(temp);
		addQueryItem(temp);
		return (T)this;
	}
	
	/**
	 * like  相当于"like '%value%'"<br>
	 * @return
	 */
	public  T like(String propertyName,Object value){
		likeByType(propertyName, value);
		return (T)this;
	}
	
	/**
	 * likeForStart  相当于"like 'value%'"
	 * @return
	 */
	public  T likeForStart(String propertyName,Object value){
		likeByType(propertyName, value,0);
		return (T)this;
	}
	
	/**
	 * likeForEnd 相当于"like '%value'"
	 * @return
	 */
	public  T likeForEnd(String propertyName,Object value){
		likeByType(propertyName, value,1);
		return (T)this;
	}

	/**
	 * 利用Map来进行多个like的限制
	 * 拼接后的sql与外部链接默认为and   sql+  and + retTerm
	 * @param map			待组装的map对象
	 * @param isAndLink		两个条件之间的连接符是否为and，默认为or
	 * @param type			like的类型  全模糊、左模糊、右模糊
	 * @return
	 */
	private  T likeForMapByType(Map<String, Object> map,boolean isAndLink,int... type){
		int count=0;
		if(Uitl.isEmpty(map)){
			return (T)this;
		}

		for (String key : map.keySet()) {
			if(Uitl.isNotEmpty(key)&& Uitl.isNotEmpty(map.get(key))){
				Object value=map.get(key);
				if(count==0){count++;
					leftMark();
				}

				if(isAndLink){
					and();
				}else {
					or();
				}

				likeByType(key, value,type);
			}
		}
		
		if(count>0){
			rightMark();
		}

		return (T)this;
	}

	public  T likeForMapAnd(Map<String, Object> map){
		boolean isNotNull=true;
		for (String key:map.keySet()){
			if(Uitl.isNotEmpty(map.get(key))){
				isNotNull=false;
				break;
			}
		}
		if(isNotNull)return (T)this;
		likeForMapByType(map,true);
		return (T)this;
	}

	public  T likeForStartForMapAnd(Map<String, Object> map){
		likeForMapByType(map,true,0);
		return (T)this;
	}

	public  T likeForEndForMapAnd(Map<String, Object> map){
		likeForMapByType(map,true,1);
		return (T)this;
	}


	/**
	 * 根据map批量设置查询条件
	 * @param map
	 * @return
	 */
	public  T likeForMap(Map<String, Object> map){
		/**
		 * 检查map是否为空
		 */
		boolean isNotNull=true;
		for (String key:map.keySet()){
			if(Uitl.isNotEmpty(map.get(key))){
				isNotNull=false;
				break;
			}
		}
		if(isNotNull)return (T)this;

		likeForMapByType(map,false);
		return (T)this;
	}

	public  T likeForStartForMap(Map<String, Object> map){
		likeForMapByType(map,false,0);
		return (T)this;
	}
	
	public  T likeForEndForMap(Map<String, Object> map){
		likeForMapByType(map,false,1);
		return (T)this;
	}
	
	
	/**
	 * in	数组为参数
	 * @return
	 */
	public  T in(String propertyName,Object... value){
		String retTerm="";
		if(Uitl.isEmpty(value)){
			return (T)this;
		}
		
		//判断是否字符串
		boolean isString=false;
		for (Object object : value) {
			if(object instanceof String){
				isString=true;
				break;
			}
		}
		
		for (Object obj : value) {
			Object temObj=obj;
			if(isString){//是字符串类型，全部转为字符串类型
				temObj=String.valueOf(obj);
			}
			
			if(retTerm.equals("")){
				retTerm=unitFormat(temObj);
			}else{
				retTerm=retTerm+","+unitFormat(temObj);
			}
		}
		
		addQueryItem(unitFormatPropertyName(propertyName)+" in ("+retTerm+") ");
		return (T)this;
	}
	
	/**
	 * in	字符串为参数格式必须为 "1,2,3,4,5,6,4,3"
	 * @return
	 */
	public  T in(String propertyName,String value){
		if(Uitl.isEmpty(value)){
			return (T)this;
		}
		
		if(value.indexOf(",")==-1){//没有
			eq(propertyName, value);
			return (T)this;
		}
		String[] o=value.split(",");
		in(propertyName, o);
		return (T)this;
	}
	
	
	/**
	 * in	Collection为参数
	 * @return
	 */
	public  T in(String propertyName,Collection value){
		in(propertyName, value.toArray());
		return (T)this;
	}

	/**
	 * 根据sql使用in查询
	 * @param propertyName
	 * @param sql
	 * @return
	 */
	public T inSql(String propertyName,String sql){
		addQueryItem(unitFormatPropertyName(propertyName)+" in ("+sql+") ");
		return (T)this;
	}

	/**
	 * isNUll	是为null
	 * @return
	 */
	public  T isNull(String propertyName){
		if(Uitl.isEmpty(propertyName)){
			return (T)this;
		}
		addQueryItem(unitFormatPropertyName(propertyName)+" is null ");
		return (T)this;
	}
	
	/**
	 * isNUll	不为null
	 * @return
	 */
	public  T isNotNull(String propertyName){
		if(Uitl.isEmpty(propertyName)){
			return (T)this;
		}
		addQueryItem(unitFormatPropertyName(propertyName) +" is not null");
		return (T)this;
	}
	
	/**
	 * (未完成)
	 * isNotEmpty	不是空（与null不同，比如字符串""是长度为0却不是null对象,list.siez()==0也不是对象为null）
	 * @return
	 */
	public  String isNotEmpty(String propertyName){
		if(Uitl.isEmpty(propertyName)){
			return "";
		}
		return  unitFormatPropertyName(propertyName) +" is not null";
	}
	
	/**
	 * (未完成)
	 * isEmpty	 是空（与null不同，比如字符串""是长度为0却不是null对象,list.siez()==0也不是对象为null）
	 * @return
	 */
	public  String isEmpty(String propertyName){
		if(Uitl.isEmpty(propertyName)){
			return "";
		}
		return  unitFormatPropertyName(propertyName) +" is null";
	}
	
	
	
	/**
	 * 对应SQL的between子句  在lo和hi之间   包含lo和hi
	 * 只填写lo，则变成>= 大于等于
	 * 只填写hi，则变成<= 小于等于
	 * @return
	 */
	public  T between(String propertyName,Object lo,Object hi){
		if(Uitl.isEmpty(propertyName)||(Uitl.isEmpty(lo)&& Uitl.isEmpty(hi))){
			return (T)this;
		}
		
		if(Uitl.isNotEmpty(hi)&& Uitl.isEmpty(lo)){//只填写hi，则变成<= 小于等于
			return le(unitFormatPropertyName(propertyName),hi);
		}
		
		if(Uitl.isNotEmpty(lo)&& Uitl.isEmpty(hi)){//只填写lo，则变成>= 大于等于
			return ge(unitFormatPropertyName(propertyName),lo);
		}

		addQueryItem(unitFormatPropertyName(propertyName) + " between " + unitFormat(lo) + " and " + unitFormat(hi));
		return (T)this;
	}
	
	/**
	 * 对应SQL的not  between子句  在lo和hi区间之外
	 * @return
	 */
	public  T betweenForNot(String propertyName,Object lo,Object hi){
		if(Uitl.isEmpty(propertyName)|| Uitl.isEmpty(lo)|| Uitl.isEmpty(hi)){
			return (T)this;
		}
		addQueryItem(unitFormatPropertyName(propertyName) + " not between " + unitFormat(lo) + " and " + unitFormat(hi));
		return (T)this;
	}
	
	/**
	 * 直接在where中写入sql
	 */
	public T writeSqlByWhere(String sql){
		writeSql(sql);
		return (T)this;
	}

	/**
	 * 直接在where中写入sql
	 */
	private T writeSql(String sql){
		if(Uitl.isEmpty(sql)){
			return (T)this;
		}
//		delEndAndOr(ssql,sql);
		addQueryItem(sql);
		return (T)this;
	}
	
	
	public static String getDbType() {
		return DB_TYPE;
	}


	public T setDbType(String dbType) {
		this.DB_TYPE = dbType;
		return (T)this;
	}
	
	
	
	@Override
	public String toString() {
		return " "+this.sql()+" ";
	}

	

	public StringBuffer getOrderbySql() {
		return orderbySql;
	}


	public StringBuffer getSelectColumnBuffer() {
		return selectColumnBuffer;
	}


	public StringBuffer getFromTableBuffer() {
		return fromTableBuffer;
	}


	/**
	 * 判断对象是否Empty(null或元素为0)<br>
	 * 1、String  			trim()后  null或""或长度为0或字符串内容为"null"  都判定为空<br>
	 * 2、集合Collection		null或size为0<br>
	 * 3、Map				null或size为0<br>
	 * 4、数组				null或size为0<br>
	 * 5、Objeact			为null<br>
	 *
	 * @param pObj 待检查对象
	 * @return boolean 返回的布尔值
	 */
	public static boolean isEmpty(Object pObj) {
		if (pObj == null){
			return true;
		}

		if (pObj == ""){
			return true;
		}

		if (pObj instanceof String) {//字符串
			String empString=(String) pObj;
			empString=empString.trim();
			if (empString.length() == 0||empString.equals("null")) {
				return true;
			}
		} else if (pObj instanceof Collection) {//集合
			if (((Collection) pObj).size() == 0) {
				return true;
			}
		} else if (pObj instanceof Map) {//Map
			if (((Map) pObj).size() == 0) {
				return true;
			}
		}else if(pObj instanceof Object[]){//数组
			if (((Object[]) pObj).length == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断对象是否NotEmpty(null或元素为0)<br>
	 * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
	 * @param pObj 待检查对象
	 * @return boolean 返回的布尔值
	 */
	public static boolean isNotEmpty(Object pObj) {
		return !isEmpty(pObj);
	}

}
