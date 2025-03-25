package com.datamaveric.anomalydetector.model;

public record AnomalyResult(Transaction transaction, boolean isAnomaly, String insight, double confidenceScore){}
