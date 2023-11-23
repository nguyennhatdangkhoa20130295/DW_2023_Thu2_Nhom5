package vn.edu.hcmuaf.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionManager {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String CONTROL_DB = "control";
    private static final String DATA_STAGING_DB = "data_staging";
    private static final String DATA_WAREHOUSE_DB = "data_warehouse";
    private static final String DATA_MART_DB = "data_mart";
    private static HikariDataSource controlDataSource;
    private static HikariDataSource stagingDataSource;
    private static HikariDataSource warehouseDataSource;
    private static HikariDataSource martDataSource;

    static {
        HikariConfig controlConfig = new HikariConfig();
        controlConfig.setJdbcUrl(DATABASE_URL + CONTROL_DB);
        controlConfig.setUsername(USERNAME);
        controlConfig.setPassword(PASSWORD);
        controlConfig.addDataSourceProperty("cachePrepStmts", "true");
        controlConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        controlConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        controlDataSource = new HikariDataSource(controlConfig);

        HikariConfig stagingConfig = new HikariConfig();
        stagingConfig.setJdbcUrl(DATABASE_URL + DATA_STAGING_DB);
        stagingConfig.setUsername(USERNAME);
        stagingConfig.setPassword(PASSWORD);
        stagingConfig.addDataSourceProperty("cachePrepStmts", "true");
        stagingConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        stagingConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        stagingDataSource = new HikariDataSource(stagingConfig);

        HikariConfig warehouseConfig = new HikariConfig();
        warehouseConfig.setJdbcUrl(DATABASE_URL + DATA_WAREHOUSE_DB);
        warehouseConfig.setUsername(USERNAME);
        warehouseConfig.setPassword(PASSWORD);
        warehouseConfig.addDataSourceProperty("cachePrepStmts", "true");
        warehouseConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        warehouseConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        warehouseDataSource = new HikariDataSource(warehouseConfig);

        HikariConfig martConfig = new HikariConfig();
        martConfig.setJdbcUrl(DATABASE_URL + DATA_MART_DB);
        martConfig.setUsername(USERNAME);
        martConfig.setPassword(PASSWORD);
        martConfig.addDataSourceProperty("cachePrepStmts", "true");
        martConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        martConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        martDataSource = new HikariDataSource(martConfig);
    }

    public ConnectionManager() {
    }

    public static HikariDataSource getControlDataSource() {
        return controlDataSource;
    }

    public static HikariDataSource getStagingDataSource() {
        return stagingDataSource;
    }

    public static HikariDataSource getWarehouseDataSource() {
        return warehouseDataSource;
    }

    public static HikariDataSource getMartDataSource() {
        return martDataSource;
    }
}
