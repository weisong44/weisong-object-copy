package com.weisong.common.vodo.converter;

import java.util.Collection;
import java.util.Map;

import org.springframework.util.Assert;

public class HandlerSingleElementCompatibleType extends AbstractAttrHandler {

    @Override
    boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        
        Assert.notNull(ctx.doFieldValue);
        
        if(ctx.voField.getType().isAssignableFrom(ctx.doFieldValue.getClass()) == false ||
           ctx.doFieldValue instanceof Collection ||
           ctx.doFieldValue instanceof Map) {
            return false;
        }

        debugAttrAction(ctx.vo.getClass(), ctx.voField, "set directly");
        ctx.voField.set(ctx.vo, ctx.doFieldValue);
        return true;
    }

    @Override
    boolean voToDo(VoToDoContext ctx) throws Exception {
        
        if(ctx.converterVoToDo.hasVocoField(ctx.voField)) {
            return false;
        }
        
        Assert.notNull(ctx.voFieldValue);
        
        if(ctx.doField.getType().isAssignableFrom(ctx.voFieldValue.getClass()) == false ||
           ctx.voFieldValue instanceof Collection ||
           ctx.voFieldValue instanceof Map) {
            return false;
        }
        
        debugAttrAction(ctx.o.getClass(), ctx.doField, "set directly");
        ctx.doField.set(ctx.o, ctx.voFieldValue);
        return true;
    }

}
