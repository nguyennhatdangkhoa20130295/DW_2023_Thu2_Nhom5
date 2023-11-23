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

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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
        dao.insertDataFileWithNewStatus(connection, config.getId(), "FINISHED");
        System.out.println("Well done!");
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

    public static void main(String[] args) throws IOException {
//        List<String> list = new ArrayList<>();
//        list.add("2023-11-13");
//        list.add("2023-11-14");
//        list.add("2023-11-15");
//        list.add("2023-11-16");
//        list.add("2023-11-17");
//        list.add("2023-11-18");
//        list.add("2023-11-19");
//        list.add("2023-11-20");
//        list.add("2023-11-21");
//        list.add("2023-11-22");
//        ConnectionManager manager = new ConnectionManager();
//        LotteryResultsDAO dao = new LotteryResultsDAO();
//        try (Connection controlConnection = manager.getControlDataSource().getConnection()) {
//            List<DataFileConfig> dataFileConfigs = dao.getDataFileConfigList(controlConnection);
//            Controller controller = new Controller();
//            for (int i = list.size() - 1; i > -1; i--) {
//                for (DataFileConfig config : dataFileConfigs) {
//                    String status = dao.getStatus(controlConnection, config.getId());
//                    try (Connection stagingConnection = manager.getStagingDataSource().getConnection()) {
//                        if (status.equals("ERROR")) {
//                            continue;
//                        } else if (status.equals("FINISHED") || status.equals("CRAWLING")) {
//                            controller.crawlData(controlConnection, list.get(i), config);
//                        } else if (status.equals("EXTRACTING")) {
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}

