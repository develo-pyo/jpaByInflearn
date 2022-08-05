package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //파라미터가 없는 생성자 자동생성(protected 로 생성)
                                                    //https://cobbybb.tistory.com/14
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // = new proxy member(); 를 넣어둠 (org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterCeptor)

    //@BatchSize(size=100)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //OneToOne (1:1) 관계에선 논리적으로 주 테이블이라고 생각되는 곳에 FK를 둠(연관관계의 주인을 주테이블로 함)
    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
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

    //생성 메서드
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //비지니스 로직
    /** 주문 취소시 주문 상태 변경 및 재고 원복 */
    public void cancel(){
        if(delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소 불가");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : this.orderItems) {
            orderItem.cancel();
        }
    }

    //조회 로직
    /** 전체 주문 가격 조회 */
    public int getTotalPrice(){
//        int totalPrice = 0;
//        for (OrderItem orderItem : this.orderItems) {
//            totalPrice += orderItem.getTotalPrice();
//        }
//        return totalPrice;
        return this.orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
