package vn.edu.hcmuaf.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vn.edu.hcmuaf.dao.LotteryResultsDAO;
import vn.edu.hcmuaf.db.ConnectionManager;
import vn.edu.hcmuaf.entity.DataFileConfig;
import vn.edu.hcmuaf.entity.LotteryResults;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public class Controller {
    public void crawlData(Connection connection, String date, DataFileConfig config) throws IOException {
        LotteryResultsDAO dao = new LotteryResultsDAO();
        dao.insertDataFileWithNewStatus(connection, config.getId(), "CRAWLING");
        String dateObj = formatDate(date, "dd-MM-yyyy");
        String dateCheck = formatDate(date, "dd/MM/yyyy");
        String url = config.getSource_path() + dateObj + ".html";
        System.out.println(url);
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
        dao.insertDataFileWithNewStatus(connection, config.getId(), "CRAWLED");
        System.out.println("Crawl successfully!");
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
                System.out.println("Đã thêm file " + excelFilePath + " thành công.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getLatestExcelFile(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));

            if (files != null && files.length > 0) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                return files[0];
            }
        }

        return null;
    }

    public void excelToStagingTable(Connection stagingConn, Connection controlConn, DataFileConfig config) throws IOException {
        LotteryResultsDAO dao = new LotteryResultsDAO();
        dao.insertDataFileWithNewStatus(controlConn, config.getId(), "EXTRACTING");
        File latestExcelFile = getLatestExcelFile(config.getLocation());
        if (latestExcelFile != null) {
            try (FileInputStream fis = new FileInputStream(latestExcelFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                Iterator<Row> iterator = workbook.getSheetAt(0).iterator();

                if (iterator.hasNext()) {
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    Row row = iterator.next();
                    String dateValue = row.getCell(0).getStringCellValue();
                    String regionValue = row.getCell(1).getStringCellValue();
                    String lotteryIdValue = row.getCell(2).getStringCellValue();
                    String provinceValue = row.getCell(3).getStringCellValue();
                    String prizeNameValue = row.getCell(4).getStringCellValue();
                    String numberValue = row.getCell(5).getStringCellValue();
                    LotteryResults results = new LotteryResults(dateValue, regionValue, lotteryIdValue, provinceValue, prizeNameValue, numberValue);
                    dao.insertDataToStaging(results, stagingConn);
                }
            }
            dao.insertDataFileWithNewStatus(controlConn, config.getId(), "EXTRACTED");
            System.out.println("Extract successfully!");
        } else {
            System.out.println("No Excel files found in the directory.");
        }
        dao.insertDataFileWithNewStatus(controlConn, config.getId(), "FINISHED");
        System.out.println("Well done!");
    }
}

