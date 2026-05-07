package edu.hei.school.agricultural.api.model;

public class CreateActivityMemberAttendance {

    public String memberIdentifier;
    public AttendanceStatus attendanceStatus;

    @Override
    public String toString() {
        return "CreateActivityMemberAttendance{" +
                "memberIdentifier='" + memberIdentifier + '\'' +
                ", attendanceStatus=" + attendanceStatus +
                '}';
    }
}
