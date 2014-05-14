package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.util.Collection;

import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.value.ValueObject;

public class HandlerNullValue extends AbstractAttrHandler {

    @Override
    public boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        if(ctx.doFieldValue == null)
            return handle(ctx.vo, ctx.voField);
        return false;
    }

    @Override
    public boolean voToDo(VoToDoContext ctx) throws Exception {
        if(ctx.voFieldValue == null) {
            // Special handling, do not copy null value over to DO
            if("createdAt".equals(ctx.voField.getName())) {
                debugAttrAction(ctx.o.getClass(), ctx.doField, "special handling, noop");
                return true;
            }
            else if(ValueObject.class.isAssignableFrom(ctx.voField.getType()) 
                  || Collection.class.isAssignableFrom(ctx.voField.getType())) {
                Field vocoField = ctx.converterVoToDo.getVocoField(ctx.voField);
                if(vocoField != null && ReflectionUtil.getFieldValue(ctx.vo, vocoField.getName()) != null) {
                    // Handled later
                    return false;
                }
            }
            return handle(ctx.o, ctx.doField);
        }
        return false;
    }

    private boolean handle(Object o, Field f) throws Exception {
        if (ReflectionUtil.isPrimitiveType(f)) {
            throw new RuntimeException("Primitive types not allowed!");
        }
        debugAttrAction(o.getClass(), f, "set to null");
        f.set(o, null);
        return true;
    }
}
