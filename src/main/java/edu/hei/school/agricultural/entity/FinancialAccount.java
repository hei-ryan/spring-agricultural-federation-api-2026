package edu.hei.school.agricultural.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FinancialAccount {
    protected String id;
    protected List<Transaction> transactions;

    public List<Transaction> addTransactions(List<Transaction> newTransactions) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.addAll(newTransactions);
        return this.transactions;
    }

    public Double getBalanceAt(LocalDate at) {
        return transactions.stream()
                .filter(transaction -> !transaction.getCreationDate().isAfter(at))
                .map(Transaction::getAmount)
                .reduce(0.0, Double::sum);
    }
}
