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

        // 1. Đọc file config.properties
        Properties propertiesDB = PropertiesReader.readPropertiesFile();

        if (propertiesDB != null) {
            // 3. Lấy trường run trong file config.properties
            String run = propertiesDB.getProperty("run");

            // 3.1. Nếu trường run = auto thì lấy ngày hiện tại
            String date = LocalDate.now().toString();
            // 3.2. Nếu trường run != auto thì lấy ngày trong trường run
            if (!run.equals("auto")) date = propertiesDB.getProperty("run");

            // 4. Lấy url, username, password trong file config.properties
            String url = propertiesDB.getProperty("db.url");
            String username = propertiesDB.getProperty("db.username");
            String password = propertiesDB.getProperty("db.password");

            LotteryResultsDAO dao = new LotteryResultsDAO();

            // 5. Kết nối đến DB Control
            DBConnection db = new DBConnection(url, username, password);

            // 6. Kêt nối thành công đến DB Control
            try (Connection connection = db.getConnection()) {
                // 7. Lấy tất cả các data file config có flag = 1
                List<DataFileConfig> configs = dao.getConfigurationsWithFlagOne(connection);
                Controller controller = new Controller();

                // 8. Duyệt qua từng data file config
                for (DataFileConfig config : configs) {
                    // 9. Join với bảng data_file để lấy status có file_timestamp mới nhất, ngoại trừ status = "ERROR"
                    String status = dao.getStatus(connection, config.getId());

                    // 10. Nếu status = "FINISHED" thi thực hiện crawl data đến load to mart
                    // 17. Nếu status = "CRAWLING" thi thực hiện crawl data đến load to mart
                    if (status.equals("FINISHED") || status.equals("CRAWLING")) {
                        // 11. Crawl data từ source
                        controller.crawlData(connection, date, config, dao);
                        // 12. extract file vào staging
                        controller.truncateAndInsertToStaging(connection, config, date, dao);
                        // 13. transform data sang skey
                        controller.transformData(config.getId(), connection, date, dao);
                        // 14. load to warehouse
                        controller.loadToWH(config.getId(), connection, date, dao);
                        // 15. aggregate lottery
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 18. Nếu status = "EXTRACTING" thì thực hiện từ extract file vào staging đến load to mart
                    else if (status.equals("EXTRACTING")) {
                        // 12. extract file vào staging
                        controller.truncateAndInsertToStaging(connection, config, date, dao);
                        // 13. transform data sang skey
                        controller.transformData(config.getId(), connection, date, dao);
                        // 14. load to warehouse
                        controller.loadToWH(config.getId(), connection, date, dao);
                        // 15. aggregate lottery
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 19. Nếu status = "TRANSFORMING" thì thực hiện từ transform data đến load to mart
                    else if (status.equals("TRANSFORMING")) {
                        // 13. transform data sang skey
                        controller.transformData(config.getId(), connection, date, dao);
                        // 14. load to warehouse
                        controller.loadToWH(config.getId(), connection, date, dao);
                        // 15. aggregate lottery
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 20. Nếu status = "WLOADING" thì thực hiện từ load to warehouse đến load to mart
                    else if (status.equals("WLOADING")) {
                        // 14. load to warehouse
                        controller.loadToWH(config.getId(), connection, date, dao);
                        // 15. aggregate lottery
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 21. Nếu status = "AGGREGATING" thì thực hiện từ aggregate lottery đến load to mart
                    else if (status.equals("AGGREGATING")) {
                        // 15. aggregate lottery
                        controller.aggregateLottery(config.getId(), connection, date, dao);
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 22. Nếu status = "MLOADING" thì thực hiện load to mart
                    else if (status.equals("MLOADING")) {
                        // 16. load to mart
                        controller.loadToMart(config.getId(), connection, date, dao);
                    }
                    // 23. Nếu còn data file config nào có flag = 1 thì tiếp tục lặp
                }

                // 24. Đóng kết nối đến DB Control
                DBConnection.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}