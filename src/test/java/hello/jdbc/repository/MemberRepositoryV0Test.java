package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member("memberV0", 10000);
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);    // @Data 가 toString() 을 적절히 오버라이딩 -> 실제 데이터 확인 가능
        assertThat(findMember).isEqualTo(member);   // @Data 는 모든 필드를 사용하여 equals() 오버라이딩 -> 다른 객체여도 true 가능
    }
}