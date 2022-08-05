package jpabook.jpashop.domain.item;

import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

//@BatchSize(size=100)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   //JOINED, SINGLE_TABLE:한곳에여러개넣기(default), TABLE_PER_CLASS
@DiscriminatorColumn(name="dtype")
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //비지니스 로직
    //domain 주도 설계로 domain에 관련 비지니스 로직을 개발
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity){
        int realStock = this.stockQuantity - quantity;
        if(realStock < 0 ){
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = realStock;
    }

}
