package io.github.beijiyi.dlsql;

import java.util.Collection;
import java.util.Map;

/**
 * @Description: <br>
 * @CreateDate: Created in 2020/4/30 16:09 <br>
 * @Author: <a href="1042040685@qq.com">dl</a>
 */
public class Uitl {
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

    public static  void s(){
        System.out.println("Sdfdsssssssfdsf");
    }
}
