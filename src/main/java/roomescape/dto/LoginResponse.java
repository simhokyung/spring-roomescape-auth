package roomescape.dto;

import roomescape.auth.LoginMember;
import roomescape.domain.Member;

public record LoginResponse(
        Long id,
        String name,
        String email
) {

    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }

    public LoginMember toLoginMember() {
        return new LoginMember(id, name, email);
    }
}
