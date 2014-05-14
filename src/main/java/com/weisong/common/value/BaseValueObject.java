package com.weisong.common.value;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BaseValueObject implements ValueObject<Long> {
    private Long id;
    private Long createdAt, updatedAt;
}
