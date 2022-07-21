package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController //(@Controller + @ResponseBody)
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    
    //Entity 직접 반환시 Entity가 갖고있는 모든 getter 반환되어 의도치 않은 정보(ex: Order)도 노출됨
    //의도치 않은 Entity 내의 Getter 는 @JsonIgnore 어노테이션을 멤버변수에 붙여 반환되지 않도록 설정이 가능
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> findMembers = memberService.findMembers();

//        List<MemberDto> collect = new ArrayList<>();
//        for(Member m : findMembers){
//            MemberDto memberDto = new MemberDto(m.getName());
//            collect.add(memberDto);
//        }
        List<MemberDto> collect = findMembers.stream()
                                    .map(m -> new MemberDto(m.getName()))
                                    .collect(Collectors.toList());
        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    //Entity 를 API 의 파라미터로 바로 받을 경우 스펙 변경시 장애를 유발하므로 DTO 를 따로 두어 DTO 를 파라미터로 받아야 한다.
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //Entity 를 파라미터로 바로 받지 않고 별도의 DTO를 둔 케이스
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data //@Getter + @Setter @ToString @RequriedArgsConstructor ... )
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id){
            this.id = id;
        }
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){

        memberService.update(id, request.getName());    //update 에서 Member 를 리턴해도 되지만 영속성이 깨진 Member 를 update 안쪽에서 return 하는 것보다
        Member findMember = memberService.findOne(id);  // query 와 커맨드를 분리하는 방식으로 개발 조회를 별도로 하여 리턴
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }


}
