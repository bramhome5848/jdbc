package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 - 순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거
 - 스프링이 제공하는 트랜잭션 AOP 적용을 위해서는 @Transactional 어노테이션 추가가 필요
 - @Transactional 은 메서드에 붙여도 되고 클래스에 붙여도 됨
 -> 클래스에서 붙일 경우 - 외부에서 호출가능한 public 메서드가 AOP 적용 대상이 됨
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bigLogic(fromId, toId, money);
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
