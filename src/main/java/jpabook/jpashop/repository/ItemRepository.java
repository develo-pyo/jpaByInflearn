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
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class , id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }
}
