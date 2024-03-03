package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

/**
 * 런타임 예외 - 대부분 복구 불가능한 예외
 - 시스템에서 발생하는 예외는 대부분 복구가 불가능한 예외
 - 런타임 예외를 사용할 경우 서비스나 컨트롤러가 복구 불가능한 예외를 신경쓰지도 않아도 됨
 - 복구 불가능한 예외는 일관성 있게 공통으로 처리해야 함

 * 런타임 예외 - 의존 관계에 대한 문제
 - 런타임 예외는 해당 객체가 처리할 수 없는 예외인 경우 무시하며 됨 -> 체크 예외처럼 예외에 강제로 의존하지 않아도 됨

 * 런타임 예외 구현 기술 변경시 파급 효과
 - 중간에 기술이 변경되어도 해당 예외를 사용하지 않는 컨트롤러, 서비스에서는 코드를 변경하지 않아도 됨
 - 구현 기술이 변경된 경우, 공통으로 처리하는 곳에서는 예외에 따른 처리가 필요할 수 있음, 공통 처리하는 한 곳만 변경하면 되기 때문에 변경에 의한 영향은 최소화 됨
 */
@Slf4j
class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(controller::request).isInstanceOf(Exception.class);
    }

    @Test
    void printEx() {
        Controller controller = new Controller();

        try {
            controller.request();
        } catch(Exception e) {
            //e.printStackTrace();
            log.info("ex", e);  //파라미터가 없기 때문에 예외만 파라미터에 전달하면 스택 트레이스를 출력할 수 있음
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    /**
     * ConnectException 대신 RuntimeConnectionException 으로 변환
     -> 런타임 예외익 때문에 서비스, 컨트롤러는 해당 예외들을 처리할 수 없다면 별도의 선언 없이 그냥 두면 됨
     */
    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectionException("연결 실패");
        }
    }

    /**
     * SQLException 을 런타임 에외인 RuntimeSQLException 으로 변환
     -> 런타임 예외익 때문에 서비스, 컨트롤러는 해당 예외들을 처리할 수 없다면 별도의 선언 없이 그냥 두면 됨
     */
    static class Repository {

        /**
         * 예외 전환 -> SQLException 발생시 RuntimeSQLException 으로 전환하여 예외를 던짐
         * 참고
         - 기존 예외를 포함해 주어야 예외 출력시 스택 트레이스에서 기존 예외도 함께 확인 가능
         - 기존 예외를 포함하지 않아 로그를 통해 어떠한 문제가 발생했는지 확인하지 못하는 경우가 있음
         ex) SQLException 누락 -> RuntimeSQLException 부터 확인 가능 -> 실제 DB 연동시 DB 에서 발생한 예외를 확인할 수 없는 심각한 문제 발생
         - 예외를 전환할 경우 꼭! 기존 예외를 포함할 것!!
         */
        public void call() {
            try {
                runSQL();
            } catch(SQLException e) {
                throw new RuntimeSQLException(e);   //기존 예외(e) 포함, 포함하지 않는 경우 기존 발생한 SQLException 은 확인 불가
            }
        }

        private void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }


    static class RuntimeConnectionException extends RuntimeException {
        public RuntimeConnectionException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }

        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
