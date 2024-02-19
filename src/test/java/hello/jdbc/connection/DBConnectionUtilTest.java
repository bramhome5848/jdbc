package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DBConnectionUtilTest {

    @Test
    void connection() {
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
        //org.h2.jdbc.JdbcConnection -> h2 데이터베이스 드라이버가 제공하는 전용 커넥션으로 JDBC 표준 인터페이스(java.sql.Connection)를 구현
    }
}