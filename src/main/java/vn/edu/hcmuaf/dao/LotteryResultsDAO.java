package vn.edu.hcmuaf.dao;

import vn.edu.hcmuaf.entity.DataFileConfig;
import vn.edu.hcmuaf.entity.LotteryResults;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LotteryResultsDAO {

    public static List<DataFileConfig> getConfigurationsWithFlagOne(Connection connection) {
        List<DataFileConfig> configurations = new ArrayList<>();

        String query = "SELECT id, name, description, source_path, location, flag FROM data_file_configs WHERE flag = 1";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String source_path = resultSet.getString("source_path");
                String location = resultSet.getString("location");
                int flag = resultSet.getInt("flag");
                configurations.add(new DataFileConfig(id, name, description, source_path, location, flag));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return configurations;
    }

    public static String getStatus(Connection connection, int idConfig) {
        String status = "";
        String query = "SELECT `status` FROM data_files WHERE df_config_id=? AND status <> 'ERROR' ORDER BY file_timestamp DESC , data_files.id DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idConfig);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    status = resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    public void insertStatus(Connection connection, int df_config_id, String status, String date) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL InsertStatus(?,?,?)}")) {
            callableStatement.setInt(1, df_config_id);
            callableStatement.setString(2, status);
            callableStatement.setString(3, date);
            // Thực hiện stored procedure
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
