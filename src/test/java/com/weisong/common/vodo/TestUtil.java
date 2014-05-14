package com.weisong.common.vodo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import com.weisong.common.data.BaseDataObject;
import com.weisong.common.value.BaseValueObject;

public class TestUtil {
    
    static public <T> T[] toArray(Set<T> set, T[] array) {
        Assert.assertEquals(set.size(), array.length);
        int index = 0;
        for(Iterator<T> i = set.iterator(); i.hasNext();) {
            array[index++] = i.next();
        }
        Arrays.sort(array, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                if(o1 == null)
                    return o2 == null ? 1 : -1;
                if(o1 instanceof BaseDataObject) {
                    return ((BaseDataObject)o1).getId().compareTo(((BaseDataObject)o2).getId());
                }
                else if(o1 instanceof BaseValueObject) {
                    return ((BaseValueObject)o1).getId().compareTo(((BaseValueObject)o2).getId());
                }
                else {
                    throw new RuntimeException("Not supported");
                }
            }
        });
        return array;
    }
    
    static public void prettyPrint(Object o) {
//        System.out.println(o.getClass().getSimpleName() + " = " + JsonUtil.toJsonString(o));
    }

}
