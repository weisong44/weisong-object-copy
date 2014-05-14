package com.weisong.common.vodo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.data.BaseDataObject;

@Getter @Setter
public class DummyChild extends BaseDataObject {
    // The order column
    private Integer order;
    
    private String childName;
    private List<DummyGrandChild> children = new ArrayList<DummyGrandChild>(10);
    private List<Long> childrenId = new ArrayList<Long>(10);
    
    private Set<DummyGrandChild> childrenSet = new HashSet<DummyGrandChild>(10);
}
