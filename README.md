## jpaByInflearn  
### jpa 기초 공부  
### https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

**연관관계 매핑과 영속성 컨텍스트**
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

**[ fk 관리 ]**  
연관관계의 주인은 외래키를 관리하는 엔티티. (1:N 에서 N 쪽에 외래키를 갖는다)    
연관관계의 주인만이 데이터베이스 연관 관계와 매핑되고 외래키를 등록/수정/삭제 할 수 있다.   
주인이 아닌 쪽은 읽기만 한다.  
연관관계의 주인이 아닌 곳에 입력된 값은 외래키에 영향을 주지 않음.  
-> line 에 station 추가시 line 의 인스턴스 station 에 setLine() 을 호출하여   
연관관계의 주인인 station 을 통해 연관관계를 맺어줌. 이 때, jvm 에서도 매핑을 해주기 위해 연관관계 편의메소드를 사용

```JAVA
@Entity
public class Line {
    @OneToMany(mappedBy="line")
    List<Station> stations;
    
    //연관관계 편의메소드
    void addStation(Station station) {
        stations.add(station);
        station.setLine(this);
    }
}

@Entity
public class Station {
    @ManyToOne
    Line line;
    
    //연관관계 편의메소드
    void setLine(final Line line){
        this.line = line;
        if(!line.getStations().contains(this)) {
            line.getStations().add(this);
        }
    }
}
```
ex)  
지하철역 Station N : 지하철노선 Line 1 관계일 때
연관관계의 주인은 Station 이며
: 연관관계 편의 메소드

**[save 동작방식]**
- 새로운 데이터라고 인식하는 경우 persist (insert) 수행  
- 기존에 존재하는 데이터라고 인식할 경우 merge (데이터 변경여부 확인하기 위해 select 쿼리 호출 이후 변화 있을 경우 update) 수행
- 
**[언제 new 로 판단하는가?]** 
1) 새로운 객체의 기준은 식별자(@Id) 가 null or 0 일 경우 new 상태로 인식  
  Primitive 타입의 식별자의 경우 new 상태로 인식. (Long 과같은 Wrapper 인 경우 null 을 newState로 인식)
2) @Version 필드가 null 인 경우 new 로 간주 (version property 가 존재하고 다른 값을 가지고 있다면 new 가 아님)
- @Version 이 들어가면 isNew 내부에서 @Id 를 newState 의 기준으로 사용하지 않음.
  @Version은 entity에 lock 을 잡고자 할 때 사용
- 
**[new 상태를 컨트롤 하고싶다면?]**  
1) entity 에서 Persistable interface 를 구현하여 isNew 를 오버라이딩 하고 리턴(boolean) 값을 정의
2) EntityInformation을 커스터마이징  
https://velog.io/@rainmaker007/spring-data-jpa-save-%EB%8F%99%EC%9E%91-%EC%9B%90%EB%A6%AC

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

SINGLE_TABLE 사용시 null 을 포함하게 되며 정규화가 제대로 되지 않을 가능성이 높아  
일반적으로 JOINED 사용

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
=>영속성 엔티티의 변경분은 하이버네이트가 변화를 감지(snapshot을 통해)하여 update 및 @Transactional 에 의해 커밋  

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
  
--------------
**[OSIV 성능 최적화]**  
Open Session In View : 하이버네이트  
Open EntityManager In View : JPA (관례상 OSV 라 함)  
**spring.jpa.open-in-view : true (default)**  
- 스프링부트 서버 기동시 open-in-view 관련하여 warn 로그가 남음  
1. open-in-view 가 켜있는 경우(true인 경우) : Transaction 이 끝나도 DB Connection 반환하지 않음    
- View 랜더링이 끝나야, 혹은 response 가 완전히 끝나야 DB connection 을 반환함  
(Lazy 로딩은 영속성 컨텍스트가 살아있어야 가능하기 때문에 DB connection 을 유지)  
- DB Connection 병목을 유발할 수 있다.  

2. open-in-view 가 꺼져있는 경우(false인 경우) : Transaction 이 끝나면 DB Connection 을 반환하여 영속성 컨텍스트도 끝  
ㄴ 트랜잭션 안에서 지연로딩을 해줘야함  
ㄴ 트랜잭션 밖에서 지연로딩 시도시 LazyInitializationException ("could not initialize proxy - no Session") 발생  
ㄴ fetch join 으로 대체  
ㄴ view template 에서 지연로딩 동작하지 않음  
ㄴ 별도의 @Service 를 두고 @Transactional(readonly = true) 설정을 한 후,   
  지연로딩을 해당 Service 안에서 하는 구조로 개발  

**[OSIV true/false 선택]**  
고객서비스 및 트래픽이 많은 API 는 OSIV 를 끄고, ADMIN 시스템과 같이 트래픽이 크지 않은 시스템은 OSIV 를 켠다  
OSIV 를 끄고, 지연로딩을 Service 안에서 하는 구조로 개발
--------------
**[Spring-Data-JPA]**
- org.springframework.boot:spring-boot-starter-data-jpa   
- Repository interface 에서 extends JPARepository<T, id type> 할 경우 기본적인 CRUD 자동생성
- 인터페이스만 만들어 개발하면 된다. 구현체는 스프링 데이터 JPA가 애플리케이션 실행시점에 주입해준다.
```java
interface  MemberRepository extends JPaRepository<Member, Long> {
} 
```


--------------
entity  
@GeneratedValue 선언시   
id 생성을 데이터베이스에 위임하게되므로 영속화(persist) 시점에 insert 쿼리가 수행된다.  
https://gmlwjd9405.github.io/2019/08/12/primary-key-mapping.html  

--------------
save() 동작방식
* 새로운 객체인 경우 persist 수행
* 새로운 객체가 아니면 merge 수행
- Id 값이 존재하는 객체의 경우 DB에 존재하는 데이터로 간주하여 merge 수행하며 merge 수행시 update 할 항목이 있는지 확인하기 위해    
select가 먼저 수행하게 되어 성능에 문제가 생길 수 있음.


https://kapentaz.github.io/jpa/Spring-Data-JPA%EC%97%90%EC%84%9C-insert-%EC%A0%84%EC%97%90-select-%ED%95%98%EB%8A%94-%EC%9D%B4%EC%9C%A0/#

--------------
**[QueryDSL]**
- www.querydsl.com
- JPQL 을 java builder pattern 포맷으로 지원
- 컴파일시에 오타를 잡을 수 있는 장점
- generated Q file 들은 git .ignore 에서 제외 (빌드시점에만 있으면 되므로 불필요) 






