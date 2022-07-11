package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm {

    private Long id;

    //bookForm 또한 Order와 마찬가지로 createForm 을 static 메소드로 만들어주는게 좋은 설계.
    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;


}
