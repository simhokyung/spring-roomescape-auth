package roomescape.domain;

import lombok.Getter;
import roomescape.exception.InvalidInputException;

@Getter
public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;

    public Member(Long id, String name, String email, String password) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("회원 이름은 필수입니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidInputException("이메일은 필수입니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidInputException("비밀번호는 필수입니다.");
        }
    }
}
