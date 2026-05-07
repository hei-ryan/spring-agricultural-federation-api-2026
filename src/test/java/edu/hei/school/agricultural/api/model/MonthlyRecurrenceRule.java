package edu.hei.school.agricultural.api.model;

public class MonthlyRecurrenceRule {

    public Integer weekOrdinal;
    public WeekDay dayOfWeek;

    @Override
    public String toString() {
        return "MonthlyRecurrenceRule{" +
                "weekOrdinal=" + weekOrdinal +
                ", dayOfWeek=" + dayOfWeek +
                '}';
    }
}
