package edu.hei.school.agricultural.repository;

import edu.hei.school.agricultural.entity.MembershipFee;
import edu.hei.school.agricultural.mapper.MembershipFeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MembershipFeeRepository {
    private final Connection connection;
    private final MembershipFeeMapper membershipFeeMapper;

    public List<MembershipFee> getMembershipFeesByCollectivityId(String collectivityId) {
        List<MembershipFee> membershipFees = new ArrayList<MembershipFee>();
        try (PreparedStatement ps = connection.prepareStatement("""
                select id, label, amount, frequency, status, eligible_from
                from membership_fee where collectivity_id = ?
                """)) {
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MembershipFee membershipFee = membershipFeeMapper.mapFromResultSet(rs);
                membershipFees.add(membershipFee);
            }
            return membershipFees;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
