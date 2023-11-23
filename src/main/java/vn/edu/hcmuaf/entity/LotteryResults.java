package vn.edu.hcmuaf.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LotteryResults {
    private String date;
    private String region;
    private String lotteryId;
    private String province;
    private String prize;
    private String number;
}
