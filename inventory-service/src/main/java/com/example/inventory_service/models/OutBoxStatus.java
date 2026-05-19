package com.example.inventory_service.models;

public enum OutBoxStatus {

    PENDING,
    PROCESSED,
    FAILED,
    DEAD_LETTER
}
