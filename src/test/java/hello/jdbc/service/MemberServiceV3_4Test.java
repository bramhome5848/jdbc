package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - DataSource, transactionManager 자동 등록

 * 스프링 부트이 자동 리소스 등록
 - 기존에는 개발자가 데이터소스와 트랜잭션 매니저를 개발자가 직접 스프링 빈으로 등록해서 사용 -> 스프링 부트에서 자동화 됨

 * 데이터 소스 - 자동 등록
 - 스프링 부트는 데이터소스(dataSource) 를 스프링 빈에 자동으로 등록(자동으로 등록된 빈 이름 : dataSource)
 - 개발자가 직접 데이터소스를 빈으로 등록하면 스프링 부트는 데이터 소스를 자동으로 등록하지 않음
 - 스프링 부트는 application.properties 에 있는 속성을 사용해서 DataSource 를 생성후 스프링 빈에 등록함
 - 스프링 부트가 기본으로 생성하는 데이터소스는 커넥션 풀을 제공하는 HikariDataSource -> 커넥션풀 관련 설정도 application.properties 에서 가능

 * 트랜잭션 매니저 - 자동 등록
 - 스프링 부트는 적절한 트랜잭션 매니저(PlatformTransactionManager) 를 자동으로 스프링 빈에 등록(자동으로 등록도니 빈 이름 : transactionManager)
 - 개발자가 직접 트랜잭션 매니저를 빈으로 등록하면 스프링 부트는 트랜잭션 매니저를 자동으로 등록하지 않음
 - 어떤 트랜잭션 매니저를 선택할지는 등록된 라이브러리를 보고 판단
 -> JDBC 인 경우 - DataSourceTransactionManager, JPA 인 경우 - JpaTransactionManager
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    MemberRepositoryV3 memberRepository;

    @Autowired
    MemberServiceV3_3 memberService;

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    /**
     - 데이터소스와 트랜잭션 매니저를 스프링 빈으로 등록하는 코드가 생략
     -> 스프링 부트가 application.properties 에 지정된 속성을 참고하여 데이터 소스와 트랜잭션 매니저를 자동으로 생성
     - 코드와 같이 생성자를 통해 스프링 부트가 만들어준 데이터 소스 빈을 주입받을 수도 있음
     */
    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @Test
    void AopCheck() {
        log.info("memberService class = {}", memberService.getClass());         //프록시(CGLIB) 적용된 것을 확인
        log.info("memberRepository class = {}", memberRepository.getClass());   //AOP 적용하지 않았기 때문에 프록시 적용X
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
        memberRepository.findById(memberEx.getMemberId());

        //memberA의 돈이 rollback 되어야함
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}