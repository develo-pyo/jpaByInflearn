package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

    //@JsonIgnore //의도치 않은 Entity 내의 Getter 는 @JsonIgnore 어노테이션을 멤버변수에 붙여 반환되지 않도록 설정이 가능
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
