package com.zmy.core.statement;

import java.sql.*;

/**
 * 数据库操作和结果处理分发
 *
 * @author zhaomy
 */
public class ZStatementHandler {



    public <T> T query(String statement, String parameter) {
        try{
            Connection connection = getConnection();
            String sql = String.format(statement, Integer.parseInt(parameter));
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getResultSet();

//            Test test = new Test();
//            while (rs.next()) {
//                test = new Test();
//                test.setId(rs.getInt(1));
//                test.setNums(rs.getInt(2));
//                test.setName(rs.getString(3));
//            }
//            return (T)test;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取数据库连接
     * @return
     */
    public Connection getConnection(){
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/gp?useUnicode=true&characterEncoding=utf-8&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String username = "root";
        String password = "MyNewPass@123";
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
