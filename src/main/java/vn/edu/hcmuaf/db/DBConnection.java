package vn.edu.hcmuaf.db;

import vn.edu.hcmuaf.entity.DataFileConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static Connection connection;
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/db_control";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public DBConnection() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                System.out.println(DATABASE_URL);
                // Tạo kết nối đến cơ sở dữ liệu
                connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể thiết lập kết nối đến cơ sở dữ liệu.");
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối đến cơ sở dữ liệu.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
