package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 - 트랜잭션 사용시 반복되는 코드 제거

 * 문제점
 - 이곳은 서비스 로직인데 비즈니스 로직뿐만 아니라 트랜잭션을 처리하는 기술 로직이 함께 포함되어 있음
 - 애플리케이션을 구성하는 로직을 핵심 기능과 부가 기능으로 구분했을 때, 비즈니스 로직은 핵심 기능, 트랜잭션은 부가 기능
 - 비즈니스 로직과 트랜잭션 처리를 한 곳에 모아두면 두 관심사를 하나의 클래스에서 처리하게 됨 -> 유지보수가 어려움
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;   //사용을 위해서는 transactionManager 가 필요, 생성자에서 주입 받으면서 생성
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    /**
     * 트랜잭션 템플릿 사용
     - 기존 트랜잭션을 시작하고 커밋, 롤백하는 코드가 모두 제거됨
     - 비즈니스 로직이 정상 수행되면 커밋 수행됨, 언체크 예외 발생시 롤백(체크 예외는 커밋됨)
     - bizLogic() 메서드 호출시 SQLException 체크 예외를 넘겨줌 -> 람다에서는 체크 예외를 밖으로 던질 수 없기 때문에 언체크 예외로 바꾸어 던짐
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        txTemplate.executeWithoutResult((status) -> {   //status -> TransactionStatus
            try {
                bigLogic(fromId, toId, money);  //비즈니스 로직 수행
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
