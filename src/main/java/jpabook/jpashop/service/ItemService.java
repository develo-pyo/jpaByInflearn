package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor    //final 키워드가 붙은 맴버변수에 대해 생성자 자동생성
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    // 하기 코드는 itemRepository entityManager.merge 와 동일한 역할을 한다.
    // merge : 준영속성 상태의 엔티티를 영속 상태로 변경할 때 사용

    // 1.영속성 엔티티 : JPA 에 의해 관리되는 엔티티
    // Item item = itemRepository.findOne(itemId);
    // item.setPrice(1000);
    // itemRepository.save() 불필요
    // =>영속성 엔티티의 변경분은 하이버네이트가 변화를 감지하여 update 및 @Transactional 에 의해 커밋

    // 2.준영속 엔티티 : JPA 에 의해 관리되지 않는 엔티티
    // Item item = new Book();
    // item.setPrice(1000);
    // itemRepository.save() 필요
    // =>준영속 엔티티의 변경분은 하이버네이트에서 감지하지 못하기 때문에 entityManager.merge를 통해 update 및 @Transactional 에 의해 커밋


    @Transactional
    public Item updateItem(Long itemId, String name, int price, int stockQuantity){
        //entity find 를 통해 조회시 영속성 엔티티로 관리되므로 변화에 대해 하이버네이트가 감지하여
        //set으로 속성값만 넣어줘도 변화를 감지하여 sql 을 날려줌 (transactional 로 커밋)
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
        return findItem;
    }


    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
