package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.BankAccount;
import edu.hei.school.agricultural.entity.CashAccount;
import edu.hei.school.agricultural.entity.MobileBankingAccount;
import edu.hei.school.agricultural.entity.Transaction;
import edu.hei.school.agricultural.exception.InternalServerErrorException;
import edu.hei.school.agricultural.mapper.FinancialAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FinancialAccountRepository {
    private final Connection connection;
    private final FinancialAccountMapper financialAccountMapper;

    public List<BankAccount> getBankAccountsByCollectivityId(String collectivityId) {
        List<BankAccount> bankAccounts = new ArrayList<BankAccount>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, holder_name, bank_name, bank_code, branch_code, account_number, key from "bank_account" where collectivity_id = ?
                """)) {
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                BankAccount bankAccount = financialAccountMapper.mapBankFromResultSet(resultSet);
                List<Transaction> transactionList = getTransactionsByFinancialAccountId(bankAccount.getId());
                bankAccount.addTransactions(transactionList);
                bankAccounts.add(bankAccount);
            }
            return bankAccounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<MobileBankingAccount> getMobileBankingAccountsByCollectivityId(String collectivityId) {
        List<MobileBankingAccount> mobileBankingAccounts = new ArrayList<MobileBankingAccount>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id, holder_name, service, mobile_number from "mobile_banking_account" where collectivity_id = ?
                """)) {
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MobileBankingAccount mobileBankingAccount = financialAccountMapper.mapMobileBankingFromResultSet(resultSet);
                List<Transaction> transactionList = getTransactionsByFinancialAccountId(mobileBankingAccount.getId());
                mobileBankingAccount.addTransactions(transactionList);
                mobileBankingAccounts.add(mobileBankingAccount);
            }
            return mobileBankingAccounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CashAccount getCashAccountByCollectivityId(String collectivityId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                select id from "cash_account" where collectivity_id = ?
                """)) {
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                CashAccount cashAccount = financialAccountMapper.mapCashFromResultSet(resultSet);
                List<Transaction> transactionList = getTransactionsByFinancialAccountId(cashAccount.getId());
                cashAccount.addTransactions(transactionList);
                return cashAccount;
            }
            throw new InternalServerErrorException("Unable to retrieve cash account for collectivity.id= " + collectivityId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Transaction> getTransactionsByFinancialAccountId(String financialAccountId) {
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        select id, amount, creation_date, transaction_type from "transaction" where financial_account_id = ?
                        """
        )) {
            preparedStatement.setString(1, financialAccountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Transaction transaction = financialAccountMapper.mapTransactionFromResultSet(resultSet);

                transactions.add(transaction);
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
