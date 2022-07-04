package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

//@Embeddable 은 타 테이블에 삽입되는 필드
//확장 가능성이 있는 필드들의 모음
//immutable 불가변하게 설계, setter 제거
//protected 사용 권장
//JPA 가 리플렉션 프록시 등을 생성하기 위해 위와 같이 설정
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    public Address(String city, String street, String zipcode){
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

}
