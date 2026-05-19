package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.LoginMember;
import roomescape.domain.Member;
import roomescape.dto.MemberCreateRequest;
import roomescape.dto.MemberResponse;
import roomescape.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse join(@Valid @RequestBody MemberCreateRequest request) {
        Member member = memberService.join(
                request.name(),
                request.email(),
                request.password()
        );

        return MemberResponse.from(member);
    }

    @GetMapping("/members/me")
    public LoginMember me(@AuthenticationPrincipal LoginMember loginMember) {
        return loginMember;
    }
}
