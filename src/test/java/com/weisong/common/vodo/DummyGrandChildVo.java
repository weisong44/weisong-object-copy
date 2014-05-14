package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.value.BaseValueObject;
import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.VoOnly;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.DummyGrandChild")
public class DummyGrandChildVo extends BaseValueObject {
    private String grandChildName;
    @VoOnly @BindToField("id")
    private Long longId;
    @VoOnly @BindToField("id")
    private String strId;
}
