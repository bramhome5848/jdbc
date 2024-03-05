package hello.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스프링이 제공하는 예외 변환기
 - 스프링은 데이터베이스에서 발생하는 (DB 마다 다른)오류 코드를 스프링이 정의한 예외로 자동으로 변환해주는 변환기를 제공
 */
@Slf4j
class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    /**
     * SQL ErrorCode 직접 확인 방법
     - 직접 예외를 확인하고 하나하나 스프링이 만들어준 예외로 변환하는 것은 현실성이 떨어짐 -> DB 마다 다른 오류코드에 대한 대응을 할 수 없음
     */
    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammar";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch(SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);  //org.springframework.jdc.support.sql-error-codes.xml
            int errorCode = e.getErrorCode();
            log.info("errorCode = {}", errorCode);
            log.info("error", e);   //org.h2.jdbc.JdbcSQLSyntaxErrorException
        }
    }

    /**
     * translate("select", sql, e)
     - 읽을 수 있는 설명, 실행한 sql, 발생된 SQLException
     - 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환
     - 예제에서는 SQL 문법이 잘못되었기 때문에 BadSqlGrammarException 을 반환
     - 눈에 보이는 최상위 타입은 DataAccessException 이지만 실제로 BadSqlGrammarException 가 반환됨

     * org.springframework.jdc.support.sql-error-codes.xml
     -> 스프링 SQL 예외 변환기는 SQL ErrorCode 를 이 파일에 대입해서 어떤 스프링 데이터 접근 예외로 전환할지 결정함
     */
    @Test
    void exceptionTranslator() {
        String sql = "select bad grammar";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch(SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);

            SQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            DataAccessException resultEx = exTranslator.translate("select", sql, e);
            log.info("resultEx", resultEx); //org.springframework.jdbc.BadSqlGrammarException

            assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
