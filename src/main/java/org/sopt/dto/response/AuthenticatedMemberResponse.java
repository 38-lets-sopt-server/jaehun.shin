package org.sopt.dto.response;

import org.sopt.domain.User;

public class AuthenticatedMemberResponse {
    private final Long memberId;
    private final String email;

    public AuthenticatedMemberResponse(Long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
    }

    public static AuthenticatedMemberResponse from(User user) {
        return new AuthenticatedMemberResponse(user.getId(), user.getEmail());
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }
}
