package com.weisong.common.vodo;

import lombok.Getter;
import lombok.Setter;

import com.weisong.common.data.BaseDataObject;

@Getter @Setter
public class Dummy2 extends BaseDataObject {
    private String nullNameValue; // Test null value
    private String name; // Simple copy
}
