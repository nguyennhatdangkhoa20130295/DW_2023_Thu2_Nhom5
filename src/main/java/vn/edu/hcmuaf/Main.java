package vn.edu.hcmuaf;

import vn.edu.hcmuaf.controller.Controller;
import vn.edu.hcmuaf.dao.LotteryResultsDAO;
import vn.edu.hcmuaf.db.DBConnection;
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
//        list.add("2023-11-14");
//        list.add("2023-11-15");
//        list.add("2023-11-16");
//        list.add("2023-11-17");
//        list.add("2023-11-18");
//        list.add("2023-11-19");
//        list.add("2023-11-20");
//        list.add("2023-11-21");
//        list.add("2023-11-22");
        DBConnection db = new DBConnection();
        LotteryResultsDAO dao = new LotteryResultsDAO();
        try (Connection connection = db.getConnection()) {
            List<DataFileConfig> configs = dao.getConfigurationsWithFlagOne(connection);
            Controller controller = new Controller();
            for (int i = list.size() - 1; i > -1; i--) {
                for (DataFileConfig config : configs) {
                    String status = dao.getStatus(connection, config.getId());
                    if (status.equals("ERROR")) {
                        continue;
                    } else if (status.equals("FINISHED") || status.equals("CRAWLING")) {
                        controller.crawlData(connection, list.get(i), config);
                        controller.extractToStaging(connection, config, list.get(i));
                        controller.transformData(config.getId(), connection, list.get(i));
                        controller.loadToWH(config.getId(), connection, list.get(i));
                        controller.aggregateLottery(config.getId(), connection,list.get(i));
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    } else if (status.equals("EXTRACTING")) {
                        controller.extractToStaging(connection, config, list.get(i));
                        controller.transformData(config.getId(), connection, list.get(i));
                        controller.loadToWH(config.getId(), connection, list.get(i));
                        controller.aggregateLottery(config.getId(), connection,list.get(i));
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    } else if (status.equals("TRANSFORMING")) {
                        controller.transformData(config.getId(), connection, list.get(i));
                        controller.loadToWH(config.getId(), connection, list.get(i));
                        controller.aggregateLottery(config.getId(), connection,list.get(i));
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    } else if (status.equals("WLOADING")) {
                        controller.loadToWH(config.getId(), connection, list.get(i));
                        controller.aggregateLottery(config.getId(), connection,list.get(i));
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    }
                    else if (status.equals("AGGREGATING")) {
                        controller.aggregateLottery(config.getId(), connection,list.get(i));
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    } else if (status.equals("MLOADING")) {
                        controller.loadToMart(config.getId(), connection,list.get(i));
                    }
                }
            }
            DBConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}