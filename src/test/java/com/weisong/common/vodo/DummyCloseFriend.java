package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

/**
 * Friend of dummy - association
 */
@Getter @Setter
public class DummyCloseFriend extends DummyFriendBase<Hunting> {
    private String closeFriendDescription;
}
