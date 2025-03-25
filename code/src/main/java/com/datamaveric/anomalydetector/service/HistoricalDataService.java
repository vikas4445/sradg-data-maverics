package com.datamaveric.anomalydetector.service;

import com.datamaveric.anomalydetector.model.Transaction;
import com.datamaveric.anomalydetector.repository.TransactionRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoricalDataService implements CommandLineRunner {

    private final TransactionRepository transactionRepository;

    @Autowired
    public HistoricalDataService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadDataFromCsv("src/main/resources/historical_transactions.csv");
    }

    private void loadDataFromCsv(String filePath) throws Exception {
        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                Transaction transaction = new Transaction(
                        record.get("transactionId"),
                        Double.parseDouble(record.get("amount")),
                        LocalDateTime.parse(record.get("timestamp")),
                        record.get("accountId"),
                        record.get("type")
                );
                transactionRepository.save(transaction);
            }
        }
    }

    public double getHistoricalAverage(String accountId, String type) {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .filter(t -> t.getAccountId().equals(accountId) && t.getType().equals(type))
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0.0);
    }

    public double getStandardDeviation(String accountId, String type) {
        List<Transaction> transactions = transactionRepository.findAll();
        double mean = getHistoricalAverage(accountId, type);
        double variance = transactions.stream()
                .filter(t -> t.getAccountId().equals(accountId) && t.getType().equals(type))
                .mapToDouble(t -> Math.pow(t.getAmount() - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    public List<Transaction> getHistoricalTransactions() {
        return transactionRepository.findAll();
    }

    // New method for ML
    public double[][] getTrainingData(String accountId, String type) {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .filter(t -> t.getAccountId().equals(accountId) && t.getType().equals(type))
                .map(t -> new double[]{t.getAmount()})
                .toArray(double[][]::new);
    }
}
