package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam

 * 비즈니스 로직과 트랜잭션
 - 트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작해야 함, 비즈니스 로직이 잘못되면 해당 로직으로 인해 문제가 되는 부분을 함께 롤백해야 하기 때문
 - 트랜잭션을 위해서는 커넥션이 필요 -> 서비스 계층에서 커넥션을 만들고, 트랜잭션 커밋 이후, 커넥션을 종료해야 함
 - 아플리케이션에서 DB 트랜잭션을 사용하려면 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 함 -> 그래야 같은 DB 세션을 사용할 수 있음
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();  // 데이터 변경시 executeUpdate() 를 사용, 조회시 executeQuery() 사용 -> 결과를 RS 에 담아 반환

            if(rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();  // 데이터 변경시 executeUpdate() 를 사용, 조회시 executeQuery() 사용 -> 결과를 RS 에 담아 반환

            if(rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            //connection 은 여기서 닫지 않음 -> 호출한 service 에서 닫아야 함
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();     // executeUpdate 는 해당 SQL 에 영향을 받은 로우 수를 반환
            log.info("resultSize = {}", resultSize);
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();     // executeUpdate 는 해당 SQL 에 영향을 받은 로우 수를 반환
            log.info("resultSize = {}", resultSize);
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            //connection 은 여기서 닫지 않음 -> 호출한 service 에서 닫아야 함
            JdbcUtils.closeStatement(pstmt);
        }
    }

    /**
     * 스프링은 JDBC 를 편리하게 다룰 수 있는 JdbcUtils 라는 편의 메서드 제공 - 커넥션을 좀 더 편리하게 닫을 수 있음
     */
    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
        //connection 을 생성하여 사용한 경우 -> connection 을 닫음
        //connection pool 에서 꺼내 사용한 경우  -> connection pool 에 connection 반환
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get Connection = {}, class = {}", con, con.getClass());
        return con;
    }
}
