package org.example.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Order {
    private Long id;
    private String customer;
    private LocalDate date;
    private boolean statusPayment;
    private String description;
    private BigDecimal totalPrice;
}
