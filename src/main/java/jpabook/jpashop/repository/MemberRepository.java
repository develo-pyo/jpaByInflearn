package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository //@Repository 어노테이션은 @Component 가 붙어있어 Component-scan 의 대상이 됨
@RequiredArgsConstructor //final 키워드가 붙은 멤버변수만 생성자를 자동 생성
public class MemberRepository {

    // JPA dependency 사용시 Lombok 의 @RequiredArgsConstructor 로 대체 가능
    //@PersistenceContext
    //private EntityManager em;

    private final EntityManager em;

    //EntityManager 대신 Factory 를 직접 주입받아 처리할 경우 아래와 같이
//    @PersistenceUnit
//    private EntityManagerFactory emf;

    public Long save(Member member){
        em.persist(member);
        return member.getId();
    }

    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    //JPQL 은 테이블을 대상으로 쿼리하는게 아닌 Entity 객체를 대상으로 조회
    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }


}
