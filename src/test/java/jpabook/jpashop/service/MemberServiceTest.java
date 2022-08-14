package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest //내부적으로 @ExtendWith 어노테이션 가지고 있음. @ExtendWith 는 @RunWith 를 대체
@Transactional  //test 안의 @Transactional 은 default 가 rollback true
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    //@Autowired
    //EntityManager em;

    @Test
    //@Rollback(false) DB에 INSERT 하고 싶은 경우
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);  //영속성 컨텍스트에만 올림 (실제 DB에 insert는 하지 않음)
                                                    //실제 DB에 insert 하려면 EntityManager .flush(); 로 영속성 컨텍스트를 DB에 반영해야 함
                                                    //insert 한 후 Rollback 하게됨 (Rollback(true) 인 경우(default))

        //then
        //em.flush(); //영속성 컨텍스트를 DB에 반영
        //Assertions.assertEquals(member, memberRepository.findOne(savedId));
    }

//    junit4 에선 @Test(expected ) 속성 사용 가능
//    @Test(expected = IllegalStateException.class)
//    public void 중복_회원_예외_junit4방식() throws Exception {
//        //given
//        Member member1 = new Member();
//        member1.setName("kim");
//
//        Member member2 = new Member();
//        member2.setName("kim");
//
//        //when
//        memberService.join(member1);
//        memberService.join(member2);    //예외(IllegalStateException) 발생 예상
//
//        //then
//        Assertions.fail("예외 발생");
//    }

    //Junit5 에선 assertThrows 사용
    @Test
    public void 중복_회원_예외_junit5방식() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);

        //then
        Assertions.assertThrows(IllegalStateException.class, () -> {
            //when
            memberService.join(member2);    //예외 발생 예상
        });
    }
}