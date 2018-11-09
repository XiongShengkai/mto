package me.tianle.login.unit;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class JdbcConnection implements ConnectionApi {

    private static final Logger logger = LoggerFactory.getLogger(JdbcConnection.class);
    private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    private static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
    private static final String PREFIX_ORACLE = "jdbc:oracle:thin:@";
    private static final String PREFIX_MYSQL = "jdbc:mysql://";
    private static final String SUFFIX_MYSQL = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";


    private Connection connection(String url, String user, String password,String DRIVER) {
        logger.info(JSON.toJSONString(url));
        logger.info(JSON.toJSONString(user));
        logger.info(JSON.toJSONString(password));
        Connection conn = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }


    public Connection MysqlConnection(String url, String user, String password) {
        return connection(PREFIX_MYSQL+url+SUFFIX_MYSQL,user,password,DRIVER_MYSQL);
    }
    public Connection OracleConnection(String url, String user, String password) {
        return connection(PREFIX_ORACLE+url,user,password,DRIVER_ORACLE);
    }



}
