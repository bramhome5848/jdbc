package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();  // Statement 를 통해 준비된 SQL 을 커넥션을 통해 실제 데이터베이스에 전달(executeUpdate 은 int 반환)
            return member;
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * 리소스 정리
     - 쿼리 실행후 리소스 정리는 항상 역순으로 수행(Connection 을 획득하고 Connection 을 통해 PreparedStatement 를 만들었기 때문)
     - PreparedStatement 를 먼저 종료, 이후 Connection 종료(ResultSet 은 조회시 사용)
     - 리소스 정리는 반드시 필요! -> 항상 수행 되어야 하므로 finally 구문에 주의해서 작성해야 함
     - 해당 부분을 놓칠 경우 커넥션이 끊어지지 않고 계속 유지되는 문제가 발생할 수 있음 -> 리소스 누수 -> 커넥션 부족으로 장애가 발생할 수 있음
     */
    private void close(Connection con, Statement stmt, ResultSet rs) {
        if(rs != null) {
            try {
                rs.close();
            } catch(SQLException e) {
                log.info("error", e);
            }
        }

        if(stmt != null) {
            try {
                stmt.close();
            } catch(SQLException e) {
                log.info("error", e);
            }
        }

        if(con != null) {
            try {
                con.close();
            } catch(SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
