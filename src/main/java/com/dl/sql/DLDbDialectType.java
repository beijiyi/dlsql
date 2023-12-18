package com.dl.sql;
/**
 * 数据库类型
 * @author jmsc
 *RDBMS 方言 
		DB2 					org.hibernate.dialect.DB2Dialect 
		DB2 					AS/400 org.hibernate.dialect.DB2400Dialect 
		DB2 					OS390 org.hibernate.dialect.DB2390Dialect 
		PostgreSQL 				org.hibernate.dialect.PostgreSQLDialect 
		MySQL 					org.hibernate.dialect.MySQLDialect 
		MySQL 					with InnoDB org.hibernate.dialect.MySQLInnoDBDialect 
		MySQL 					with MyISAM org.hibernate.dialect.MySQLMyISAMDialect 
		Oracle (any version) 	org.hibernate.dialect.OracleDialect 
		Oracle 9i/10g 			org.hibernate.dialect.Oracle9Dialect 
		Sybase 					org.hibernate.dialect.SybaseDialect 
		Sybase Anywhere 		org.hibernate.dialect.SybaseAnywhereDialect 
		Microsoft SQL Server	org.hibernate.dialect.SQLServerDialect 
		SAP DB 					org.hibernate.dialect.SAPDBDialect 
		Informix 				org.hibernate.dialect.InformixDialect 
		HypersonicSQL 			org.hibernate.dialect.HSQLDialect 
		Ingres 					org.hibernate.dialect.IngresDialect 
		Progress 				org.hibernate.dialect.ProgressDialect 
		Mckoi SQL 				org.hibernate.dialect.MckoiDialect 
		Interbase 				org.hibernate.dialect.InterbaseDialect 
		Pointbase 				org.hibernate.dialect.PointbaseDialect 
		FrontBase 				org.hibernate.dialect.FrontbaseDialect 
		Firebird 				org.hibernate.dialect.FirebirdDialect 
 */
public class DLDbDialectType {
	public final static String MYSQL="MYSQL";
	public final static String ORACLE="ORACLE";
	public final static String DB2="DB2";
}
