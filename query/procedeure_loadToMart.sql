CREATE DEFINER=`root`@`localhost` PROCEDURE `AggregateTable`()
BEGIN
    SET @current_date = CURDATE();
    SET @start_date = DATE_SUB(@current_date, INTERVAL 29 DAY);

TRUNCATE TABLE data_warehouse.lottery_results;

INSERT INTO data_warehouse.lottery_results (id, date, region, lottery_code, province, prize, number)
SELECT
    fact.id,
    date.full_date,
    re.`name`,
    co.`code`,
    pro.`name`,
    pri.`name`,
    fact.number
FROM
    data_warehouse.lottery_results_fact AS fact
        JOIN data_warehouse.dates_dim AS date ON fact.date = date.date_sk
    JOIN data_warehouse.regions_dim AS re ON fact.region = re.region_sk
    JOIN data_warehouse.lottery_codes_dim AS co ON fact.lottery_code_id = co.lottery_code_sk
    JOIN data_warehouse.provinces_dim AS pro ON fact.province = pro.province_sk
    JOIN data_warehouse.prizes_dim AS pri ON fact.prize = pri.prize_sk
WHERE
    date.full_date BETWEEN @start_date AND @current_date;
END