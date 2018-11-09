package me.tianle.login.unit;

import java.sql.Connection;
import java.sql.ResultSet;

public class ConnectionCheck implements ConnectionApi{

    public static void ConectionCheck(Connection conn,String tableName){
        String sql = "SELECT COUNT(1) FROM " + tableName;
        ResultSet rs;
        try {
            rs = conn.prepareStatement(sql).executeQuery();
            int i = 0;
            if(rs.next()){
                i++;
            }
            if(i ==0){
                throw new RuntimeException(tableName + "表中没有数据");
            }
        }catch (Exception e){
            throw new RuntimeException("找不到表：" + tableName);
        }

    }
}
