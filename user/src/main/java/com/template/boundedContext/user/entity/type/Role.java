package com.template.boundedContext.user.entity.type;

import lombok.Getter;

@Getter
public enum Role {
    USER("회원"),
    ADMIN("관리자");

    private final String label;

    Role(String label) {
        this.label = label;
    }
}
