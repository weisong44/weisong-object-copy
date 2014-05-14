package com.weisong.common.vodo;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.value.BaseValueObject;
import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.ContentOptimization;
import com.weisong.common.vodo.annotation.DoOnly;
import com.weisong.common.vodo.annotation.VoDoIgnore;
import com.weisong.common.vodo.annotation.VoOnly;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.Dummy")
public class DummyVo extends BaseValueObject {
    //
    // Both direction
    //
    private String name;
    private String nullNameValue; // same attr name, but null value
    private String notExisting; // Non-existing attribute, should see a warning
    private String size;
    private String byteArrayString;

    private DummyChildVo child; // child, recursive
    // Extract child Id
    // TODO wei.song How to handle both directions including List?
    @BindToField("child.id")
    private Long childId;

    // Simple lists, just set them
    private List<Long> longList  = new ArrayList<Long>(10);
    private List<String> stringList  = new ArrayList<String>(10);

    // List, set recursively
    private List<DummyChildVo> children;

    @BindToField("closeFriend.id")
    private Long friendId;
    // Inheritence
    private DummyFriendBaseVo<?> closeFriend;
    private DummySocialFriendVo socialFriend;

    //
    // VO only
    //
    // Extract child name
    // TODO wei.song How to handle both directions?
    @VoOnly @BindToField("child.childName")
    private String childName;

    // mapped attr name, null value - this is VO only since another attr is already mapped
    @VoOnly @BindToField("nullNameValue")
    private String nullNameValue2;

    // Collection, set recursively
    @VoOnly @BindToField("children")
    private List<DummyChildVo> children2;
    // Extract children Id
    // TODO weisong - not natural, revisit!
    @VoOnly @BindToField("children")
    private List<Long> childrenId = new ArrayList<Long>(10);

    // VO only, since there already one up there
    @VoOnly @BindToField("friends")
    private List<Long> friendIds = new ArrayList<Long>(10);
    @VoOnly @BindToField("longList")
    private List<String> differentTypeList  = new ArrayList<String>(10);

    // DO -> VO 2nd object
    @VoOnly @BindToField(clazz="com.weisong.common.vodo.Dummy2", value="id")
    private Long dummy2Id;
    @VoOnly @BindToField(clazz="com.weisong.common.vodo.Dummy2", value="name")
    private String dummy2Name;
    //
    // DO only
    //
    @DoOnly
    private List<DummyFriendBaseVo<?>> friends = new ArrayList<>(10);
    //
    // VoDoIgnore
    //
    @VoDoIgnore
    private String ignored;
    //
    // Content optimization
    //
    @ContentOptimization("children") private ArrayList<Long> vocoChildrenIds;
    @ContentOptimization("child") private Long vocoChildId;

}
