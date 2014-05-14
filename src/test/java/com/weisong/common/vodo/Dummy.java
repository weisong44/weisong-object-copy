package com.weisong.common.vodo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.OrderBy;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.data.BaseDataObject;

@Getter @Setter
public class Dummy extends BaseDataObject {
    
    public enum Size {
        Small,
        Medium,
        Large
    }
    
    private String nullNameValue; // Test null value
    private String name; // Simple copy
    private Size size; // Enum handling
    private byte[] byteArrayString; //byte[] handling
    
    private DummyChild child; // set recursively
    private DummyFriendBase<?> closeFriend; // set recursively, inheretence
    private DummySocialFriend socialFriend; // set recursively, inheretence
    
    @OrderBy("position")
    private List<DummyChild> children = new ArrayList<DummyChild>(10); // set recursively
    private List<DummyFriendBase<?>> friends = new ArrayList<DummyFriendBase<?>>(10); // set recursively
    
    private List<Long> longList  = new ArrayList<Long>(10); // Simple copy
    private List<String> stringList  = new ArrayList<String>(10); // Simple copy
    
    private String ignored;
    
    protected Dummy() {
    }
}
