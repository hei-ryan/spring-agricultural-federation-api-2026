package edu.hei.school.agricultural.entity;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Transaction {
    private String id;
    private TransactionType type;
    private Double amount;
    private LocalDate creationDate;
}
