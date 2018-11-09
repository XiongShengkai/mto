package me.tianle.login.unit;

import com.alibaba.fastjson.JSON;
import me.tianle.login.bean.DataSourceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @Description:
 * @auther: sheen.lee
 * @date: 10:18 2018/8/1
 *
 */
public class ConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);

    private static final JdbcConnection jdbcConnection = new JdbcConnection();

    /**
     *
     * @Description: 连接数据库
     *                 1.验证源数据：a.表是否存在，b.验证表是否有数据；
     *                 2.验证目标源：是否存在。
     * @auther: sheen.lee
     * @date: 10:14 2018/8/1
     * @param: [DataSourceRequest]
     * @return: java.sql.Connection
     *
     */
    public static Connection getConnection(DataSourceRequest dataSource){
        logger.info(JSON.toJSONString(dataSource));
        Connection conn = null;
//        String DRIVER="oracle.jdbc.driver.OracleDriver";
//        String url = "jdbc:oracle:" + "thin:@127.0.0.1:1521:XE";// 127.0.0.1是本机地址，XE是精简版Oracle的默认数据库名
//        String user = "user";// 用户名,系统默认的账户名
//        String password = "***";// 你安装时选设置的密码
        String DRIVER=dataSource.getBooleanTarget()?"oracle.jdbc.driver.OracleDriver":"com.mysql.jdbc.Driver";
        String url =
                (dataSource.getBooleanTarget()?"jdbc:oracle:thin:@":"jdbc:mysql://")
//                +"localhost:3306/db_test"
                +(dataSource.getBooleanTarget()?dataSource.getTargetIp():
                        dataSource.getSourceIp()
                                +"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai");// 127.0.0.1是本机地址，XE是精简版Oracle的默认数据库名
        String user = (dataSource.getBooleanTarget()?dataSource.getTargetUser():dataSource.getSourceUser());// 用户名,系统默认的账户名
        String password = (dataSource.getBooleanTarget()?dataSource.getTargetPassword():dataSource.getSourcePassword());// 你安装时选设置的密码
        try {
            Class.forName(DRIVER);// 加载数据库驱动程序
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
            return conn;
        }
        try {
            conn = DriverManager.getConnection(url, user, password);// 获得Connection对象
        } catch (Exception e) {
            logger.info(e.getMessage());
            logger.info(url+"  "+user+"  "+password);
            e.printStackTrace();
            return conn;
        }

        ResultSet rs;
//        String sql = "SELECT ID,MEDID,COMMODITYNAME,JBBM,JBMC,JZHOSPITALID,DOCTORNAME  FROM DISEASE_DRUG_ASSOCIATE_test";
        String sql = "SELECT COUNT(*) from "+ (dataSource.getBooleanTarget()?"\""+dataSource.getTargetTable()+"\"":dataSource.getSourceTable());
        logger.info(sql);
        try {
            rs = conn.prepareStatement(sql).executeQuery();
            int i = 0;
            if(rs.next()){
                i++;
            }
            if(!dataSource.getBooleanTarget() && i ==0){
                throw new RuntimeException("源数据表没有数据");
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage()+ (dataSource.getBooleanTarget()?dataSource.getTargetTable():dataSource.getSourceTable()) );
        }
        return conn;
    }


    /**
     *
     * @Description: 查询源数据所有列，以及源数据库的字段名，拼接占位符等
     *
     * @auther: sheen.lee
     * @date: 10:06 2018/8/1
     * @param: [dataSource]
     * @return: java.util.List<java.util.List<java.lang.String>>
     *
     */
    public static List<List<String>> tableInput(DataSourceRequest dataSource) throws FileNotFoundException,
            SQLException {
        String tableStr = dataSource.getSourceTable();
//        List<List<String>> FindList = new ArrayList<List<String>>();
        List<String> columnNames = new ArrayList<>();//列名
        List<String> columnTypes = new ArrayList<>();//列type
        List<String> placeholders = new ArrayList<>();//问号占位符
        List<List<String>> columnValues = new ArrayList<>();//值

        Connection con = ConnectionFactory.getConnection(dataSource);
        JdbcConnection jdbcConnection = new JdbcConnection();

/*        con = jdbcConnection.MysqlConnection(
                dataSource.getSourceUser()
                        +"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
                dataSource.getSourceUser(),
                dataSource.getSourcePassword());
        ConnectionCheck.ConectionCheck(con,dataSource.getSourceTable());*/





        PreparedStatement pre = null;
        ResultSet resultSet = null;
//        String sql = "SELECT ID,MEDID,COMMODITYNAME,JBBM,JBMC,JZHOSPITALID,DOCTORNAME  FROM DISEASE_DRUG_ASSOCIATE_test";
        String sql = "SELECT * from "+dataSource.getSourceTable()
//                + " WHERE series!=101807250070268 ORDER BY end_day_temp DESC "
                +" LIMIT 0,10000000";//此处设置长度，mycat只能查询100条
        logger.info(sql);
        try {
            pre = con.prepareStatement(sql);
            resultSet = pre.executeQuery();
//            String[] columu = {"ID","MEDID","COMMODITYNAME","JBBM","JBMC","JZHOSPITALID","DOCTORNAME"};
            int i=0;//用来计数，分每10000条数据插入，和第一次才拼接字符串
//            String str = "";//拼接问号【中间】字符串用于jdbc操作
            String columnNameStr = "";//拼接列名字符串用于jdbc操作
            String placeholderStr = "";//拼接占位符字符串用于jdbc操作
//            StringBuffer columnName = new StringBuffer();//最终列名字符串
            tableStr = dataSource.getTargetTable();//用于log输出的表名
//            if(!resultSet.wasNull()){//不知道这个wasNull是个什么方法！
//                getColumnValues(con,columnValues,columnNames.size(),dataSource);
//            }
            while (resultSet.next()) {
                if(i==0){
                    getColumnNameList(dataSource,columnNames,columnTypes,placeholders,con);
                }
//                List<String> minList = new ArrayList<String>();
//                int total = resultSet.getMetaData().getColumnCount();//获取总共的全部列计数
//                StringBuffer sb = new StringBuffer();
//                for (int j = 1; j <= total; j++) {
////                    String s = resultSet.getString(i);
//                    String col = resultSet.getString(j);//列数据
//                    minList.add(col);//把数据加入到minList中
//
//                    if(i==0){
//                        //只需要获取第一列的列名和?即可
//                        // 注意：此处不能这样比较！！！！！会有bug，如果第一列的数据为空，则会出现问题
//                        // oracle插入的时候会有此种限制，
//                        if(col!=null &&col.length() > 19 &&
//                                col.charAt(4)=='-'&&
//                                col.charAt(7) == '-')
//                        {//【重要】判断是否是时间类型，需要使用to_date转化
//                            sb.append(",to_date(substr(?,1,19),\'yyyy-mm-dd hh24:mi:ss\')");
//                        }else if(col!=null &&col.length() < 19 &&
//                                col.length() > 9&&
//                                col.charAt(4)=='-'&&
//                                col.charAt(7) == '-'){
//                            sb.append(",to_date(?,\'yyyy-mm-dd\')");
//                        }
//                        else
//                        {//不是时间类型的，直接加?
//                            sb.append(",?");
//                        }
//
//                        String getColumnName = resultSet.getMetaData().getColumnName(j);
//                        if(getColumnName!=null && getColumnName.length()>30){//oracle有标识符长度限制，不能长于30个
//                            getColumnName = getColumnName.substring(0,30);
//                        }
//                            /**
//                             *
//                             * 最后一个数据字符串，因为名字是 _mycat_op_time
//                             * 【PS：这是不符合规范的取名，oracle不能识别，需要加""双引号】
//                             *  但是注意的是，加了引号之后，oracle会区分列名大小写，如果列名大小写不一致会导致找不到列名的错误
//                             *
//                             */
//                        columnName.append(","+ (getColumnName.charAt(0)=='_'?"\""+getColumnName+"\"":getColumnName));
//                    }
//                }
//                if(i==0){//去掉字符串最前面的逗号
//                    str = sb.substring(1,sb.length());
//                    columnNameStr = columnName.substring(1,columnName.length());
//                }
//                for(String each:columu){
//                    minList.add(resultSet.getString(each));
//                }
//                FindList.add(minList);
//                i++;
//                if(i%10000==0){           //设置的每次提交大小为10000
//                    logger.info(columnNameStr);
//                    executeManySql(FindList,dataSource,str,columnNameStr);
//                    FindList.removeAll(FindList);
//                    System.out.println(i);
//                }

                columnNameStr = String.join(",",columnNames);
                placeholderStr = String.join(",",placeholders);
//                pre = con.prepareStatement(sql);
//                resultSet = pre.executeQuery();
                List<String> columnValue = new ArrayList<>();
                for (int k = 1; k <= columnNames.size(); k++) {
                    columnValue.add(resultSet.getString(k));
                }
                columnValues.add(columnValue);

                i++;
                if(i%10000==0){           //设置的每次提交大小为10000
                    logger.info(columnNameStr);
                    executeManySql(columnValues,dataSource,placeholderStr,columnNameStr);
                    columnValues.removeAll(columnValues);
                    System.out.println(i);
                }
            }
            executeManySql(columnValues,dataSource,placeholderStr,columnNameStr);//最后别忘了提交剩余的
            return columnValues;
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage()+" ["+tableStr+"] ");
        } finally {
            try {
                pre.close();// 关闭Statement
            } catch (Exception e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
            try {
                con.close();// 关闭Connection
            } catch (Exception e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**     <p>主要功能为：
    *                   把mysql数据库表中数据转移到oracle数据表中
    *                   （注：oracle中已经建好表了，mysql中需要转移的字段名请在oracle中提前建好，并对应好字段长度和类型等）。
    *                   需要有以下的条件：
    *                                   1.mysql表中存在的字段名oracle中必须存在(否则会失败)；
    *                                   2.必须保证oracle中的非空字段在mysql中有值；
    *                                   3.必须保证oracle中的字段名为大写(否则会出现标识符无效的提示[找不到字段名])；
    *                                   4.如果mysql中存在下划线开头的数据，请在oracle中对应大小写，否则会出错；
    *                                   5.mysql中字段名大于30的，会自动截断为30位(oracle标识符长度<30)；
    *       </p>
    */
    public static  void mto(DataSourceRequest dataSource) throws Exception{
//        DataSource dataSource =new DataSource();
        long sbeginTime = System.currentTimeMillis();
        tableInput(dataSource);
        long sendTime = System.currentTimeMillis();
        System.out.print((sendTime-sbeginTime)/1000+" s");
    }

    public static void main(String[] args) throws Exception{
        DataSourceRequest dataSource =new DataSourceRequest();
//        dataSource.setTargetIp("172.16.2.209:1521/LDREPORT");
        dataSource.setTargetIp("139.196.76.129:1521/YKREPORT");
        dataSource.setSourceIp("localhost:3306/db_test");
//        dataSource.setTargetUser("ldreport");
        dataSource.setTargetUser("ykreport");
        dataSource.setSourceUser("root");
//        dataSource.setTargetPassword("ldreport");
        dataSource.setTargetPassword("Ykrpt123");
        dataSource.setSourcePassword("123456");
        dataSource.setBooleanTarget(false);
        dataSource.setTargetTable("newtable222");
        dataSource.setSourceTable("newtable");

        List<String> columnNames = new ArrayList<>();//列名
        List<String> columnTypes = new ArrayList<>();//列type
        List<String> placeholders = new ArrayList<>();//问号占位符
        long sbeginTime = System.currentTimeMillis();
        mto(dataSource);
//        getColumnNameList(dataSource,columnNames,columnTypes,placeholders,null);
        long sendTime = System.currentTimeMillis();
        System.out.print((sendTime-sbeginTime)/1000+" s");
//
//        Connection con = ConnectionFactory.getConnection();
//        con.setAutoCommit(false);
//        Statement stat = null;
//        PreparedStatement pst = (PreparedStatement) con
//                .prepareStatement("insert into newtable values (?,?,?)");
//        for (int k = 0; k < 100; k++) {
//        for (int i = 0; i < 10000; i++) {
//            for (int j = 0; j < 3; j++) {
//                pst.setString(j+1, i+j+"");
//            }
//// 把一个SQL命令加入命令列表
//            pst.addBatch();
//        }
//// 执行批量更新
//        pst.executeBatch();
//        // 语句执行完毕，提交本事务
//        con.commit();
//
//
//        }
//        pst.close();
//        con.close();//一定要记住关闭连接，不然mysql回应为too many connection自我保护而断开。
    }


    /**
     *
     * @Description: 执行批量插入Oracle数据库的操作
     *
     * @auther: sheen.lee
     * @date: 9:58 2018/8/1
     * @param: [FindList 需要插入的数据,
     *           dataSource 数据源,
     *           placeholderStr 占位符,
     *           columnNameStr 需要插入的字段]
     * @return: void
     *
     */
    public static void executeManySql(List<List<String>> FindList,
                                      DataSourceRequest dataSource,
                                      String placeholderStr,
                                      String columnNameStr) throws SQLException {
        dataSource.setBooleanTarget(true);
       Connection con = ConnectionFactory.getConnection(dataSource);
 /*             con = jdbcConnection.OracleConnection(
                dataSource.getSourceUser()
                        +"",
                dataSource.getSourceUser(),
                dataSource.getSourcePassword());
                ConnectionCheck.ConectionCheck(con,dataSource.getTargetTable());*/

        if (con == null){
            throw new RuntimeException("目标数据库连接异常");
        }
        con.setAutoCommit(false);
        Statement stat = null;
        String sql = "insert into \""+ dataSource.getTargetTable() + "\" ("+columnNameStr +") values( " + placeholderStr + " )";
        PreparedStatement pst = (PreparedStatement) con
                .prepareStatement(sql);
        for (List<String> minList: FindList) {
            logger.info(JSON.toJSONString(minList));
            for(int i=0;i<minList.size();i++){
                pst.setString(i+1, minList.get(i));
            }
//            pst.executeUpdate();
            pst.addBatch();
        }
        logger.info(sql);
        pst.executeBatch();
        con.commit();
        pst.close();
        con.close();
    }

        /*
    Class.forName("com.mysql.jdbc.Driver");
    String url = "jdbc:mysql://localhost:3306/spider";
    String user = "root";
    String password = "***";*/

    /**
     * 拼接字段名，已经占位符
     *
     * @param dataSource 属性
     * @param columnNames 列名集合
     * @param columnTypes 列类型集合
     * @param placeholders 占位符集合
     * @param con
     */
        private static void getColumnNameList(DataSourceRequest dataSource,
                                              List<String> columnNames,
                                              List<String> columnTypes,
                                              List<String> placeholders,
                                              Connection con){

//            Connection con = ConnectionFactory.getConnection(dataSource);
            DatabaseMetaData metaData;
            ResultSet rs = null;
            ResultSet crs = null;
//            List<String> columnNames = new ArrayList<>();
//            List<String> columnTypes = new ArrayList<>();
//            List<List<String>> columnValues = new ArrayList<>();
            try {
                metaData = con.getMetaData();
                rs = metaData.getTables(null, "%", "%", new String[] { "TABLE" });
                while (rs.next()){
                    String tableName = rs.getString("TABLE_NAME");
                    if(dataSource.getSourceTable().equalsIgnoreCase(tableName)){

                        crs = metaData.getColumns(null, "%", tableName, "%");
                        while (crs.next()){
                            String placeholder = "?";
                            String columnName = crs.getString("COLUMN_NAME");
                            if(Objects.nonNull(columnName) && columnName.length()>30){//截断标识符30位最长
                                columnName = columnName.substring(0,30);
                            }
                            /**下划线开头的标识符加引号；下划线开头的字段是不好的命名方式。
                             * 注意加双引号之后，字段大小写就固定了， Oracle在执行SQL的时候就不会再自动转化为大写
                            */
                            if(Objects.nonNull(columnName) && columnName.charAt(0)=='_'){
                                columnName = "\""+columnName+"\"";
                            }
                            String columnType = crs.getString("TYPE_NAME");
                            /**
                             * 拼接日期占位符，使用TO_DATE函数
                             */
                            if(Objects.nonNull(columnType) && "DATE".equalsIgnoreCase(columnType)){
                                placeholder = "TO_DATE(?,\'yyyy-mm-dd\')";
                            }else if(Objects.nonNull(columnType)
                                    && ("DATETIME".equalsIgnoreCase(columnType)||"TIMESTAMP".equalsIgnoreCase(columnType))){
                                placeholder = "TO_DATE(substr(?,1,19),\'yyyy-mm-dd hh24:mi:ss\')";
                            }
                            columnNames.add(columnName);
                            columnTypes.add(columnType);
                            placeholders.add(placeholder);
                        }
                        logger.info(JSON.toJSONString(columnNames));
                        logger.info(JSON.toJSONString(columnTypes));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("查询数据库表结构失败:"+e.getMessage());
            }finally {
                try {
                    if (null != rs) {
                        rs.close();
                    }
                    if (null != crs) {
                        crs.close();
                    }
                }catch (Exception e){

                }
            }
        }


        private static void getColumnValues(Connection con,
                                            List<List<String>> columnValues,
                                            int columnNameSize,
                                            DataSourceRequest dataSource){

            PreparedStatement pre = null;
            ResultSet resultSet = null;
            String sql = "SELECT * from "+dataSource.getSourceTable()
                    +" LIMIT 0,10000000";//此处设置长度，mycat只能查询100条
            logger.info(sql);
            try {
                pre = con.prepareStatement(sql);
                resultSet = pre.executeQuery();
                while (resultSet.next()){
                    List<String> columnValue = new ArrayList<>();
                    for (int i = 1; i<= columnNameSize; i++) {
                        columnValue.add(resultSet.getString(i));
                    }
                    columnValues.add(columnValue);
                }
                logger.info(JSON.toJSONString(columnValues));
            }catch (Exception e){

            }
        }

    /**
     * 检查目标字符串是否小写字符串
     * @param param
     * @return
     */
    private static boolean checkCase(String param){
            if (Objects.isNull(param)) {
                return false;
            }
            if(Objects.equals(param,param.toUpperCase())){
                return false;
            }
            return true;
        }
}
