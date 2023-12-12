package vn.edu.hcmuaf.db;

import vn.edu.hcmuaf.util.SendEmailError;

import java.io.IOException;
import java.sql.*;

public class DBConnection {
    private static Connection connection;
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    public DBConnection(String URL, String USERNAME, String PASSWORD) {
        this.URL = URL;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Connected successfully " + URL);
            } catch (SQLException e) {
                e.printStackTrace();
                // 27. Gửi email thông báo lỗi
                SendEmailError.sendErrorEmail("Connect database", e.getMessage());
                throw new RuntimeException("Không thể thiết lập kết nối đến cơ sở dữ liệu.");
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                // Đóng kết nối đến cơ sở dữ liệu
                connection.close();
                System.out.println("Đã đóng kết nối đến cơ sở dữ liệu.");
            } catch (SQLException e) {
                e.printStackTrace();
                // Gửi email thông báo lỗi
                SendEmailError.sendErrorEmail("Close connection", e.getMessage());
            }
        }
    }


}
