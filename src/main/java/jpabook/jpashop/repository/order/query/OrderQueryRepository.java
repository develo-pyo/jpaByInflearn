package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** OrderRepository : Entity와 직접적인 관련이 있는 쿼리
 *  OrderQueryRepository : 화면이나 API 에 종속되어있는 쿼리
 * */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    // N+1 문제 발생 케이스
    public List<OrderQueryDto> findOrderQueryDtos(){
        List<OrderQueryDto> result = findOrders(); //query 1번 -> 결과 2개(N개)

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // query 2번(N개)
            o.setOrderItems(orderItems);
        });
        return result;
    }

    //N+1 문제 해결 : 1+1 로 최적화 (쿼리 1회 수행후 id 값 기준으로 IN 조건에 넣어 쿼리 수행)
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();  //쿼리 1회 수행

        List<Long> orderIds = toOrderIds(result);   //in 조건에 넣을 id를 list 로 만들기

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);   //쿼리 1회 수행

        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        //쿼리 1회 수행
        //값에 대한 처리는 메모리에서
        List<OrderItemQueryDto> orderItems = em.createQuery(
                                    "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                            " from OrderItem oi" +
                                            " join oi.item i" +
                                            " where oi.order.id in :orderIds", OrderItemQueryDto.class
                            ).setParameter("orderIds", orderIds)
                            .getResultList();

        // key : orderItemQueryDto.getOrderId()
        // value : List<OrderItemQueryDto>
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                                                            .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class
                ).setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findOrders(){
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderFlatDto> findAllByDto_flat(){
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }

}
