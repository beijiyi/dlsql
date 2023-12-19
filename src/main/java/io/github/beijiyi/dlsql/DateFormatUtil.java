package io.github.beijiyi.dlsql;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author  作者姓名：dl <br>
 * @version 创建时间：Dec 16, 2009  11:14:18 AM<br>
 * 类说明<br>
 * 该工具类为日期处理类<br>
 * 1、转换方法   StoD、DtoS和StoS<br>
 * 		1.1 字符串转日期		StoD<br>
 * 	    1.2 日期转字符串		DtoS<br>
 * 	    1.3 字符串转字符串【符合日期格式的字符串】 StoS<br>
 * 2、日期计算方法  以count开头<br>
 */
public class DateFormatUtil {
	/**
	 * 平台默认日期格式集合<br>
	 * 没有指定格式的日期字符串按照顺序依次尝试转换<br>
	 * 第一个格式是日期类型转换成字符串时的默认类型
	 */
	public final static String[]  DATA_FORMAT_DEL= new String[]{
			"yyyy-MM-dd HH:mm:ss"//默认格式
			,"yyyy-MM-dd HH:mm"
			,"yyyy-MM-dd HH"
			,"yyyy-MM-dd"
			,"yyyy-MM"
			,"yyyy-MM-dd HH:mm:ss.S"

			,"yyyy/MM/dd HH:mm:ss.S"
			,"yyyy/MM/dd HH:mm:ss"
			,"yyyy/MM/dd HH:mm"
			,"yyyy/MM/dd HH"
			,"yyyy/MM/dd"
			,"yyyy/MM"

			,"yyyyMMddHHmmssS"
			,"yyyyMMddHHmmss"
			,"yyyyMMddHHmm"
			,"yyyyMMddHH"
			,"yyyyMMdd"
			,"yyyy"
	};//覆盖平台所有日期类型即可

	public static final SimpleDateFormat sdf=new SimpleDateFormat(DATA_FORMAT_DEL[0]);

	/**
	 *
	 *@方法说明：时间转成字符串
	 *@author dl
	 *@version:2011-5-4下午12:09:51
	 *@方法签名:public String DtoS(){}
	 * @param date
	 * @param dataformat  转换模式  如:yyyy年MM月dd日 HH时mm分ss秒  默认为yyyy年MM月dd日 HH时mm分ss秒
	 * @return
	 */
	public static String DtoS(Date date,String dataformat){
		String retDate="";
		if(date==null){
			return null;
		}
		if(Uitl.isNotEmpty(dataformat)){//有自定义格式
			sdf.applyPattern(dataformat);
		}else{
			sdf.applyPattern(DATA_FORMAT_DEL[0]);//使用默认格式
		}

		retDate=sdf.format(date);
		return retDate;
	}

	public static String DtoS(Date date){
		return DtoS(date,null);
	}
	/**
	 * 以追加转换格式的方式将字符串转换成日期对象<br>
	 * @param date
	 * @param dataformat
	 * @return
	 */
	public static Date StoDAppend(String date,String... dataformat){
		return StoD(date,true,dataformat);
	}

	/**
	 * 将字符串转换成日期对象<br>
	 * @param date			日期字符串
	 * @param dataformat	字符串格式
	 * @return
	 */
	public static Date StoD(String date,String... dataformat){
		return StoD(date,false,dataformat);
	}

	/**
	 * 讲字符串转换成日期对象<br>
	 * 如下面情况都可以返回正确的时间对象
	 * StoD("2013-12-12","yyyy-MM-dd","yyyy-MM","yyyy")
	 * @param isAppand		是否采用追加方式
	 * @param date			日期字符串
	 * @param dataformat	字符串格式
	 * @return
	 */
	public static Date StoD(String date,boolean isAppand,String... dataformat){
		Date retDate=null;

		if(Uitl.isNotEmpty(dataformat)){//有自定义格式
			for (String df : dataformat) {
				sdf.applyPattern(df);
				try {retDate=sdf.parse(date);} catch (Exception e) {}
				if(retDate!=null){
					break;
				}
			}
		}

		if(isAppand|| Uitl.isEmpty(dataformat)){//追加方式  或 没有指定转换格式
			for (String df:DATA_FORMAT_DEL){
				try {
					sdf.applyPattern(df);
					retDate=sdf.parse(date);
					if(Uitl.isNotEmpty(retDate)){//成功转换
						break;
					}
				} catch (Exception e) {}
			}
		}

		return retDate;
	}


	/**
	 * 将一个符合日期格式的字符串转换成另外日期格式的字符串形式<br>
	 * "201701"  转成  "2017-01"
	 * @param date
	 * @param dataformat
	 * @return
	 */
	public static String StoS(String date,String dataformat,String toDataformat){
		if(Uitl.isEmpty(date)){
			return "";
		}
		return DtoS(StoD(date, dataformat), toDataformat);
	}

	/**
	 * 将一个符合日期格式的字符串转换成另外日期格式的字符串形式<br>
	 * "201701"  转成  "2017-01"
	 * @param date
	 * @param toDataformat
	 * @return
	 */
	public static String StoS(String date,String toDataformat){
		return DtoS(StoD(date), toDataformat);
	}


/***************************************************************************************************/



	/**
	 * 当前时间最大值  2015-12-17 23:59:59
	 * @param date
	 * @return
	 */
	public static Date maxDayBySDate(Date date){
		String dates=DtoS(date, "yyyy-MM-dd");
		Date retDate=StoD(dates+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
		return  retDate;
	}

	/**
	 * 当前时间最小值  2015-12-17 00:00:00
	 * @param date
	 * @return
	 */
	public static Date minDayBySDate(Date date){
		String dates=DtoS(date, "yyyy-MM-dd");
		Date retDate=StoD(dates+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
		return  retDate;
	}


	/**
	 * 根据一个时间格式 一个字符串，返回这个字符串代表时间的下一天
	 * @return
	 */
	public static String nextDateInString(String dataformat,String date){
		try {

			if(date==null){
				return null;
			}

			if(dataformat!=null&&!" ".equals(dataformat)){
				sdf.applyPattern(dataformat);
			}

			Calendar   cla=Calendar.getInstance();

			try {
				cla.setTime(sdf.parse(date));
			} catch (Exception e) {
				return null;
			}

			cla.add(Calendar.DAY_OF_YEAR,+1);

			return sdf.format(cla.getTime());

		} catch (Exception e) {
			return date;
		}
	}

	/**
	 * 根据一个时间格式 一个字符串，返回这个字符串代表时间的上一天
	 * @return
	 */
	public static String backDateInString(String dataformat,String date){
		try {


			if(date==null){
				return null;
			}

			if(dataformat!=null&&!" ".equals(dataformat)){
				sdf.applyPattern(dataformat);
			}

			Calendar   cla=Calendar.getInstance();

			try {
				cla.setTime(sdf.parse(date));
			} catch (Exception e) {
				return null;
			}

			cla.add(Calendar.DAY_OF_YEAR,-1);

			return sdf.format(cla.getTime());

		} catch (Exception e) {
			return date;
		}
	}


	/**
	 * 根据一个时间格式 一个字符串,以及一个整数[负数表示过去多少天,正数表示未来多少天]
	 * @return
	 */
	public static Date goDate(Date date,Integer s){
		try {

			if(s==null){
				s=0;
			}

			if(date==null){
				return null;
			}

			Calendar   cla=Calendar.getInstance();

			try {
				cla.setTime(date);
			} catch (Exception e) {
				return null;
			}

			cla.add(Calendar.DAY_OF_YEAR,s);

			return cla.getTime();

		} catch (Exception e) {
			return date;
		}
	}

	/**
	 *
	 *@方法说明：从默认时间开始后的毫秒数
	 *@author dl
	 *@version:2011-5-4下午12:11:21
	 *@方法签名:public Date TimestampToDate(){}
	 * @param tp
	 * @return
	 */
	public static Date TimestampToDate(Timestamp tp){
		if(tp==null){
			return null;
		}
		return (Date)(tp);
	}

	/**
	 * 当前月最后一天
	 * @return
	 */
	public static Calendar getMonthMax(){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		return c;
	}

	/**
	 * 当前月第一天
	 * @return
	 */
	public static Calendar getMonthMin(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
		return c;
	}

	/**
	 * 根据毫秒数格式化show出相差的时间
	 * @param mss 要转换的毫秒数
	 * @return 该毫秒数转换为 【115天17小时46分钟40秒】 后的格式
	 */
	public static String showFormatDate(long mss) {
		StringBuffer sBuffer=new StringBuffer();
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (mss % (1000 * 60)) / 1000;
		if(days>0){
			sBuffer.append(days+"天");
		}
		if(hours>0){
			sBuffer.append(hours+"小时");
		}
		if(minutes>0){
			sBuffer.append(minutes+"分钟");
		}
		if(seconds>0){
			sBuffer.append(seconds+"秒");
		}else{
			sBuffer.append("0秒");
		}
		return sBuffer.toString();
	}

	/**
	 * 传入两个时间格式化show出相差的时间
	 * @return 该毫秒数转换为 【115天17小时46分钟40秒】 后的格式
	 */
	public static String showFormatDate(Date begin, Date end) {
		return showFormatDate(end.getTime()-begin.getTime());
	}


	/**
	 * 返回日期对应的星期几<br>
	 * 1	星期一<br>
	 * 2	星期二<br>
	 * ...
	 * 7	星期天<br>
	 * @param date
	 * @return
	 */
	public static int dayForWeek(Date date){
		int retWeek=-1;
		if(Uitl.isEmpty(date)){
			return  retWeek;
		}

		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		int temi=calendar.get(Calendar.DAY_OF_WEEK);

		if(temi==1){
			retWeek=7;
		}else{
			retWeek=temi-1;
		}

		return  retWeek;
	}


	/**
	 * @param date1 需要比较的时间 不能为空(null),需要正确的日期格式
	 * @param date2 被比较的时间  为空(null)则为当前时间
	 * @param stype 返回值类型   0为多少天，1为多少个月，2为多少年
	 * @return
	 */
	public static int compareDate(String date1,String date2,int stype){
		int n = 0;
		String[] u = {"天","月","年"};
		String formatStyle = stype==1?"yyyy-MM":"yyyy-MM-dd";

		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd");

		date2 = date2==null? simple.format(date):date2;


		DateFormat df = new SimpleDateFormat(formatStyle);
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			c1.setTime(df.parse(date1));
			c2.setTime(df.parse(date2));
		} catch (Exception e3) {
			System.out.println("wrong occured");
		}

		while (!c1.after(c2)) {								// 循环对比，直到相等，n 就是所要的结果
			n++;
			if(stype==1){
				c1.add(Calendar.MONTH, 1);          // 比较月份，月份+1
			}
			else{
				c1.add(Calendar.DATE, 1);           // 比较天数，日期+1
			}
		}

		n = n-1;

		if(stype==2){
			n = (int)n/365;
		}
		return n;
	}


	/**
	 *
	 * 传入一个日期返回第几季度
	 * 1 第一季度
	 * 2 第二季度
	 * 3 第三季度
	 * 4 第四季度
	 * @param date
	 * @return
	 */
	public static int getSeason(Date date) {
		int season = 0;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int month = c.get(Calendar.MONTH);
		switch (month) {
			case Calendar.JANUARY:
			case Calendar.FEBRUARY:
			case Calendar.MARCH:
				season = 1;
				break;
			case Calendar.APRIL:
			case Calendar.MAY:
			case Calendar.JUNE:
				season = 2;
				break;
			case Calendar.JULY:
			case Calendar.AUGUST:
			case Calendar.SEPTEMBER:
				season = 3;
				break;
			case Calendar.OCTOBER:
			case Calendar.NOVEMBER:
			case Calendar.DECEMBER:
				season = 4;
				break;
			default:
				break;
		}
		return season;
	}

	public static void main(String[] agrs) throws ParseException{
//		System.out.println(CryptoUtil.jiemi(CryptoUtil.jiami("你好吗", null), null));
//			System.out.println(TimestampToDate(new Timestamp(23)));
//			System.out.println(DateFormatUtil.DtoS(new java.sql.Date(234234),null));
//			System.out.println(new Timestamp((DateFormatUtil.StoD("2008年12月3日 12时12分2秒", "").getTime())));

		System.out.println(DateFormatUtil.StoS("2017-12","yyyy-dd"));
	}

}
