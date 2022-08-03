package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    
    private final OrderRepository orderRepository;

    //LAZY 는 강제 초기화를 시켜주어야 함
    //양방향 관계의 entity 매핑인 경우 field 에 @JsonIgnore 붙이기
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
    }

    //responseType을 DTO 로 변환한 version 2
    //N+1 문제 있음
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                //.map(o -> new OrderDto(o))
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return collect;
    }

    //페치조인 최적화
    //distinct 의 사용
    //* fetch join 사용시 페이징 처리 불가
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            System.out.println("order ref=" + order + " id=" + order.getId());
            //상기 수행시 하기 결과가 나옴. 동일한 orderId 끼린 ref 값까지 동일. (join 으로 인해 카디션 곱만큼 결과가 나옴)
            //distinct 키워드를 통해 해결
            //* query 에 distinct 가 붙어서 수행됨 + entity 중복시 제거
            //order ref=jpabook.jpashop.domain.Order@7689b944 id=4
            //order ref=jpabook.jpashop.domain.Order@7689b944 id=4
            //order ref=jpabook.jpashop.domain.Order@5075a4c2 id=11
            //order ref=jpabook.jpashop.domain.Order@5075a4c2 id=11
        }

        List<OrderDto> collect = orders.stream()
                //.map(o -> new OrderDto(o))
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return collect;
    }


    @Getter
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;    //VO 는 API response 로 그대로 노출시켜도 무방하다
        //private List<OrderItem> orderItems; //Entity 를 API response 로 그대로 노출시키면 안되므로 DTO로 바꿔준다.
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            order.getOrderItems().stream().forEach(o->o.getItem().getName());   //entity 초기화
            //orderItems = order.getOrderItems(); //orderItems 는 LAZY entity 이므로 NULL 이 나옴.
                                                //초기화 필요
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());

        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice =orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }


}
