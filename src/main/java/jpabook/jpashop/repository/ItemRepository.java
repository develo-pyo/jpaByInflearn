package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item){
        if(item.getId() == null){   //저장하기 전까지 Id 가 없으므로 Id 가 null 인 경우 신규 데이터
            em.persist(item);
        } else {
            em.merge(item);
            //Item item = em.merge(item); 에서 Item item 과 em.merge(item) 의 item 은 다른 객체.
            //좌측의 item 은 JPA 에서 관리되는 영속 엔티티
            //우측의 item 은 준영속 엔티티(JPA에서 관리되지 않아 setter 사용 후 transaction 에 의해 자동 commit 불가)
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class , id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }
}
