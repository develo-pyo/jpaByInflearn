package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;

    //@Embeddable 을 받는 쪽은 @Embedded
    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)    //ORDINAL : 숫자사용할경우 ENUM 추가시 기존 숫자 뒤로 밀려서 문제발생하므로 STRING 사용
    private DeliveryStatus status;  //READY, COMP

}
