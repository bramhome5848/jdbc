package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 2가지 문제
 1. 복구 불가능한 예외
 - 일부 복구가능한 예외가 있지만 대부분의 예외는 복구가 불가능
 ex) SQLException -> 데이터베이스에 문제가 있는 경우 발생(SQL 문법문제, 데이터베이스 자체 문제, 데이터베이스 서버 중단 등등)

 - 대부분의 서비스나 컨트롤러의 경우 위와 같은 문제를 해겨랗기 어려움
 -> 이런 문제들은 일관성 있게 공통적 처리가 필요
 -> 오류 로그를 남기고 개발자가 해당 오류를 빠르게 인지하는 것이 필요
 -> 서블릿 필터, 스프링 인터셉터, 스프링의 ControllerAdvice 를 사용하면 깔끔하게 공통으로 해결 가능

 2. 의존 관계에 대한 문제
 - 체크 예외이기 때문에 컨트롤러나 서비스 입장에서는 본인이 처리할 수 없어도 어쩔 수 없이 throws 를 통해 던지는 예외를 선언해야 함
 - 서비스, 컨트롤러에서 SQLException 에 의존하는 것이 문제가 됨
 - 향후 JDBC 기술이 아닌 다른 기술로 변경시 SQLException 에 의존하는 모든 코드는 수정이 필요함
 -> 결과적으로 OCP, DI 를 통해 클라이언트 코드의 변경 없이 대상 구현체를 변경할 수 있다는 장점이 체크 예외 때문에 발목을 잡히게 됨

 * throws Exception
 - Exception 은 하위 타입인 SQLException, ConnectException 도 함께 던짐
 -> 코드는 깔끔하지만 최상위 타입으로 모든 예외를 다 밖으로 던지는 문제가 발생
 -> 다른 체크 예외를 체크할 수 있는 기능이 무효화 되고, 중요한 체크 예외를 다 놓치게 됨
 -> 중간에 중요한 체크 예외가 발생해도 컴파일러는 Exception 을 던지기 때문에 문법에 마잗고 판단하여 컴파일 오류를 발생하지 않음
 -> Exception 을 던지는 것은 체크 예외를 의도한대로 사용할 수 없음, 꼭 필요한 경우가 아니라면 좋은 방법은 아님
 */
@Slf4j
class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(controller::request).isInstanceOf(Exception.class);
    }

    /**
     * 체크 예외를 처리하지 못해 밖으로 던지기 위해 throws SQLException, ConnectException 선언
     */
    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    /**
     * 체크 예외를 처리하지 못해 밖으로 던지기 위해 throws SQLException, ConnectException 선언
     */
    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex");
        }
    }
}
