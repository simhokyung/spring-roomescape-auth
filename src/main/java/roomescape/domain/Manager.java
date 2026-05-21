package roomescape.domain;

import lombok.Getter;
import roomescape.exception.InvalidInputException;

@Getter
public class Manager {

    private final Long id;
    private final Long memberId;
    private final Long storeId;

    public Manager(Long id, Long memberId, Long storeId) {
        validateMemberId(memberId);
        validateStoreId(storeId);

        this.id = id;
        this.memberId = memberId;
        this.storeId = storeId;
    }

    public Manager(Long memberId, Long storeId) {
        this(null, memberId, storeId);
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidInputException("매니저 회원은 필수입니다.");
        }
    }

    private void validateStoreId(Long storeId) {
        if (storeId == null) {
            throw new InvalidInputException("매니저 매장은 필수입니다.");
        }
    }
}
