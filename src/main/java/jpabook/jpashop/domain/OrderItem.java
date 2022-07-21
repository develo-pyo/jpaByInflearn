package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문 가격

    private int count;  //주문 수량

    //생성 로직은 생성메서드인 createOrderItem 만 사용하도록 protected 키워드를 사용하여 생성자 사용을 금지한다
    //다른 로직에서 new OrderItem() 으로 인스턴스 생성한 후 .setCount() 와 같이 인스턴스 생성하지 못하도록..
    //@NoArgsConstructor(access = AccessLevel.PROTECTED) 사용시 아래 코드 자동 생성
//    protected OrderItem(){
//    }

    //생성 메서드
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    //비지니스 로직
    public void cancel(){
        getItem().addStock(this.count);
    }

    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
