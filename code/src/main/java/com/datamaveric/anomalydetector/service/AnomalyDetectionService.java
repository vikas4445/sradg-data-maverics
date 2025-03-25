package com.datamaveric.anomalydetector.service;


import com.datamaveric.anomalydetector.model.AnomalyResult;
import com.datamaveric.anomalydetector.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.anomaly.IsolationForest;

@Service
@Slf4j
public class AnomalyDetectionService {
    private final ChatModel chatModel;
    private final HistoricalDataService historicalDataService;
    private IsolationForest isolationForest;

    @Autowired
    public AnomalyDetectionService(ChatModel chatModel, HistoricalDataService historicalDataService) {
        this.chatModel = chatModel;
        this.historicalDataService = historicalDataService;
        trainModel(); // Train on startup
    }

    private void trainModel() {
        // Train Isolation Forest with historical data for all transactions
        double[][] trainingData = historicalDataService.getHistoricalTransactions()
                .stream()
                .map(t -> new double[]{t.getAmount()})
                .toArray(double[][]::new);

        if (trainingData.length > 0) {
            isolationForest = new IsolationForest(100, 256); // 100 trees, max samples 256
            isolationForest.fit(trainingData);
        }
    }

    public AnomalyResult detectAnomaly(Transaction currentTransaction) {
        double historicalAvg = historicalDataService.getHistoricalAverage(
                currentTransaction.getAccountId(),
                currentTransaction.getType()
        );
        double stdDev = historicalDataService.getStandardDeviation(
                currentTransaction.getAccountId(),
                currentTransaction.getType()
        );
        log.info("historicalAvg: {} stdDev: {}", historicalAvg, stdDev);
        // Simple statistical anomaly detection (2 standard deviations)
        double zScore = Math.abs(currentTransaction.getAmount() - historicalAvg) / stdDev;
        boolean isStatisticalAnomaly = zScore > 2.0;
        log.info("zScore: {} isStatisticalAnomaly: {}", zScore, isStatisticalAnomaly);
        // Machine Learning approach
        boolean isMLAnomaly = false;
        double mlScore = 0.0;
        if (isolationForest != null) {
            double[][] testData = new double[][]{{currentTransaction.getAmount()}};
            double[] scores = isolationForest.score(testData);
            mlScore = scores[0]; // Score > 0.5 typically indicates anomaly
            isMLAnomaly = mlScore > 0.5;
        }

        // Combine results (anomaly if either method flags it)
        boolean isAnomaly = isStatisticalAnomaly || isMLAnomaly;
        double combinedConfidence = Math.max(zScore / 4.0, mlScore); // Take higher confidence
        log.info("zScore: {} isAnomaly: {}", zScore, isAnomaly);
        String insight = generateInsight(currentTransaction, historicalAvg, stdDev, zScore, mlScore);

        return new AnomalyResult(
            currentTransaction,
            isAnomaly,
            insight,
            Math.min(combinedConfidence, 1.0) // Cap at 1.0
        );
    }

    private String generateInsight(Transaction transaction, double historicalAvg,
                                 double stdDev, double zScore, double mlScore) {
        String prompt = String.format(
            "Given a transaction of $%.2f for account %s (type: %s), with historical average $%.2f " +
            "and standard deviation $%.2f (z-score: %.2f), and machine learning anomaly score %.2f, " +
            "provide a concise insight about potential reasons for this anomaly and suggested actions.",
            transaction.getAmount(), transaction.getAccountId(), transaction.getType(),
            historicalAvg, stdDev, zScore, mlScore
        );

        return chatModel.call(new Prompt(prompt)).getResult().getOutput().getContent();
    }
}