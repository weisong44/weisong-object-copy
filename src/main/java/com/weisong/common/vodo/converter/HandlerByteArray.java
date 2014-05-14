package com.weisong.common.vodo.converter;

import java.nio.charset.Charset;

public class HandlerByteArray extends AbstractAttrHandler {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Class<?> BYTE_ARRAY_CLASS = (new byte[0]).getClass();
    
    @Override
    public boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {
        if (ctx.voField.getType() == String.class && BYTE_ARRAY_CLASS.equals(ctx.doFieldType)) {
            ctx.voField.set(ctx.vo, new String((byte[])ctx.doFieldValue, UTF8));
            String action = String.format("convert byte[] <%s> to String", ctx.doFieldType.getSimpleName());
            debugAttrAction(ctx.vo.getClass(), ctx.voField, action);
            return true;
        }
        return false;
    }

    @Override
    public boolean voToDo(VoToDoContext ctx) throws Exception {
        if (ctx.voField.getType() == String.class && BYTE_ARRAY_CLASS.equals(ctx.doField.getType())) {
            String action = String.format("convert String to byte[] <%s>", ctx.doField.getType().getSimpleName());
            debugAttrAction(ctx.o.getClass(), ctx.doField, action);
            
            byte[] value = ((String)ctx.voFieldValue).getBytes(UTF8); 
            ctx.doField.set(ctx.o, value);
            return true;
        }
        return false;
    }

}
