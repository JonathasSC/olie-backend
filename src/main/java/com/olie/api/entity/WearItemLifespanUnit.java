package com.olie.api.entity;

public enum WearItemLifespanUnit {
    DAYS(1),
    WEEKS(7),
    MONTHS(30),
    YEARS(365);

    private final int days;

    WearItemLifespanUnit(int days) {
        this.days = days;
    }

    public long toDays(int amount) {
        return (long) amount * days;
    }
}
