package vn.edu.hcmuaf.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vn.edu.hcmuaf.dao.LotteryResultsDAO;
import vn.edu.hcmuaf.entity.DataFileConfig;
import vn.edu.hcmuaf.entity.LotteryResults;
import vn.edu.hcmuaf.util.SendEmailError;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;


public class Controller {
    public void crawlData(Connection connection, String date, DataFileConfig config, LotteryResultsDAO dao) throws IOException, SQLException {
        // 11.1. Insert vào bảng db_control.data_files dòng dữ liệu với status = CRAWLING
        dao.insertStatus(connection, config.getId(), "CRAWLING", date);
        String dateObj = formatDate(date, "dd-MM-yyyy");
        String dateCheck = formatDate(date, "dd/MM/yyyy");
        // 11.2. Trích xuất và xử lý dữ liệu từ trang web thông qua các tham số truyền vào: source_path của đối tượng DataFileConfig và date
        String url = config.getSource_path() + dateObj + ".html";
        try {
            Document doc = Jsoup.connect(url).get();
            Elements lotteryElements = doc.select("div.box_kqxs");
            List<LotteryResults> results = new ArrayList<>();
            for (Element lotteryElement : lotteryElements) {
                String regionName = lotteryElement.select("div.title > a").get(0).text();
                String dateNow = lotteryElement.select("div.title > a").get(1).text();
                if (dateNow.equals(dateCheck) && (regionName.equals("KẾT QUẢ XỔ SỐ Miền Nam") || regionName.equals("KẾT QUẢ XỔ SỐ Miền Trung"))) {
                    Elements lotteryLeft = lotteryElement.select("div.content table[class=leftcl] tbody");
                    Elements lotteryRight = lotteryElement.select("div.content table[class=rightcl] tbody");

                    for (Element e : lotteryRight) {
                        String lotteryId = e.select("td.matinh").text();
                        String province = e.select("td.tinh").text();
                        Elements rows = e.select("tr");
                        for (int i = 3; i < rows.size() + 1; i++) {
                            String prize = lotteryLeft.select("tr:nth-child(" + i + ")").text();
                            List<String> numbers = e.select("tr:nth-child(" + i + ") td div").eachText();
                            for (String number : numbers) {
                                results.add(new LotteryResults(date, regionName, lotteryId, province, prize, number));
                            }
                        }
                    }
                } else if (dateNow.equals(dateCheck) && regionName.equals("KẾT QUẢ XỔ SỐ Miền Bắc")) {
                    Elements lotteryContent = lotteryElement.select("div.content table[class=bkqtinhmienbac] tbody");
                    Elements rows = lotteryContent.select("tr");

                    String thu = lotteryContent.select("tr:nth-child(1) td").first().text();
                    String province = getProvinceForMB(thu);
                    String lotteryId = "XSMB";
                    List<String> codes = List.of(lotteryContent.select("tr:nth-child(1) td div.loaive_content").text().split("-"));
                    for (String code : codes) {
                        results.add(new LotteryResults(date, regionName, lotteryId, province, "Mã ĐB", code));
                    }

                    for (int i = 2; i < rows.size() + 1; i++) {
                        String prize = lotteryContent.select("tr:nth-child(" + i + ") td").get(0).text();
                        List<String> numbers = lotteryContent.select("tr:nth-child(" + i + ") td:nth-child(2) div").eachText();
                        for (String number : numbers) {
                            results.add(new LotteryResults(date, regionName, lotteryId, province, prize, number));
                        }
                    }
                }
            }
            // 11.3. Lưu dữ liệu đã trích xuất và xử lý vào file Excel
            writeDataToExcel(results, connection, config, date);
            // 11.11. Insert vào bảng db_control.data_files dòng dữ liệu với status = CRAWLED
            dao.insertStatus(connection, config.getId(), "CRAWLED", date);
            System.out.println("Crawl successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            // 25. Insert vào bảng db_control.data_files dòng dữ liệu với status = ERROR
            dao.insertStatus(connection, config.getId(), "ERROR", date);
            // 26. Gửi mail thông báo lỗi
            SendEmailError.sendErrorEmail("CRAWLING", "Error while crawling data: " + e.getMessage());
            // 24. Đóng kết nối db_control
            connection.close();
        }
    }

    private String formatDate(String dateString, String pattern) {
        String result = "";
        String year = dateString.substring(0, 4);
        String month = dateString.substring(5, 7);
        String day = dateString.substring(8, 10);
        if (pattern.equals("dd-MM-yyyy")) {
            result = day + "-" + month + "-" + year;
        }
        if (pattern.equals("dd/MM/yyyy")) {
            result = day + "/" + month + "/" + year;
        }
        return result;
    }

    private String getProvinceForMB(String input) {
        String result = "";
        if (input.equals("Thứ hai")) {
            result = "Thủ đô Hà Nội";
        } else if (input.equals("Thứ ba")) {
            result = "Quảng Ninh";
        } else if (input.equals("Thứ tư")) {
            result = "Bắc Ninh";
        } else if (input.equals("Thứ năm")) {
            result = "Thủ đô Hà Nội";
        } else if (input.equals("Thứ sáu")) {
            result = "Hải Phòng";
        } else if (input.equals("Thứ bảy")) {
            result = "Nam Định";
        } else if (input.equals("Chủ nhật")) {
            result = "Thái Bình";
        }
        return result;
    }

    public void writeDataToExcel(List<LotteryResults> lotteryResults, Connection connection, DataFileConfig config, String date) throws SQLException {
        // 11.4. Kiểm tra xem thư mục cha chứa file excel đã tồn tại hay chưa?
        createDirectoryIfNotExists(connection, config, date);

        // 11.5. Lưu file với đường dẫn: D:\\+location+\\crawl_lottery_results_+date+.xlsx
        String excelFilePath = config.getLocation() + "\\crawl_lottery_results_" + date + ".xlsx";
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet spreadsheet = workbook.createSheet("KQXS");

            // Tạo header
            Row headerRow = spreadsheet.createRow(0);
            headerRow.createCell(0).setCellValue("Ngày");
            headerRow.createCell(1).setCellValue("Khu vực");
            headerRow.createCell(2).setCellValue("Mã xổ số");
            headerRow.createCell(3).setCellValue("Tỉnh");
            headerRow.createCell(4).setCellValue("Loại giải");
            headerRow.createCell(5).setCellValue("Số trúng");

            // Thêm dữ liệu
            int rowNum = 1;
            for (LotteryResults result : lotteryResults) {
                Row row = spreadsheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getDate());
                row.createCell(1).setCellValue(result.getRegion());
                row.createCell(2).setCellValue(result.getLotteryId());
                row.createCell(3).setCellValue(result.getProvince());
                row.createCell(4).setCellValue(result.getPrize());
                row.createCell(5).setCellValue(result.getNumber());
            }

            // Lưu file
            // 11.9. Lưu thành công?
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                fos.close();
                // 11.10. In ra "Data has been saved: " + đường dẫn đã lưu
                System.out.println("Data has been saved: " + excelFilePath);
            } catch (IOException e) {
                e.printStackTrace();
                // 11.12. Insert vào bảng db_control.data_files dòng dữ liệu với status = ERROR
                new LotteryResultsDAO().insertStatus(connection, config.getId(), "ERROR", date);
                // 11.13. Gửi mail thông báo lỗi
                SendEmailError.sendErrorEmail("WRITING DATA", "Error while writing data to file: " + e.getMessage());
                // 24. Đóng kết nối db_control
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDirectoryIfNotExists(Connection connection, DataFileConfig config, String date) throws SQLException {
        Path path = Paths.get(config.getLocation());
        // 11.6. Tạo tất cả các thư mục cha nếu chúng không tồn tại
        if (!Files.exists(path)) {
            // 11.7. Tạo thư mục thành công?
            try {
                Files.createDirectories(path);
                // 11.8. In ra màn hình thông báo đã tạo thư mục thành công
                System.out.println("Đã tạo thư mục: " + config.getLocation());
            } catch (IOException e) {
                e.printStackTrace();
                // 11.12. Insert vào bảng db_control.data_files dòng dữ liệu với status = ERROR
                new LotteryResultsDAO().insertStatus(connection, config.getId(), "ERROR", date);
                // 11.13. Gửi mail thông báo lỗi
                SendEmailError.sendErrorEmail("CREATING DIRECTORY", "Error while creating directory: " + e.getMessage());
                // 24. Đóng kết nối db_control
                connection.close();
            }
        }
    }


    public void extractToStaging(String pathFile, Connection connection) {
        try (FileInputStream excelFile = new FileInputStream(pathFile);
             Workbook workbook = new XSSFWorkbook(excelFile)) {

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                String date = currentRow.getCell(0).getStringCellValue();
                String region = currentRow.getCell(1).getStringCellValue();
                String lottery_code = currentRow.getCell(2).getStringCellValue();
                String province = currentRow.getCell(3).getStringCellValue();
                String prize = currentRow.getCell(4).getStringCellValue();
                String number = currentRow.getCell(5).getStringCellValue();

                // Insert data into the database
                String callProcedure = "{CALL ExtractToStaging(?,?,?,?,?,?,?)}";
                try (CallableStatement callableStatement = connection.prepareCall(callProcedure)) {
                    callableStatement.setString(1, date);
                    callableStatement.setString(2, region);
                    callableStatement.setString(3, lottery_code);
                    callableStatement.setString(4, province);
                    callableStatement.setString(5, prize);
                    callableStatement.setString(6, number);
                    callableStatement.setString(7, date);
                    // Thực hiện gọi stored procedure
                    callableStatement.execute();
                }
            }
            System.out.println("Extract successfully!");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            SendEmailError.sendErrorEmail("READING FILE", "Error while reading file data: " + e.getMessage());
        }
    }


    public static Optional<File> findLatestExcelFile(String folderPath) throws IOException {
        Path folder = Paths.get(folderPath);
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return Optional.empty();
        }

        // Lọc tất cả các tệp Excel trong thư mục và con thư mục
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk
                    .filter(path -> path.toString().toLowerCase().endsWith(".xlsx") || path.toString().toLowerCase().endsWith(".xls"))
                    .map(Path::toFile)
                    .max(Comparator.comparingLong(file -> file.lastModified()));
        }
    }

    public void truncateAndInsertToStaging(Connection connection, DataFileConfig config, String date, LotteryResultsDAO dao) throws IOException, SQLException {
        // 12.1. Insert vào bảng db_control.data_files dòng dữ liệu với status = EXTRACTING
        dao.insertStatus(connection, config.getId(), "EXTRACTING", date);
        // 12.2. Thực hiện TRUNCATE bảng data_warehouse.lottery_results_staging
        try (CallableStatement callableStatement = connection.prepareCall("{CALL truncate_staging_table()}")) {
            callableStatement.execute();
            // 12.3. Tìm kiếm file excel kết quả xổ số mới nhất trong thư mục lưu trữ có location là tham số location của đối tượng DataFileConfig
            Optional<File> latestExcelFile = findLatestExcelFile(config.getLocation());
            // 12.4. Kiểm tra xem có tệp Excel mới nhất được tìm thấy hay không?
            if (latestExcelFile.isPresent()) {
                File excelFile = latestExcelFile.get();
                // 12.5. Insert dữ liệu vào bảng data_staging.lottery_results_staging
                extractToStaging(excelFile.getAbsolutePath(), connection);
                // 12.6. Insert vào bảng db_control.data_files dòng dữ liệu với status = EXTRACTED
                dao.insertStatus(connection, config.getId(), "EXTRACTED", date);
            } else {
                System.out.println("Không tìm thấy tệp Excel trong thư mục.");
                // 12.7. Insert vào bảng db_control.data_files dòng dữ liệu với status = ERROR
                dao.insertStatus(connection, config.getId(), "ERROR", date);
                // 12.8. Gửi mail thông báo lỗi
                SendEmailError.sendErrorEmail("FILE NOT FOUND", "Error while finding excel file.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 25. Insert vào bảng db_control.data_files dòng dữ liệu với status = ERROR
            dao.insertStatus(connection, config.getId(), "ERROR", date);
            // 26. Gửi mail báo lỗi
            SendEmailError.sendErrorEmail("EXTRACTING", "Error while extracting data: " + e.getMessage());
            // 24. Đóng kết nối db_control
            connection.close();
            throw new RuntimeException(e);
        }
    }

    // transform data
    public void transformData(int idConfig, Connection connection, String date, LotteryResultsDAO dao) throws IOException, SQLException {
        // 13.1. insert status "TRANSFORMING"
        dao.insertStatus(connection, idConfig, "TRANSFORMING", date);

        // thực hiện stored procedure TransformData
        try (CallableStatement callableStatement = connection.prepareCall("{CALL TransformData()}")) {
            callableStatement.execute();

            // 13.18. insert status "TRANSFORMED"
            dao.insertStatus(connection, idConfig, "TRANSFORMED", date);
            System.out.println("Transform successfully!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            // 25. insert status "ERROR"
            dao.insertStatus(connection, idConfig, "ERROR", date);
            // 26. gửi email báo lỗi
            SendEmailError.sendErrorEmail("TRANSFORMING", "Error while transforming data: " + e.getMessage());
            // 24. đóng kết nối
            connection.close();
            throw new RuntimeException(e);
        }
    }

    // load to warehouse
    public void loadToWH(int idConfig, Connection connection, String date, LotteryResultsDAO dao) throws IOException, SQLException {
        // 14.1. insert status "WLOADING"
        dao.insertStatus(connection, idConfig, "WLOADING", date);

        // thực hiện stored procedure LoadDataToWH
        try (CallableStatement callableStatement = connection.prepareCall("{CALL LoadDataToWH()}")) {
            callableStatement.execute();

            // 14.3 insert status "WLOADED"
            dao.insertStatus(connection, idConfig, "WLOADED", date);
            System.out.println("Load to warehouse successfully!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            // 25. insert status "ERROR"
            dao.insertStatus(connection, idConfig, "ERROR", date);
            // 26. gửi email báo lỗi
            SendEmailError.sendErrorEmail("WLOADING", "Error while loading data to warehouse: " + e.getMessage());
            // 24. đóng kết nối
            connection.close();
            throw new RuntimeException(e);
        }
    }

    // aggregate data
    public void aggregateLottery(int idConfig, Connection connection, String date, LotteryResultsDAO dao) throws IOException, SQLException {
        // 15.1. insert data_files với status = AGGREGATING
        dao.insertStatus(connection, idConfig, "AGGREGATING", date);
        //15.2. truncate bảng lottery_results
        try (CallableStatement callableStatement = connection.prepareCall("{CALL AggregateTable()}")) {
            // Thực hiện stored procedure
            // 15.4. insert vào bảng lottery_results trong data_warehouse
            callableStatement.execute();
            // 15.5. insert data_files với status = AGGREGATED
            dao.insertStatus(connection, idConfig, "AGGREGATED", date);
            System.out.println("Aggregate successfully!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            // 25. insert data_files với status = ERROR
            dao.insertStatus(connection, idConfig, "ERROR", date);
            // 26. gửi email báo lỗi
            SendEmailError.sendErrorEmail("AGGREGATING", "Error while aggregating data: " + e.getMessage());
            // 24. đóng kết nối
            connection.close();
            throw new RuntimeException(e);
        }
    }

    // load to mart
    public void loadToMart(int idConfig, Connection connection, String date, LotteryResultsDAO dao) throws IOException, SQLException {
        // 16.1 insert data_files với status = MLOADING
        dao.insertStatus(connection, idConfig, "MLOADING", date);

        try (CallableStatement callableStatement = connection.prepareCall("{CALL LoadToDM()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();
            // 16.11 insert data_files với status = MLOADED
            dao.insertStatus(connection, idConfig, "MLOADED", date);
            // 16.12 insert data_files với status = FINISHED
            dao.insertStatus(connection, idConfig, "FINISHED", date);
            System.out.println("Load to mart successfully!");
            System.out.println("Finished!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            // 25. insert data_files với status = ERROR
            dao.insertStatus(connection, idConfig, "ERROR", date);
            // 26. gửi email báo lỗi
            SendEmailError.sendErrorEmail("MLOADING", "Error while loading data to mart: " + e.getMessage());
            // 24. đóng kết nối
            connection.close();
            throw new RuntimeException(e);
        }
    }

}






























































































































































































