package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.weisong.common.value.BaseValueObject;
import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.VoOnly;

@Getter @Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "jsonType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value=DummySocialFriendVo.class, name="social-friend")
  , @JsonSubTypes.Type(value=DummyCloseFriendVo.class, name="close-friend")
})   
@BindToClass("com.weisong.common.vodo.DummyFriendBase")
abstract public class DummyFriendBaseVo<HOBBY extends HobbyVo> extends BaseValueObject {

    @VoOnly 
    private String jsonType;
    @VoOnly @BindToField("friendOfDummyName")
    private String name;
    private String friendOfDummyName;
    private HOBBY hobby;

}
