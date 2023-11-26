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
    public void crawlData(Connection connection, String date, DataFileConfig config,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, config.getId(), "CRAWLING", date);
        String dateObj = formatDate(date, "dd-MM-yyyy");
        String dateCheck = formatDate(date, "dd/MM/yyyy");
        String url = config.getSource_path() + dateObj + ".html";
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
        dao.insertStatus(connection, config.getId(), "CRAWLED", date);
        System.out.print("Crawl successfully! ");
        writeDataToExcel(results, config.getLocation(), date);
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

    public void writeDataToExcel(List<LotteryResults> lotteryResults, String location, String date) {
        String excelFilePath = location + "\\crawl_lottery_results_" + date + ".xlsx";

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
            try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                workbook.write(fos);
                fos.close();
                System.out.println(excelFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void extractToStaging(Connection connection, DataFileConfig config, String date,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, config.getId(), "EXTRACTING", date);
        try (CallableStatement callableStatement = connection.prepareCall("{CALL truncate_staging_table()}")) {
            callableStatement.execute();
            Optional<File> latestExcelFile = findLatestExcelFile(config.getLocation());
            if (latestExcelFile.isPresent()) {
                File excelFile = latestExcelFile.get();
//                System.out.println("Tệp Excel mới nhất là: " + excelFile.getAbsolutePath());
                extractToStaging(excelFile.getAbsolutePath(), connection);
                dao.insertStatus(connection, config.getId(), "EXTRACTED", date);
            } else {
                System.out.println("Không tìm thấy tệp Excel trong thư mục.");
                dao.insertStatus(connection, config.getId(), "ERROR", date);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void transformData(int idConfig, Connection connection, String date,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, idConfig, "TRANSFORMING", date);

        try (CallableStatement callableStatement = connection.prepareCall("{CALL TransformData()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();

            dao.insertStatus(connection, idConfig, "TRANSFORMED", date);
            System.out.println("transform success!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            dao.insertStatus(connection, idConfig, "ERROR", date);
            SendEmailError.sendErrorEmail("TRANSFORMING", "Error while transforming data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void loadToWH(int idConfig, Connection connection, String date,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, idConfig, "WLOADING", date);

        try (CallableStatement callableStatement = connection.prepareCall("{CALL LoadDataToWH()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();

            dao.insertStatus(connection, idConfig, "WLOADED", date);
            System.out.println("load to warehouse success!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            dao.insertStatus(connection, idConfig, "ERROR", date);
            SendEmailError.sendErrorEmail("WLOADING", "Error while loading data to warehouse: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void aggregateLottery(int idConfig, Connection connection,String date,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, idConfig, "AGGREGATING",date);

        try (CallableStatement callableStatement = connection.prepareCall("{CALL AggregateTable()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();

            dao.insertStatus(connection, idConfig, "AGGREGATED",date);
            System.out.println("aggregate success!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            dao.insertStatus(connection, idConfig, "ERROR",date);
            SendEmailError.sendErrorEmail("AGGREGATING", "Error while aggregating data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public void loadToMart(int idConfig, Connection connection,String date,LotteryResultsDAO dao) throws IOException {
        dao.insertStatus(connection, idConfig, "MLOADING",date);

        try (CallableStatement callableStatement = connection.prepareCall("{CALL LoadToDM()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();

            dao.insertStatus(connection, idConfig, "MLOADED",date);
            dao.insertStatus(connection, idConfig, "FINISHED",date);
            System.out.println("load to mart success!");
            System.out.println("finished!");
        } catch (SQLException e) {
            // Xử lý lỗi khi thực hiện stored procedure
            e.printStackTrace();
            dao.insertStatus(connection, idConfig, "ERROR",date);
            SendEmailError.sendErrorEmail("MLOADING", "Error while loading data to mart: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

