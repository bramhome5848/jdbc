package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 * driveManager() 와 dataSourceDriveManager() 차이점
 1. 파라미터 차이
 - DriverManger 는 커넥션을 획득할 때 마다 URL, USERNAME, PASSWORD 같은 파라미터를 계속 전달
 - DataSource 를 사용하는 방삭은 처음 객체 생성시에 필요한 파라미터를 넘겨두고, 커넥션을 획득할 때는 단순이 getConnection 만 호출

 2. 설정과 사용 분리
 - 설정 : DataSource 생성시 초기화 속성(URL, USERNAME, PASSWORD)들이 한 곳에 모여 있어 향후 변경에 더 유연하게 대처 가능
 - 사용 : 설정은 신경쓰지 않고, DataSource 의 getConnection() 만 호출

 * 설정과 사용 분리 설명
 - DataSource 가 만들어지는 시점에 필요한 데이터를 다 넣어두게 되면, DataSource 를 사용하는 곳에서는 getConnection() 만 호출하면 됨
 - 호출하는 곳에서는 URL, USERNAME, PASSWORD 같은 속성들에 의존하지 않고 DataSource 만 주입받아 getConnection() 을 사용
 - 예를 들어 Repository 가 DataSource 만 의존하고 속성은 몰라도 되는 것과 같음
 */
@Slf4j
class ConnectionTest {

    /**
     * 기존에 개발했던 DriveManager 를 통한 커넥션 획득
     */
    @Test
    void driveManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection = {}, class = {}", con1, con1.getClass());
        log.info("connection = {}, class = {}", con2, con2.getClass());
    }

    /**
     * 스프링이 제공하는 Datasource 가 적용된 DriverManager 인 DriveManagerDataSource 사용
     */
    @Test
    void dataSourceDriveManager() throws SQLException {
        // DriveManagerDataSource - 내부에서 DriverManager 를 사용하기 때문에 항상 새로운 커넥션을 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection = {}, class = {}", con1, con1.getClass());
        log.info("connection = {}, class = {}", con2, con2.getClass());
    }
}
