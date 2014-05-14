package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.weisong.common.data.ModelMetadata;

abstract public class AbstractAttrHandler {

    final static protected Exception NotImplementedException = new Exception("not implemented");
    
    static class DoToVoContext {
        Map<Class<?>, Class<?>> voClassMapping;
        ConverterDoToVo converterDoToVo;
        Field doField, voField;
        Class<?> doFieldType; // could be traversed type, i.e. child.name
        Object vo, doFieldValue;
    }
    
    static class VoToDoContext {
        ConverterVoToDo converterVoToDo;
        ModelMetadata metadata;
        Field voField, doField;
        Object o, vo, voFieldValue;
        String doFieldName; // Special handling, i.e. "child.id"
    }
    
    abstract boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception;
    abstract boolean voToDo(VoToDoContext ctx) throws Exception;
    
    protected Logger logger = LoggerFactory.getLogger(getClass());
    
    protected void debugAttrAction(Class<?> c, Field f, String action) {
        debugAttrAction(c, f.getName(), action);
    }
    
    protected void debugAttrAction(Class<?> c, String fieldName, String action) {
        logger.debug(String.format("  %s.%s: %s", c.getSimpleName(), fieldName, action));
    }
    
    protected void warnAttrAction(Class<?> c, Field f, String action) {
        warnAttrAction(c, f.getName(), action);
    }
    
    protected void warnAttrAction(Class<?> c, String fieldName, String action) {
        logger.warn(String.format("  %s.%s: %s", c.getSimpleName(), fieldName, action));
    }
    
}
