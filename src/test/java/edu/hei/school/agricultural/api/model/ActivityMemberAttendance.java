package edu.hei.school.agricultural.api.model;

public class ActivityMemberAttendance {

    public String id;
    public MemberDescription memberDescription;
    public AttendanceStatus attendanceStatus;

    @Override
    public String toString() {
        return "ActivityMemberAttendance{" +
                "id='" + id + '\'' +
                ", memberDescription=" + memberDescription +
                ", attendanceStatus=" + attendanceStatus +
                '}';
    }
}
