package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor    //final 또는 @NotNull 필드의 생성자를 자동 생성
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false);           //트랜잭션 시작, default 자동 커밋을 수동 커밋으로 변경 -> 트랜잭션 시작한다고 보통 표험함
            bigLogic(con, fromId, toId, money); //트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 분리
            con.commit();                       //성공시 커밋
        } catch(Exception e) {
            con.rollback();                     //실패시 롤백
            throw new IllegalStateException(e);
        } finally {                             //커넥션을 모두 사용하고 안전하게 종료
            release(con);
        }
    }

    private void bigLogic(Connection con, String fromId, String toId, int money) throws SQLException{
        Member fromMember = memberRepository.findById(con, fromId); //기존에 만들었던 커넥션 전달
        Member toMember = memberRepository.findById(con, toId);     //기존에 만들었던 커넥션 전달

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);   //이체중 예외확인을 위해 추가
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection con) {
        if(con != null) {
            try {
                con.setAutoCommit(true);    //커넥션 풀 고려
                con.close();                //커넥션 풀 사용시 커넥션 종료가 아닌 풀에 반납
            } catch(Exception e) {
                log.info("error", e);
            }
        }
    }
}
