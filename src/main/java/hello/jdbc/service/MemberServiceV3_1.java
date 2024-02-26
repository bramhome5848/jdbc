package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저

 * 트랜잭션 매니저의 전체 동작 흐름
 - 트랜잭션 매니저 - 트랜잭션 시작
 1. 서비스 계층에서 transactionManager.getTransaction() 을 호출하여 트랜잭션 시작
 2. 트랜잭션 시작을 위해서는 데이터베이스 커넥션이 필요, 트랜잭션 매니저는 내부에서 데이터소스를 사용하여 커넥션을 생성
 3. 커넥션을 수동 커밋 모드로 변경하며 실제 데이터베이스 트랜잭션을 시작
 4. 커넥션을 트랜잭션 동기화 매니저에 보관

 - 트랜잭션 매니저 - 로직실행
 5. 트랜잭션 동기화 매니저는 쓰레드 로컬에 커넥션을 보관 -> 멀티 쓰레드 환경에서 안전하게 커넥션을 보관할 수 있음
 6. 서비스는 비스니스 로직을 실행하며 레포지토리의 메서드 호출(커넥션을 파라미터로 전달하지 않음)
 7. 레포지토리 메서드들은 트랜잭션이 시작된 커넥션이 필요
 -> DataSourceUtils.getConnection() 을 사용하여 트랜잭션 동기화 매니저에 보관된 커넥션을 꺼내어 사용 -> 같은 커넥션을 사용하고 트랜잭션도 유지 됨
 8. 획득한 커넥션을 사용하여 SQL 을 데이터베이스에 전달하여 실행

 - 트랜잭션 매니저 - 트랜잭션 종료
 9. 비즈니스 로직이 끝나고 트랜잭션 종료, 트랜잭션은 커밋하거나 롤백하면 종료 됨
 10. 트랜잭션을 종료하려면 동기화된 커넥션이 필요, 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득
 11. 획득한 커넥션을 통해 데이터베이스에 트랜잭션을 커밋하거나 롤백
 12. 전체 리소스 정리
 -> 트랜잭션 동기화 매니저 정리, 쓰레드 로컬은 사용후 꼭 정리해야 함
 -> setAutoCommit(true) 로 되돌림(커넥션 풀 고려)
 -> con.close() 를 호출하여 커넥션 종료, 커넥션 풀 사용시 con.close() 를 호출하면 커넥션 풀에 반환

 * 정리
 - 트랜잭션 추상화 덕분에 서비스 코드는 JDBC 기술에 의존하지 않음
 - 이후 JDBC -> JPA 로 변경시에도 서비스 코드를 그대로 유지 가능
 - 기술 변경시 의존관계 주입만 DataSourceTransactionManager 에서 JpaTransactionManager 로 변경하면 됨
 - 트랜잭션 동기화 매니저 덕분에 커넥션을 파라미터로 넘기지 않아도 됨
 - java.sql.SQLException -> JDBC 기술에 의존, 이후 강의에서 해결!
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    //현재 JDBC 기술을 사용하기 때문에 DataSourceTransactionManager 를 주입 받아야 함
    //JPA 같은 기술로 변경되면 JpaTransactionManger 를 주입 받으면 됨
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());   //트랜잭션 시작

        try {
            bigLogic(fromId, toId, money);          //비즈니스 로직 수행
            transactionManager.commit(status);      //성공시 커밋
        } catch(Exception e) {
            transactionManager.rollback(status);    //실패시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void bigLogic(String fromId, String toId, int money) throws SQLException{
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
