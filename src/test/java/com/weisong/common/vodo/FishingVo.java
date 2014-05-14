package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.vodo.annotation.BindToClass;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.Fishing")
public class FishingVo extends HobbyVo {
    private String placeToFish;
}
