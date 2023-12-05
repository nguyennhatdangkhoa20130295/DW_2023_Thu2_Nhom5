package vn.edu.hcmuaf;

import vn.edu.hcmuaf.controller.Controller;
import vn.edu.hcmuaf.dao.LotteryResultsDAO;
import vn.edu.hcmuaf.db.DBConnection;
import vn.edu.hcmuaf.entity.DataFileConfig;
import vn.edu.hcmuaf.util.PropertiesReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {

        String date = LocalDate.now().toString();
        Properties propertiesDB = PropertiesReader.readPropertiesFile();

        if (propertiesDB != null) {
            String url = propertiesDB.getProperty("db.url");
            String username = propertiesDB.getProperty("db.username");
            String password = propertiesDB.getProperty("db.password");

            DBConnection db = new DBConnection(url, username, password);
            LotteryResultsDAO dao = new LotteryResultsDAO();
            try (Connection connection = db.getConnection()) {
                List<DataFileConfig> configs = dao.getConfigurationsWithFlagOne(connection);
                Controller controller = new Controller();
                for (DataFileConfig config : configs) {
                    String status = dao.getStatus(connection, config.getId());
                    if (status.equals("FINISHED") || status.equals("CRAWLING")) {
                        controller.crawlData(connection, date, config, dao);
                        controller.truncateAndInsertToStaging(connection, config, date, dao);
                        controller.transformData(config.getId(), connection, date, dao);
                        controller.loadToWH(config.getId(), connection, date, dao);
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        controller.loadToMart(config.getId(), connection, date, dao);
                    } else if (status.equals("EXTRACTING")) {
                        controller.truncateAndInsertToStaging(connection, config, date, dao);
                        controller.transformData(config.getId(), connection, date, dao);
                        controller.loadToWH(config.getId(), connection, date, dao);
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        controller.loadToMart(config.getId(), connection, date, dao);
                    } else if (status.equals("TRANSFORMING")) {
                        controller.transformData(config.getId(), connection, date, dao);
                        controller.loadToWH(config.getId(), connection, date, dao);
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        controller.loadToMart(config.getId(), connection, date, dao);
                    } else if (status.equals("WLOADING")) {
                        controller.loadToWH(config.getId(), connection, date, dao);
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        controller.loadToMart(config.getId(), connection, date, dao);
                    } else if (status.equals("AGGREGATING")) {
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        controller.loadToMart(config.getId(), connection, date, dao);
                    } else if (status.equals("MLOADING")) {
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                }
                DBConnection.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}