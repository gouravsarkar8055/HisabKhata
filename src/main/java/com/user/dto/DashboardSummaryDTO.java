package com.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSummaryDTO {

    private Long todaySales;
    private Long todayPurchase;
    private Long paymentIn;
    private Long paymentOut;
    private Long expenses;
    private Long todayProfit;
    private Long cashFlow;

}