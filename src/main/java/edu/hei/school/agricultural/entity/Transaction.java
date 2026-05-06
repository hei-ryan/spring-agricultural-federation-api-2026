package edu.hei.school.agricultural.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Transaction {
    private String id;
    private TransactionType type;
    private Double amount;
    private LocalDate creationDate;
    private Member memberDebited;
}
