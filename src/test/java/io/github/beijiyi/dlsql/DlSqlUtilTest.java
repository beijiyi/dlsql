package io.github.beijiyi.dlsql;
import io.github.beijiyi.dlsql.DLDbDialectType;
import io.github.beijiyi.dlsql.DateFormatUtil;
import io.github.beijiyi.dlsql.DlSqlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DlSqlUtilTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("------likeForMap-------");
		Map<String, Object> map =new HashMap<String, Object>();
		String value="村";
		map.put("sr_name", value);
		map.put("sr_remark", value);
		map.put("sr_type", value);
		System.out.println(DlSqlUtil.create().fromTable("sys_role").likeForMap(map).sqlAll());

		System.out.println("------eqForAll-------");
		System.out.println(DlSqlUtil.create().fromTable("sys_role").eqForMap(map).sqlAll());


		System.out.println("------ge-------");
		System.out.println(DlSqlUtil.create().ge("sr_use",1));

		System.out.println("------between-------");
		DlSqlUtil sqlBetween=DlSqlUtil.create();
		sqlBetween.setDbType(DLDbDialectType.ORACLE);
		sqlBetween.between("age", DateFormatUtil.StoD("2015-07-09", "yyyy-MM-dd"),DateFormatUtil.StoD("2015-07-10", "yyyy-MM-dd"));
		System.out.println(sqlBetween);


		System.out.println("------in-------");

		Integer[] ids={1,2,34,5,6,7};

		List aList= new ArrayList();
		aList.add(23);
		aList.add(444);
		aList.add(999);

		Object[] idss={"323",23,"333","444"};
		System.out.println(DlSqlUtil.create().in("name",idss));
	}
}
