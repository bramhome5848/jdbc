package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 언체크 예외 기본 이해
 - RuntimeException 과 그 하위 예외는 언체크 예외로 분류
 - 말 그대로 컴파일러가 예외를 체크하지 않음
 - 언체크 예외는 체크 예외와 기본적으로 동일, 차이가 있다면 예외를 던지는 throws 를 선언하지 않고, 생략할 수 있음, 이 경우 자동으로 예외를 던짐

 * 체크 예외 vs 언체크 예외
 - 체크 예외 -> 예외를 잡아서 처리하지 않으면 항상 throws 에 던지는 예외를 선언해야 함
 - 언체크 예외 -> 예외를 찹아서 처리하지 않아도 throws 를 생략할 수 있음

 * 언체크 예외의 장단점
 - 언체크 예외는 예외를 잡아서 처리할 수 없을 때, 예외를 밖으로 던지는 [throws 예외]를 생략할 수 있음

 - 장점 -> 신경쓰고 싶지 않은 언체크 예외 무시 가능, 처리할 수 없는 예외를 밖으로 던질 경우에 [throws 예외] 생략 가능, 의존관계 참조 하지 않아도 됨
 - 단점 -> 언체크 예외는 개발자가 실수로 예외를 누락할 수 있음, 반면 체크 예외는 컴파일러를 통해 예외 누락을 잡아줌
 */
@Slf4j
class UncheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(service::callThrow).isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException 을 상속받은 예외는 언체크 예외가 됨
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * 언체크 예외는 예외를 잡거나 던지지 않아도 됨, 예외를 잡지 않으면 자동으로 밖으로 던짐
     */
    static class Service {
        Repository repository = new Repository();

        public void callCatch() {
            try {
                repository.call();
            } catch(MyUncheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 언체크 예외를 잡지 않아도 됨, 자연스럽게 상위(호출한 곳)로 넘어감
         * 체크 예외와 다르게 throws 예외 선언을 하지 않아도 됨

         * 언체크 예외를 밖으로 던지는 코드 - 선언
         - 언체크 예외도 [throws 예외]를 선언해도 됨, 생략 가능
         - 언체크 예외는 주로 생략하지만, 중요한 경우 선언해두면 해당 코드 호출시 개발자가 해당 예외가 발생한다는 점을 편리하게 인지함
         -> 컴파일 시점에 막을 수 있는 것은 아니고, IDE 를 통해 인지할 수 있는 정도
         */
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
