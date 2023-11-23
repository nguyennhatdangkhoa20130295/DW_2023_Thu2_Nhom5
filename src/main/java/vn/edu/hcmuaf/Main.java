package vn.edu.hcmuaf;

import vn.edu.hcmuaf.controller.Controller;
import vn.edu.hcmuaf.dao.LotteryResultsDAO;
import vn.edu.hcmuaf.db.ConnectionManager;
import vn.edu.hcmuaf.entity.DataFileConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> list = new ArrayList<>();
        list.add("2023-11-13");
        list.add("2023-11-14");
        list.add("2023-11-15");
        list.add("2023-11-16");
        list.add("2023-11-17");
        list.add("2023-11-18");
        list.add("2023-11-19");
        list.add("2023-11-20");
        list.add("2023-11-21");
        list.add("2023-11-22");
        ConnectionManager manager = new ConnectionManager();
        LotteryResultsDAO dao = new LotteryResultsDAO();
        try (Connection controlConnection = manager.getControlDataSource().getConnection()) {
            List<DataFileConfig> dataFileConfigs = dao.getDataFileConfigList(controlConnection);
            Controller controller = new Controller();
            for (int i = list.size() - 1; i > -1; i--) {
                for (DataFileConfig config : dataFileConfigs) {
                    String status = dao.getStatus(controlConnection, config.getId());
                    try (Connection stagingConnection = manager.getStagingDataSource().getConnection()) {
                        if (status.equals("ERROR")) {
                            continue;
                        } else if (status.equals("FINISHED") || status.equals("CRAWLING")) {
                            controller.crawlData(controlConnection, list.get(i), config);
                        } else if (status.equals("EXTRACTING")) {
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}