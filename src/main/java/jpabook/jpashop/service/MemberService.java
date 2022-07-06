package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //읽기전용엔 readOnly 속성 true로 주어 성능에 도움이 되도록
//@AllArgsConstructor         //멤버변수에 대해 생성자를 자동 생성
@RequiredArgsConstructor    //final 키워드가 붙은 멤버변수만 생성자를 자동 생성
public class MemberService {

    //@Autowired 사용은 지양
    private final MemberRepository memberRepository;

    // 주입은 스프링컨테이너 로드시 최초 주입되고 추후에 변동될 가능성이 없으므로 생성자 주입 지향.
    // 생성자 사용 지향(객체 주입을 강제하여 null ex 방지) --> Lombok의 @RequiredArgsConstructor 사용으로 대체 가능
//    @Autowired
//    public MemberService(MemberRepository memberRepository){
//        this.memberRepository = memberRepository;
//    }

    /**
    * 회원 가입
    */
    @Transactional  //메소드에 붙은 Transactional 이 class에 붙은 Transactional 오버라이딩하므로 우선적용됨
    public Long join(Member member){
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    //member.getName이 유니크하다고 가정..
    private void validateDuplicateMember(Member member){
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원");
        }
    }

    //회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }


}
