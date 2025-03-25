package com.datamaveric.anomalydetector.controller;


import com.datamaveric.anomalydetector.model.AnomalyResult;
import com.datamaveric.anomalydetector.model.Transaction;
import com.datamaveric.anomalydetector.service.AnomalyDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReconciliationController {

    private final AnomalyDetectionService anomalyDetectionService;

    @Autowired
    public ReconciliationController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @PostMapping("/api/reconcile")
    public AnomalyResult reconcileTransaction(@RequestBody Transaction transaction) {
        return anomalyDetectionService.detectAnomaly(transaction);
    }
}
