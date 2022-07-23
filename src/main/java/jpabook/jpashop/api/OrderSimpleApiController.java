package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * XToOne
 * Order
 * Order -> Member
 * Order -> Delivery
 * */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    //Entity 직접 리턴하는 예. (지양)
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        //순환참조 발생 (무한Loop)
        //Order -> Member -> Order -> Member -> ...
        //순환참조를 끊기 위해 한쪽에 @JsonIgnore 어노테이션을 붙여 순환참조를 끊어주어야 함
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        // hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true); 설정을 하지 않고
        // 지연로딩의 Member, Delivery 값을 채워주는 방법은 아래와 같이 조회를 해주면 됨.
        for (Order order : all) {
            order.getMember().getName();        //LAZY 강제 초기화
            order.getDelivery().getAddress();   //LAZY 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        // N + 1 문제 발생
        // 1(ORDER 조회) + N(2) (member 조회 2번 + delivery 조회 2번) = ORDER 조회 1회 + Order 하위 Member , delivery 조회 각2회 (Order 결과 2건인 경우)
        // 만약 두 Order의 Member 가 동일한 경우, Member 는 영속성 컨텍스트에서 조회하므로 1회만 조회함
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> all = orders.stream().map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return all;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        // N + 1 문제 발생
        // 1(ORDER 조회) + N(2) (member 조회 2번 + delivery 조회 2번) = ORDER 조회 1회 + Order 하위 Member , delivery 조회 각2회 (Order 결과 2건인 경우)
        // 만약 두 Order의 Member 가 동일한 경우, Member 는 영속성 컨텍스트에서 조회하므로 1회만 조회함
        List<Order> orders = orderRepository.findAllWithMemberDelivery(new OrderSearch());
        List<SimpleOrderDto> all = orders.stream().map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return all;
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

}
