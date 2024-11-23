package org.system.citronix.enums;

import java.time.LocalDate;
import java.time.Month;

public enum SeasonEnum {
    WINTER,
    SPRING,
    SUMMER,
    AUTUMN;

    public static SeasonEnum fromDate(LocalDate date) {
        Month month = date.getMonth();
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> WINTER;
            case MARCH, APRIL, MAY -> SPRING;
            case JUNE, JULY, AUGUST -> SUMMER;
            case SEPTEMBER, OCTOBER, NOVEMBER -> AUTUMN;
        };
    }
}