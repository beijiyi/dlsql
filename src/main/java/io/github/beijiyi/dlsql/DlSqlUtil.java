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
public class DlSqlUtil<T extends DlSqlUtil>{
	private static String DB_TYPE= DLDbDialectType.MYSQL;
	private StringBuffer whereSql=new StringBuffer();//where部分的sql
	private StringBuffer orderbySql=new StringBuffer();//orderby部分的sql
	private StringBuffer selectColumnBuffer=new StringBuffer(" * ");//(1) 列的定义
	private StringBuffer fromTableBuffer=new StringBuffer();//(2) 表的定义
	private List<Object> paramList=new ArrayList<>();//参数

	private StringBuffer insertSql=new StringBuffer();//插入sql   例子：(key1,key2,key3) values(value1,value2,value3) 或 (key1,key2,key3) values(?,?,?)
	private StringBuffer updateSql=new StringBuffer();//插入sql   例子：(key1,key2,key3) values(value1,value2,value3) 或 (key1,key2,key3) values(?,?,?)

	private StringBuffer leftSql=new StringBuffer();//left join
	private StringBuffer leftLinkWhereSql=new StringBuffer();//left join link 语句

	private int leftTName=0;//当前第几个连接表
	String[] leftNames=new String[]{"t1","t2","t3","t4","t5","t6","t7","t8","t9","t10"};


	//当前操作的别名
	private  static  String thisT="";

	public DlSqlUtil(){
	}


	/**
	 * 获取一个新的sql。
	 * 每次调用此方法都会先清空一次sql语句。
	 * @return
	 */
	public static DlSqlUtil create(){
		return new DlSqlUtil();
	}

	/**
	 * 获取where 部分语句 （不包括 order by部分）
	 * @return
	 */
	public  String getWhereSql(){
		String temsql="";
		if(Uitl.isNotEmpty(whereSql.toString().trim())){
			where();
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
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumnBuffer+fromTableBuffer+sql
	 * @return
	 */
	public String sql(){
//		select 【(1)*】   from  【(2)表名1,表名2】  左连接或右连接语句  where 【(3)消除关联条件】    and  【(4)其他条件】      【(5)order  by 或  (6)group By】
		String rSql="";
		//没有填写表名，只返回where部分语句
		if(Uitl.isEmpty(fromTableBuffer.toString().trim())){
			rSql=getWhereSql()+getOrderAndGroupSql();
		}else{
			if(Uitl.isNotEmpty(selectColumnBuffer.toString().trim())){
				rSql+=" select "+ selectColumnBuffer.toString()+" ";//select  列
			}
			rSql+=" from "+ fromTableBuffer.toString()+" ";//from   表名
			//left join
			rSql+=leftSql.toString();
			rSql+=leftLinkWhereSql.toString();
			rSql+=getWhereSql();//where
			rSql+=getOrderAndGroupSql();//order by 或 group by等
		}

		return rSql;
	}

	/**
	 * 构建并获取sql语句【不带?号，'转换成"】【不推荐的方式】
	 * 目前是：selectColumnBuffer+fromTableBuffer+sql
	 * @return
	 */
	public String sqlAll(){
		String rSql=sql();
		//替换?
		for (Object object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}



	/**
	 * 返回参数list
	 * @return
	 */
	public List<Object> paramList(){
		return  paramList;
	}


	/**
	 * 返回参数list
	 * @return
	 */
	public Object[] paramArrs(){
		return  paramList.toArray();
	}


	/**
	 * 设置count
	 * @return
	 */
	public T selectColumnCount(){
		this.selectColumn("COUNT(*)");
		return (T)this;
	}


	/**
	 * 设置返回的列
	 * @return
	 */
	public T selectColumn(String columnString){
		selectColumnBuffer=new StringBuffer(columnString);
		return (T)this;
	}

	/**
	 * 设置返回的列
	 * @return
	 */
	public T selectColumn(String... columnString){
		String temSql="";
		for (String string : columnString) {
			if(temSql.equals("")){
				temSql=string;
			}else{
				temSql+=","+string;
			}
		}

		selectColumnBuffer=new StringBuffer(temSql);
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
		fromTableBuffer=new StringBuffer(tables+ " t1 ");
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
			thisT="";
			return (T)this;
		}

		if(leftTName==0){
			leftTName=1;
		}

		leftSql.append(" left join "+tableName  +"  " +leftNames[leftTName]);
		return (T)this;
	}

	public T innerJoinTable(String tableName) {
		if (Uitl.isEmpty(tableName)) {
			this.thisT = "";
			return (T)this;
		} else {
			if (this.leftTName == 0) {
				this.leftTName = 1;
			}

			this.leftSql.append(" inner join " + tableName + "  " + this.leftNames[this.leftTName]);
			return (T)this;
		}
	}

	/**
	 * on 部分 左连接、右连接或全连接
	 * 不需要在link方法中处理
	 * @return
	 */
	/*public T on(){
		leftSql.append(" on ");
		return (T)this;
	}*/

	/**
	 * 两张表之间的连接
	 */
	public  T link(String field1,String field2){
		leftSql.append(" on "+leftNames[0]+"."+field1+"="+leftNames[leftTName]+"."+field2+" ");
		leftTName++;
		return (T)this;
	}

    /**
     * 两张表之间的连接
     */
    public  T link(String field){
        link(field,field);
        return (T)this;
    }


	public T t1(){
		thisT=leftNames[0];
		return (T)this;
	}
	public T t2(){
		thisT=leftNames[1];
		return (T)this;
	}
	public T t3(){
		thisT=leftNames[2];
		return (T)this;
	}
	public T t4(){
		thisT=leftNames[3];
		return (T)this;
	}
	public T t5(){
		thisT=leftNames[4];
		return (T)this;
	}
	public T t6(){
		thisT=leftNames[5];
		return (T)this;
	}
	public T t7(){
		thisT=leftNames[6];
		return (T)this;
	}
	public T t8(){
		thisT=leftNames[7];
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
	 * 根据sql语句，判断是否需要增加groupBy语句
	 *
	 * @return
	 */
	public  T groupBy(String groupByHql){
		if(Uitl.isEmpty(groupByHql)){
			return (T)this;
		}
		if(!groupByHql.contains("group by")){
			this.orderbySql.append(" group by "+groupByHql);
		}else{
			if(groupByHql.trim().indexOf("group by")==0){
				this.orderbySql.append(groupByHql);
			}else{//在前面增加order by
				this.orderbySql.append(" group by "+groupByHql);

			}
		}
		return (T)this;
	}

	/**
	 * 自动判断where语句，如果没有and或or结束，则添加and作为连接
	 * @return
	 */
	public T andAuto(){
		String sql=this.whereSql.toString().trim().toLowerCase();
		if(Uitl.isEmpty(sql)){//还没有条件，不需要添加任何连接词【没有意义】
			return (T)this;
		}

		//根据现有条件字符串判断是否需要添加and条件
		if(sql.endsWith("or")||sql.endsWith("and")||sql.endsWith("(")||sql.endsWith("where")){//是以and或or结束的。【可能是调用了and()或or()方法】
			//保持原来的语义
			return (T)this;
		}

		this.whereSql.append(" and ");//通过检查，添加and连接词
		return (T)this;
	}

	public  T and(){
		String sql=this.whereSql.toString().trim();
		if(Uitl.isNotEmpty(sql)&&!sql.endsWith("and")){//当前sql 不为空，并且不是以and 结束
			if(sql.endsWith("or")){//以or结束，置换成以and结束
				sql=sql.substring(0, sql.length()-2);
			}
			this.whereSql.append(" and ");
		}
		return (T)this;
	}

	/**
	 * 自动判断where语句，如果没有and或or结束，则添加or作为连接
	 * @return
	 */
	public T orAuto(){
		String sql=this.whereSql.toString().trim().toLowerCase();
		if(Uitl.isEmpty(sql)){//还没有条件，不需要添加任何连接词【没有意义】
			return (T)this;
		}

		if(sql.endsWith("or")||sql.endsWith("and")){//是以and或or结束的。【可能是调用了and()或or()方法】
			//保持原来的语义
			return (T)this;
		}

		this.whereSql.append(" or ");//通过检查，添加and连接词
		return (T)this;
	}

	public  T or(){
		String sql=this.whereSql.toString().trim();
		if(Uitl.isNotEmpty(sql)&&!sql.endsWith("or")){//当前sql 不为空，并且不是以or 或and 结束
			if(sql.endsWith("and")){
				sql=sql.substring(0, sql.length()-3);
			}
			this.whereSql.append(" or ");
		}
		return (T)this;
	}

	public  T leftMark(){
		String sql=this.whereSql.toString().trim();
		this.andAuto();
		//添加"("
		this.whereSql.append(" ( ");

		return (T)this;
	}

	public  T rightMark(){
		String sql=this.whereSql.toString().trim();
		//添加")"
		this.whereSql.append(" ) ");
		return (T)this;
	}


	public String andInTrem(String sql1,String sql2){
		sql1=sql1.trim();
		sql2=sql2.trim();
		if(Uitl.isEmpty(sql1)|| Uitl.isEmpty(sql2)){
			return (Uitl.isEmpty(sql1)?(Uitl.isEmpty(sql2)?"":sql2):sql1);
		}else{
			return " "+sql1+" and "+sql2;
		}
	}


	public String orInTrem(String sql1,String sql2){
		sql1=sql1.trim();
		sql2=sql2.trim();
		if(Uitl.isEmpty(sql1)|| Uitl.isEmpty(sql2)){
			return (Uitl.isEmpty(sql1)?(Uitl.isEmpty(sql2)?"":sql2):sql1);
		}else{
			return sql1+" or "+sql2;
		}
	}

	/**
	 * 当前sql，是否需要添加where
	 * @return
	 */
	public  T where(){
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
	 * @param value
	 * @return
	 */
	public String paramFormat(Object value){
		if(Uitl.isEmpty(value)){
			return "";
		}
		if(value instanceof String){//处理字符串类型   防注入处理
			String tempString=(String)value;
			tempString = tempString.replaceAll("'", "''");//防止单引号注入sql
			return "'"+tempString+"'";
		}else if(value instanceof Date||value instanceof Timestamp||value instanceof Calendar){//处理日期类型
			Date date=null;
			if(value instanceof Date){
				date=(Date)value;
			}else if (value instanceof Timestamp) {
				Timestamp timestamp=(Timestamp)value;
				date=new Date(timestamp.getTime());
			}else if (value instanceof Calendar) {
				Calendar calendar=(Calendar)value;
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
		return  value.toString();
	}
	
	
	/**
	 * 根据不同类型，返回相应的单元格式化对象  <br>
	 * 字符串		"2344" 返回  '2344'<br>
	 * 数值型		2344	返回  2344<br>
	 * 日期型		date对象	根据数据库类型返回不同内容  MYSQL  ORACLE<br>
	 * ------------------------------------------------<br>
	 * @param value<br>
	 * @return<br>
	 */
	public String unitFormat(Object value){
		/*if(value instanceof  String ){
			if(value.toString().contains(".")){//已经指定了别名  多表关联下，   select * from A a ,B b where a.id=b.aid
				return unitFormatPropertyName(value.toString());
			}
		}*/

		paramList.add(value);
		return "?";
	}

	/**
	 * 格式化字段名称<br>
	 * 根据上下文决定字段前缀   例如传入  name转化成  t1.name
	 * ------------------------------------------------<br>
	 * @return<br>
	 */
	public String unitFormatPropertyName(String propertyName){
		if(propertyName.contains(".")){//已经指定了别名
			return propertyName;
		}
		String ret=(Uitl.isEmpty(thisT)?leftNames[0]+ ".":thisT+".")+propertyName;
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
	 *  检查当前sql，是否需要去掉and或or
	 * @return
	 */
	public T delEndAndOr(String retTerm){
		String sql=this.whereSql.toString().trim();

		if(sql.equals("")){//sql为空
			this.whereSql.append(retTerm);
			return (T)this;
		}

		if(Uitl.isEmpty(retTerm)){//去掉前面的链接符
			if(sql.endsWith("or")){//以or结尾
				sql=sql.substring(0, sql.length()-2);
			}else if(sql.endsWith("and")){//以and结尾
				sql=sql.substring(0, sql.length()-3);
			}
		}else{
			if(!sql.endsWith("or")&&!sql.endsWith("and")&&!sql.endsWith("(")){//没有任何连接符，默认and链接
				sql=sql+" and "+retTerm+" ";
			}else{
				sql=sql+"  "+retTerm+" ";
			}
		}
		this.whereSql.setLength(0);
		this.whereSql.append(sql);
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
	 * insert into from user +insersql(变量)
	 * @return
	 */
	public String sqlInsertAll(){
		String rSql=sqlInsert();
		//替换?
		for (Object object : paramList) {
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
	 * update user set key1=value,key2=value2,key3=value3 where id=2
	 * @return
	 */
	public String sqlUpdate(){
		String sql="";
		if(Uitl.isEmpty(fromTableBuffer.toString())){
			sql=updateSql.toString();
		}else{
			sql=" update  " +fromTableBuffer.toString()+" set "+updateSql.toString();
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
		for (Object object : paramList) {
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
				rSql+=" delete t1 from "+ fromTableBuffer.toString()+" ";		//from   表名
			}else{
				rSql+=" delete t1  from "+ fromTableBuffer.toString()+" ";		//from   表名
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
		for (Object object : paramList) {
			rSql=rSql.replaceFirst("\\?", paramFormat(object));
		}
		return rSql;
	}



	/**
	 * ＝
	 * @return
	 */
	public  T eq(String propertyName,Object value){
		 delEndAndOr(eqByType(propertyName,value,"="));
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
		 delEndAndOr(eqByType(propertyName,value,">"));
		 return (T)this;
	}
	
	/**
	 * <> 不等于
	 * @return
	 */
	public  T ne(String propertyName,Object value){
		 delEndAndOr(eqByType(propertyName, value, "<>"));
		 return (T)this;
	}
	
	/**
	 * >= 大于等于
	 * @return
	 */
	public  T ge(String propertyName,Object value){
		 delEndAndOr(eqByType(propertyName, value, ">="));
		 return (T)this;
	}
	
	/**
	 *  < 小于
	 * @return
	 */
	public  T lt(String propertyName,Object value){
		 delEndAndOr(eqByType(propertyName, value, "<"));
		 return (T)this;
	}
	
	/**
	 * <= 小于等于
	 * @return
	 */
	public  T le(String propertyName,Object value){
		 delEndAndOr(eqByType(propertyName, value, "<="));
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
		if(type==null||type.length<1){//全模糊搜索
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat("%"+value+"%"));
		}else{
			if(type[0]==0){//0左模糊
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat("%"+value));
			}else if(type[0]==1){//1右模糊
				temp=(unitFormatPropertyName(propertyName)+" like "+unitFormat(value+"%"));
			}
		}
		
		delEndAndOr(temp);
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
	 * like  相当于"like '%value%'"
	 * 利用Map来进行多个like的限制
	 * 拼接后的sql与外部链接默认为and   sql+  and + retTerm
	 * 例子:
	 * 	Map<String, Object>  tempMap=new HashMap<String, Object>();
		tempMap.put("title", dto.get("q_note_title"));
		tempMap.put("content", dto.get("q_note_title"));
		DlSqlUtil.getInstanceForOld().likeForMap(tempMap);
	 * @return
	 */
	private  T likeForMapByType(Map<String, Object> map,boolean isAnd,int... type){
		int count=0;
		if(Uitl.isEmpty(map)){
			return (T)this;
		}

		for (String key : map.keySet()) {
			if(Uitl.isNotEmpty(key)&& Uitl.isNotEmpty(map.get(key))){
				Object value=map.get(key);
				if(count==0){count++;
					leftMark();
				}else{
					if(isAnd){
						and();
					}else{
						or();
					}
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
		andAuto();
		likeForMapByType(map,true);
		return (T)this;
	}

	public  T likeForStartForMapAnd(Map<String, Object> map){
		andAuto();
		likeForMapByType(map,true,0);
		return (T)this;
	}

	public  T likeForEndForMapAnd(Map<String, Object> map){
		andAuto();
		likeForMapByType(map,true,1);
		return (T)this;
	}


	public  T likeForMap(Map<String, Object> map){
		boolean isNotNull=true;
		for (String key:map.keySet()){
			if(Uitl.isNotEmpty(map.get(key))){
				isNotNull=false;
				break;
			}
		}
		if(isNotNull)return (T)this;
		andAuto();
		likeForMapByType(map,false);
		return (T)this;
	}

	public  T likeForStartForMap(Map<String, Object> map){
		andAuto();
		likeForMapByType(map,false,0);
		return (T)this;
	}
	
	public  T likeForEndForMap(Map<String, Object> map){
		andAuto();
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
		
		
		
		delEndAndOr(unitFormatPropertyName(propertyName)+" in ("+retTerm+") ");
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
	 * isNUll	是为null
	 * @return
	 */
	public  T isNull(String propertyName){
		if(Uitl.isEmpty(propertyName)){
			return (T)this;
		}
		delEndAndOr(unitFormatPropertyName(propertyName)+" is null ");
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
		delEndAndOr(unitFormatPropertyName(propertyName) +" is not null");
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
		
		delEndAndOr(unitFormatPropertyName(propertyName) + " between " + unitFormat(lo) + " and " + unitFormat(hi));
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
		delEndAndOr(unitFormatPropertyName(propertyName) + " not between " + unitFormat(lo) + " and " + unitFormat(hi));
		return (T)this;
	}
	
	/**
	 * 直接在where中写入sql
	 */
	public T writeSqlByWhere(String sql){
		if(Uitl.isEmpty(sql)){
			return (T)this;
		}
		delEndAndOr(sql);
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



	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//sql.geti().where().eqMap().and().likeMap().or().orderby()
		Map<String, Object> map =new HashMap<String, Object>();
		String value="查询";
		map.put("key1", value);
		map.put("key2", value);

//		System.out.println("------likeForMap-------");
//		System.out.println(DlSqlUtil.getInstance().likeForMap(map).sqlAll());
//		System.out.println("------eqForAll-------");
//		System.out.println(DlSqlUtil.getInstance().eqForMap(map).sqlAll());
//		
//		System.out.println("------ge-------");
//		System.out.println(DlSqlUtil.getInstance().ge("age",23).sqlAll());
//		System.out.println("------between-------");
//		System.out.println(DlSqlUtil.getInstance().setDbType(DialectType.MYSQL).between("age", UtilDateFormat.StoD("2015-07-09", "yyyy-MM-dd"), UtilDateFormat.StoD("2015-07-10", "yyyy-MM-dd")).sqlAll());
//		
//		System.out.println("------in-------");
//		Integer[] ids={1,2,34,5,6,7};
//		
//		List aList= new ArrayList();
//		aList.add(23);
//		aList.add(444);
//		aList.add(999);
//		
//		Object[] idss={"323",23,"333","444"};
//		System.out.println(DlSqlUtil.getInstance().in("name", idss).orderBy(true,"id","name").sqlAll());
		
		
//		Object[] idsObjects={1,2,34,5,"a",7};
		System.out.println("-----------综合test-----------");
		DlSqlUtil sqlUtil= DlSqlUtil.create();
		sqlUtil
					//.selectColumn("id,name,age")
					//.fromTable("SysUser")
					//.eqForMap(map)
				   .likeForEndForMap(map)
					//.in("age", idsObjects)
					//.eq("kkk", new Date())
					//.writeSqlByWhere("1=1  and 2!=2")
					//.orderBy(true,"id","name","age")
					;
		List param=sqlUtil.paramList();
		Object[] arrs=sqlUtil.paramArrs();
		System.out.println(sqlUtil.sql());
		System.out.println(sqlUtil.sqlAll());
		
//		System.out.println("-----------防注入-----------");
//		map.clear();
//		map.put("name", "系统管理员' and pas='0cd7730fbf6d36059ccc687b43b1ef56");
//		System.out.println(
//				DlSqlUtil.getInstance().selectColumn("id,name,age").fromTable("sys_user")
//				.eqForMap(map)
//				//.likeForMap(map)
//				//.in("age", idsObjects)
//				//.writeSqlByWhere("1=1  and 2!=2")
//				//.orderBy(true,"id","name","age")
//				.sqlAll()
//		);
	}

}
