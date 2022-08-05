package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    //파라미터가 고정인 경우
//    public List<Order> findAll(OrderSearch orderSearch){
//        return em.createQuery("select o from Order o join o.member m" +
//                                    " where o.status = :status " +
//                                    " and m.name like :name",Order.class)
//                                .setParameter("status", orderSearch.getOrderStatus())
//                                .setParameter("name", orderSearch.getMemberName())
//                                //.setFirstResult(1)    //페이징에서 사용
//                                .setMaxResults(1000)    //최대 1000건
//                                .getResultList();
//    }


    /**
     * 1. 문자열 처리를 통한 query. 실무에서 사용하지 않음. 실무에선 QueryDSL 사용 혹은 Mybatis 사용
     * */
    public List<Order> findAllByString(OrderSearch orderSearch){
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        if(orderSearch.getOrderStatus() != null){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name = :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null){
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * 2. JPA Criteria. 실무에서 사용하지 않음. 실무에선 QueryDSL 사용 혹은 Mybatis 사용
     * */
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();
        
        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

        return query.getResultList();
    }
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d", Order.class
                        ).getResultList();
    }

    //ToOne 관계는 페이징처리를 해도 상관없다. (row 수 변화가 없기 때문에)
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o"+
                " join fetch o.member m"+
                " join fetch o.delivery d", Order.class
                ).setFirstResult(offset)
                 .setMaxResults(limit)
                 .getResultList();
    }

    // 카디션의 곱만큼 결과가 나오므로 member 1 : orderItem  4개인 경우 결과는 4줄이 나옴
    // 1:N (toMany)fetch join 사용시 페이징 처리 불가
    // (1:n에서 n을 기준으로 row가 생성되며 메모리에서 페이징 처리를 하기때문에 OOM 발생 등 위험)
    // 컬렉션 둘 이상에 페치 조인 사용하면 안됨. 1 * n * m 으로 row가 너무 많아짐
    public List<Order> findAllWithItem() {
        // distinct 키워드를 붙여 Order entity 의 중복을 제거
        // (query 수행시 distinct 키워드가 붙음 + 조회결과에서 JPA Order entity (Root entity) 중복 제거)
        return em.createQuery("select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }
}
