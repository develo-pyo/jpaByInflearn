package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Inheritance
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long Category;

    private String name;

    // 다대다 관계는 중간 매핑 테이블(다대일 <> 테이블(테이블명:category_item) <> 일대다)이 필요
    // category_id fk, item_id fk 칼럼을 가지고있는 중간테이블
    // 실무에서 사용하지않음
    @ManyToMany
    @JoinTable(name = "category_item", //테이블명
            joinColumns = @JoinColumn(name = "category_id"),    //fk 칼럼
            inverseJoinColumns = @JoinColumn(name = "item_id")) //fk 칼럼
    private List<Item> items = new ArrayList<>();

    //셀프 참조(순환참조)
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    //연관관계 메서드
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }

}
