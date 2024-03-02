package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 체크 예외 기본 이해
 - Exception 과 그 하 위 예외는 모두 컴파일러가 체크하는 체크 예외, 단 RuntimeException 은 예외
 - 체크 예외는 잡아서 처리하거나 또는 밖으로 던지도록 선언해야 함, 그렇지 않은 경우 컴파일 오류가 발생

 * 체크 예외의 장단점
 - 체크 예외는 예외를 잡아서 처리할 수 없을 때, 예외를 밖으로 던지는  [throws 예외]를 필수로 선언해야 함
 - 그렇지 않은 경우 컴파일 오류가 발생

 - 장점 -> 개발자가 실수로 예외를 누락하지 않도록 컴파일러를 통해 문제를 잡아주는 훌륭한 안전 장치
 - 단점 -> 개발자가 모든 체크 예외를 반드시 잡거나 던지도록 처리해야 하기 때문에 번거로움, 신경쓰고 싶지 않은 예외까지 모두 챙겨야 함, 의존관계에 따른 단점 존재
 */
@Slf4j
class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();    //callCatch() 에서 예외를 처리 했기 때문에 호출한 테스트 메서드까지 예외가 올라오지 않음
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        assertThatThrownBy(service::callThrow).isInstanceOf(MyCheckedException.class);  //예외가 호출한 테스트 메서드까지 올라옴
    }

    /**
     * MyCheckedException
     - Exception 을 상속받았기 때문에 체크 예외가 됨
     - RuntimeException 을 상속받을 경우 언체크 예외가 됨(자바 언어에서 문법으로 정한 것!!)
     - 참고 : RuntimeException 은 Exception 을 상속받았지만 언체크 예외임
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * 체크 예외는 예외를 잡아서 처리하거나, 던지거나 둘 중 하나를 필수로 선택해야 함
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         - catch -> 해당 타입과 그 하위 타입을 모두 잡을 수 있음
         - log.info -> 로그를 남길 때 로그의 마지막 인수에 예외 객체를 전달해주면 로그가 해당 예외의 스택 트레이스를 추가로 출력
         */
        public void callCatch() {
            try {
                repository.call();
            } catch(MyCheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         - 체크 예외는 예외를 잡지 않고 밖으로 던지려면 [throws 예외]를 메서드에 필수로 선언해야 함
         - 체크 예외를 잡지 않고 밖으로 던지지 않으면 컴파일 오류 발생
         - 체크 예외를 밖으로 던지는 경우에도 해당 타입과 하위 타입을 모두 던질 수 있음
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }
}
