## jpaByInflearn  
### jpa 기초 공부  

**[JPA 에서의 entity 매핑 : 1:n , n:1, n:m, 1:1]**  

@Entity 테이블에 필수  

1. n:m  
```JAVA
@ManyToMany //다대다 관계에서 사용. 다대다 관계는 중간 매핑 테이블이 필요하며 실무에서 사용하지 않음.
@JoinTable(name = "category_item", //테이블명
           joinColumns = @JoinColumn(name = "category_id"),    //fk 칼럼
           inverseJoinColumns = @JoinColumn(name = "item_id")) //fk 칼럼
```

2. 1:n  
```JAVA
  class Team {
  @OneToMany(mappedBy = "team")
  private List<User> userList = new ArrayList<>();
}
```
  
3. n:1  
```JAVA
class User {
  @ManyToOne
  @JoinColumn(name = "team_id")
  private Team team;
}
```

4. 1:1  
@OneToOne

------------------------------------------------------------------------------------------------------------------------------
**[@Embeddable , @Embedded]**  
- @Embeddable 은 타 테이블에 삽입되는 클래스에 붙여줌 (@Entity 아님)   
- 확장 가능성이 있는 필드들의 모음   
- immutable 불가변하게 설계, setter 제거   
- protected 사용 권장   
- JPA 가 리플렉션 프록시 등을 생성하기 위해 위와 같이 설정   

@Embedded 는 @Embeddable 클래스를 멤버변수로 사용할 때 사용  
```JAVA
@Embeddable
class Address {
}

@Entity
class Member{
    @Embedded
    private Address address;
}
```

------------------------------------------------------------------------------------------------------------------------------
**[Lombok 관련]**  
1. @RequiredArgsConstructor  
final 키워드가 붙은 맴버변수와 @NonNull 키워드가 붙은 필드에 대해 생성자 자동생성  

2. @NoArgsConstructor  
파라미터가 없는 생성자 자동 생성  
* @NoArgsConstructor(access = AccessLevel.PROTECTED)  
 : 파라미터가 없는 protected 접근제한자 생성자 자동생성.  
 : 비어있는(null) 멤버변수가 존재하는 객체 생성 막기 위해  

3. @AllArgsConstructor  
모든 필드를 파라미터로 갖는 생성자 생성  

4. @Builder  
builder 패턴 생성  
* @Builder 패턴은 @AllArgsConstructor 와 함께 사용  
  : @NoArgsConstructor(access = AccessLevel.PROTECTED) 와 함께 사용시 생성자 못찾음.  

 참고 : https://cobbybb.tistory.com/14  
 참고 : https://hyoj.github.io/blog/java/basic/lombok/#noargsconstructor-requiredargsconstructor-allargsconstructor  

--------------
**[n:m 관계 테이블의 설계]**  
1. 설계시 n:m 지양 -> 중간테이블을 두고 1:n n:1 로 풀어서 설계   
2. 동일 트랜잭션 내에선 entity 객체는 동일하다고 판단   
3. getter 는 열어두되 setter 는 꼭 필요할때만 선언   
* setter 를 모두 열어둘 경우 entity cud 에 대한 처리가 어디서 이뤄지는지 추적 어려움   

// h2 설치경로 ~/bin h2.sh 실행 후 서버 띄워야 JPA 관련 에러 발생하지 않음   
// h2 에서 테이블명세보기: show columns from category;   

// 실무에서 @Getter 는 열어두고 @Setter 는 필요시에만 사용  

-------------- 
**[Entity 상속]**   
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   
: super Entity 에 붙여줌   
: JOINED, SINGLE_TABLE:한곳에여러개넣기(default), TABLE_PER_CLASS   
@DiscriminatorColumn(name="dtype")   
: 구분필드  

@DiscriminatorValue("B")  
: 상속하는 자식 Entity 에 붙여줌 

```java
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   //JOINED, SINGLE_TABLE:한곳에여러개넣기(default), TABLE_PER_CLASS
@DiscriminatorColumn(name="dtype") // 구분필드
class Item {
}
```

```java
@DiscriminatorValue("B")
class Book {
}
```
```java
@DiscriminatorValue("A")
class Album {
}
```

--------------
**[FETCH TYPE : 지연로딩과 즉시로딩]**  
fetch = fetchType.EAGER  or LAZY  
EAGER : 연관관계에 있는 데이터를 즉시 로딩 (Order 를 조회했을 경우 @ManyToOne Member 를 같이 가져옴  
    -> n + 1 문제 발생 ( ex: select * from order where order_date between  sysdate and sysdate-1 JPQL 수행 결과가 100건인 경우  
                            Order 의 연관관계인 Member 를 100개(n) + Order쿼리(1) = n+1 문제 발생 )  
LAZY : 연관관계에 있는 데이터를 지연 로딩  
    -> 조회한 테이블만 조회. EAGER를 부분적으로 사용하고 싶은경우(ORDER 조회시 연관관계인 MEMBER를 함께 가져오고 싶은경우), fetch join 사용  
* @ManyToOne @OneToOne (@XToOne) 은 default fetchType 이 EAGER.  
* @OneToMany (@XToMany) 는 default fetchType 이 LAZY.  


--------------
**[영속성 엔티티와 준영속성 엔티티 : merge 사용과 setter 사용을 통한 update]**  

1.영속성 엔티티 : JPA 에 의해 관리되는 엔티티  
Item item = itemRepository.findOne(itemId);  
item.setPrice(1000);  
itemRepository.save() //불필요  
=>영속성 엔티티의 변경분은 하이버네이트가 변화를 감지하여 update 및 @Transactional 에 의해 커밋  

2.준영속 엔티티 : JPA 에 의해 관리되지 않는 엔티티  
Item item = new Book();  
item.setPrice(1000);   
itemRepository.save() //필요  
=>준영속 엔티티의 변경분은 하이버네이트에서 감지하지 못하기 때문에 entityManager.merge를 통해 update 및 @Transactional 에 의해 커밋  

**[병합(merge)의 동작 순서]**  
member merge 호출  
ㄴ> 1차 캐시 엔티티 조회 -> (캐시에 없을 경우)DB 조회  
ㄴ> 병합(setter 와 같이 값 채우기)  
ㄴ> mergeMember 영속상태  
ㄴ> return Member(영속상태의 엔티티반환)  

*** 병합시 주의점 ***   
병합 사용시 모든 속성(필드) 가 변경되므로 null 값이 들어갈 수도 있음   
=> 이때문에 merge 는 실무에서 사용되는 경우가 드물며 영속성 Entity 에 대한 수정으로 처리   
=> 생성자로 객체 생성 후 set 을 통한 속성 값 삽입 및 merge 를 통한 데이터 저장은 준영속성 엔티티에 대한 처리방식으로 비권장   
   JPA find 를 통해 영속성 entity 를 조회한 후 set 을 통한 속성 값 삽입 후 Transactional commit 을 통해 데이터 저장 방식 권장   
   
--------------
**[@Enumerated : Enum 및 Enum 타입]**  
Enum type 은 ORDINAL 과 STRING 두가지.  
ORDINAL : 숫자사용할경우 ENUM 추가시 기존 숫자 뒤로 밀려서 문제발생하므로 STRING 사용  

```java
@Enumerated(EnumType.STRING)  
private OrderStatus status; //주문상태 ORDER, CANCEL  
```

--------------
**[쿼리방식 선택 권장 순서]**  
1.엔티티를 DTO로 변환하는 방법을 선택
2.필요하면 페치 조인으로 성능 최적화
3.그래도 안되면 DTO로 직접 조회하는 방법 사용
4.최후의 방법은 JPA가 제공하는 네이티브SQL이나 스프링JDBCTemplate 사용하여 SQL 직접 작성

--------------
**[1+N 문제 해결하기]**
1. distinct 사용
: unique 한 값만 가져오게 되어 1+N 문제 해결
: paging 처리가 불가 (1:n 의 경우 row 가 n 개 나오므로..)
2. fetch join + @BatchSize(or default_batch_fetch_size) 사용
: ToOne 관계는 fetch join 사용. (row 수가 증가하지 않으므로)
: spring.jpa.properties.hibernate.default_batch_fetch_size : 100 ~ 1000 (최대1000개 제한)
: 내부적으로 IN 조건을 사용하여 ToMany 관계의 Entity 를 한번에 가져옴
(@BatchSize 를 사용하여 개별적으로 적용 가능)


--------------
**[조회 방식 권장 순서]**
1. entity 조회 방식으로 우선 접근
1) fetch join 으로 쿼리 수 최적화 (ToOne 관계 entity 만 fetch join 사용)
2) 컬렉션 최적화
- 페이징 필요 (hibernate.default_batch_fetch_size, @BatchSize) 로 최적화
- 페이징 필요하지 않은 경우 : fetch join 사용 
2. Entity 조회 방식으로 해결이 안될 경우 DTO 조회 방식 사용 
3. DTO 조회 방식으로 해결이 안되면 NativeSQL or Spring JdbcTemplate 사용
* 엔티티 조회 방식은 fetch join 이나 default_batch_fetch_size 등을 사용하여 코드를 거의 수정하지 않고 옵션만 약간 변경해서 다양한 성능 최적화 시도가 가능하나,
* DTO를 직접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 코드 수정량이 많아짐
* Redis 와 같은 캐싱을 사용시 반드시 Entity 대신 DTO를 캐싱해야함


