package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 * DriveManager 커넥션 요청 흐름
 1. 애플리케이션로직에서 DriverManager 에게 커넥션 요청(DriverManager.getConnection())
 2. DriverManager 는 라이브러리에 등록된 드라이버 목록을 자동으로 인식하고 이 드라이버들에게 순서대로 다음 정보를 넘겨 커넥션을 획득할 수 있는지 확인
 - URL, 이름, 비밀번호
 - 각각의 드라이버는 URL 정보를 체크해서 본인이 처리할 수 있는 요청인지 확인(ex -> jdbc:h2 로 시작하면 h2 데이터베이스 접근을 위한 규칙)
 - h2 드라이버 입장에서 본인이 처리할 수 있으므로 실제 데이터베이스에 연결해서 커넥션을 취득하고 이 커넥션을 클라이언트에 반환(커넥션 구현체가 클라이언트에 반환됨)
 - 반면 URL 이 h2 로 시작했는데 MySQL 드라이버가 먼저 실행되면 드라이버는 본인이 처리할 수 없다는 결과를 반환하고, 다음 드라이버에 순서가 넘어감
 */
@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection = {}, class = {}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
