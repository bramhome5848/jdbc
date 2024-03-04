package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

/**
 * 체크 예외와 인터페이스
 - 기존에 인터페이스를 만들지 않은 이유는 SQLException 때문 -> 구현체에서 체크 예외를 사용하려면 인터페이스도 해당 체크 예외가 선언되어 있어야 함
 - 참고로 구현 클래스의 메서드에서 선언할 수 있는 예외는 부모 타입에서 던진 예외와 같거나 하위 타입이어야 함

 * 특정 기술에 종속되는 인터페이스
 - 구현 기술을 쉽게 변경하기 위해 인터페이스 도입시 SQLException 같은 특정 구현 기술에 종속적인 체크 예외를 사용할 경우 인터페이스도 해당 예외를 포함해야 함
 - 순수한 인터페이스가 아님, jdbc 기술에 종속적인 인터페이스 -> 구현체를 쉽게 변경하기 위해 만든 인터페이스인데 jdbc 기술 변경시 인터페이스 자체를 변경해야 함

 * 런타임(언체크) 예외와 인터페이스
 - 인터페이스에 런타임 예외를 따로 선언하지 않아도 됨 -> 특정 기술에 종속적일 필요가 없음
 */
public interface MemberRepositoryEx {
    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;
}
