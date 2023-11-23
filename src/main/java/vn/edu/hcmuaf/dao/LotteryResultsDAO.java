package vn.edu.hcmuaf.dao;

import vn.edu.hcmuaf.entity.DataFileConfig;
import vn.edu.hcmuaf.entity.LotteryResults;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LotteryResultsDAO {

    public void insertDataToStaging(LotteryResults lotteryResults, Connection connection) {
        String query = "INSERT INTO lottery_results_staging(date, region, lottery_code, province, prize, number, dtChanged) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, lotteryResults.getDate());
            ps.setString(2, lotteryResults.getRegion());
            ps.setString(3, lotteryResults.getLotteryId());
            ps.setString(4, lotteryResults.getProvince());
            ps.setString(5, lotteryResults.getPrize());
            ps.setString(6, lotteryResults.getNumber());
            ps.setString(7, lotteryResults.getDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DataFileConfig> getDataFileConfigList(Connection connection) {
        List<DataFileConfig> dataFileConfigs = new ArrayList<>();
        String query = "SELECT id, name, description, source_path, location, flag FROM data_file_configs WHERE flag = 1";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String source_path = rs.getString("source_path");
                String location = rs.getString("location");
                int flag = rs.getInt("flag");
                DataFileConfig dataFileConfig = new DataFileConfig(id, name, description, source_path, location, flag);
                dataFileConfigs.add(dataFileConfig);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataFileConfigs;
    }

    public void insertDataFileWithNewStatus(Connection connection, int df_config_id, String newStatus) {
        String query = "INSERT INTO data_files(df_config_id, status) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, df_config_id);
            ps.setString(2, newStatus);
            ps.executeUpdate();
            System.out.println("status: " + newStatus);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStatus(Connection connection, int df_config_id) {
        String status = "";
        String query = "SELECT `status` FROM data_files WHERE df_config_id = ? ORDER BY file_timestamp DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, df_config_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                status = rs.getString("status");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return status;
    }
}
