package com.weisong.common.vodo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.value.BaseValueObject;
import com.weisong.common.vodo.annotation.BindToClass;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.ContentOptimization;
import com.weisong.common.vodo.annotation.VoOnly;

@Getter @Setter
@BindToClass("com.weisong.common.vodo.DummyChild")
public class DummyChildVo extends BaseValueObject {

    @VoOnly @BindToField("childName")
    private String name;
    private String childName;
    
    private List<DummyGrandChildVo> children;
    @ContentOptimization("children") private ArrayList<Long> vocoChildrenIds;
    @VoOnly @BindToField("children")
    private List<Long> childrenId = new ArrayList<Long>(10);

    private Set<DummyGrandChildVo> childrenSet = new HashSet<DummyGrandChildVo>(10);
    @ContentOptimization("childrenSet") private Set<Long> vocoChildrenSetIds;

}
