package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

// Entity를 직접 조회하는 Repository 가 아닌 커스터마이징 한 쿼리의 경우 이와 같이 별도의 Repository 로 분리하여 개발
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    //원하는 칼럼만 select 함
    //성능면에서 v3 에 비해 장점이 있음(미미함), Entity 가 아니므로 영속성 관리가 되지 않는 객체
    //new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
    //traffic 이 과다한 경우 등, 최적화를 해야하는 상황에서 사용
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o " +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
