package com.weisong.common.vodo.converter;

import java.lang.reflect.Method;

import com.weisong.common.util.ReflectionUtil;

public class HandlerEnum extends AbstractAttrHandler {
    private static final String ENUM_VALUE_OF_METHOD = "valueOf";
    private static final Class<?>[] parameterClass = {String.class};
    
    @Override
    public boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        if (ctx.voField.getType() == String.class && ctx.doFieldType.isEnum()) {
            ctx.voField.set(ctx.vo, ctx.doFieldValue.toString());
            String action = String.format("convert Enum<%s> to String", ctx.doFieldType.getSimpleName());
            debugAttrAction(ctx.vo.getClass(), ctx.voField, action);
            return true;
        }
        return false;
    }

    @Override
    public boolean voToDo(VoToDoContext ctx) throws Exception {
        if (ctx.voField.getType() != String.class || !ctx.doField.getType().isEnum()) {
            return false;
        }

        String action = String.format("convert String to Enum<%s>", ctx.doField.getType().getSimpleName());
        debugAttrAction(ctx.o.getClass(), ctx.doField, action);
        
        Method valueOfMethod = ReflectionUtil.getMethod(ctx.doField.getType(), ENUM_VALUE_OF_METHOD, parameterClass);
        Object value = valueOfMethod.invoke(null, ctx.voFieldValue);

        ctx.doField.set(ctx.o, value);

        return true;
    }
}
