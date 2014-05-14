package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.data.BaseDataObject;

@Getter @Setter
abstract public class DummyFriendBase<HOBBY extends Hobby> extends BaseDataObject {
    private String friendOfDummyName;
    private HOBBY hobby;
}
