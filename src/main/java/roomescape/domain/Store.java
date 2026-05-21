package roomescape.domain;

import lombok.Getter;
import roomescape.exception.InvalidInputException;

@Getter
public class Store {

    private final Long id;
    private final String name;

    public Store(Long id, String name) {
        validateName(name);

        this.id = id;
        this.name = name;
    }

    public Store(String name) {
        this(null, name);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("매장 이름은 필수입니다.");
        }
    }
}
