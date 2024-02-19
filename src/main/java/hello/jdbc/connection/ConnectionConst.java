package hello.jdbc.connection;

/**
 * 데이터 베이스 접속에 필요한 기본정보(상수로 구성)
 * 생성을 하지 못하도록 abstract 클래스로 구성
 */
public abstract class ConnectionConst {
    public static final String URL = "jdbc:h2:tcp://localhost/~/desktop/db파일/jdbc";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}
