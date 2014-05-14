package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

/**
 * Friend of dummy - association
 */
@Getter @Setter
public class DummySocialFriend extends DummyFriendBase<Fishing> {
    private String socialFriendDescription;
}
