package hello.jdbc.repository.ex;

/**
 * MyDuplicateKeyException
 - 기존에 사용했던 MyDbException 을 상속받아 계층 형성 -> 데이터베이스 관련 예외 계층을 만듦
 - 데이터 중복이 경우에만 던지는 예외
 - 해당 예외는 직접 만든 에외로, JDBC 나 JPA 같은 특정 기술에 종속적이지 않으므로 예외를 사용하더라도 서비스 계층의 순수성을 유지할 수 있음
 */
public class MyDuplicateKeyException extends MyDbException {

    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
