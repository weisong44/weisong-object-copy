package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.DummyCloseFriend")
public class DummyCloseFriendVo extends DummyFriendBaseVo<HuntingVo> {
    @BindToField("closeFriendDescription")
    private String description;
}
