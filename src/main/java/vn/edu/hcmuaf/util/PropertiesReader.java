package vn.edu.hcmuaf.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesReader {
    public static Properties readPropertiesFile() {
        Properties properties = new Properties();
        String path = Paths.get("").toAbsolutePath() + "\\config.properties";
        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            SendEmailError.sendErrorEmail("Read file properties", e.getMessage()+ ". Note: the file name must be \"config.properties\" and be in the same directory as the jar file");
            return null;
        }
    }

}

