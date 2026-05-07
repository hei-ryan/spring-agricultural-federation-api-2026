package edu.hei.school.agricultural.api.model;

import java.time.LocalDate;
import java.util.List;

public class CreateCollectivityActivity {

    public String label;
    public ActivityType activityType;
    public List<MemberOccupation> memberOccupationConcerned;
    public MonthlyRecurrenceRule recurrenceRule;
    public LocalDate executiveDate;

    @Override
    public String toString() {
        return "CreateCollectivityActivity{" +
                "label='" + label + '\'' +
                ", activityType=" + activityType +
                ", memberOccupationConcerned=" + memberOccupationConcerned +
                ", recurrenceRule=" + recurrenceRule +
                ", executiveDate=" + executiveDate +
                '}';
    }
}
