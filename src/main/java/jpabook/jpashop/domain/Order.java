package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    //OneToOne (1:1) 관계에선 논리적으로 주 테이블이라고 생각되는 곳에 FK를 둠(연관관계의 주인을 주테이블로 함)
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)    //ORDINAL : 숫자사용할경우 ENUM 추가시 기존 숫자 뒤로 밀려서 문제발생하므로 STRING 사용
    private OrderStatus status; //주문상태 ORDER, CANCEL

    //연관관계 메서드 : 컨트롤 하는 쪽에 둔다
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    //연관관계 메서드
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    //연관관계 메서드
    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

}