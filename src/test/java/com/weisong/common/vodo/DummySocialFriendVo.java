package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.DummySocialFriend")
public class DummySocialFriendVo extends DummyFriendBaseVo<FishingVo> {
    @BindToField("socialFriendDescription")
    private String description;
}
