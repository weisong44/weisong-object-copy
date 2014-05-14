package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import com.weisong.common.util.CommonUtil;
import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.value.ValueObject;
import com.weisong.common.vodo.ValueObjectHelper;
import com.weisong.common.vodo.VoDoUtil;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.ContentOptimization;
import com.weisong.common.vodo.annotation.DoOnly;
import com.weisong.common.vodo.annotation.VoDoIgnore;
import com.weisong.common.vodo.converter.AbstractAttrHandler.DoToVoContext;

public class ConverterDoToVo extends ConverterBase {
    
    public ConverterDoToVo(VoDoUtil vodoUtil, ApplicationContext ctx) {
        super(vodoUtil, ctx);
    }
    
    <VO> ArrayList<VO> toVoList(int curVocoLevel, Class<VO> voClass, Map<Class<?>, Class<?>> voClassMapping, Collection<?> doList)
            throws Exception {
        updateVoClassMapping(voClass, voClassMapping, doList.iterator().next());
        return toVoList(curVocoLevel, voClassMapping, doList);
    }

    public <VO> ArrayList<VO> toVoList(int curVocoLevel, Class<VO> voClass, Collection<?> doList) throws Exception {
        if(doList.isEmpty()) {
            return new ArrayList<VO>();
        }
        Map<Class<?>, Class<?>> map = createVoClassMapping(voClass, doList.iterator().next());
        return toVoList(curVocoLevel, map, doList);
    }
    
    public <VO> Page<VO> toVoPage(int curVocoLevel, Class<VO> voClass, Page<?> doPage) throws Exception {
        ArrayList<VO> voList = toVoList(curVocoLevel, voClass, doPage.getContent());
        
        Pageable pageable = new PageRequest(doPage.getNumber(), doPage.getSize(), doPage.getSort());
        Page<VO> voPage = new PageImpl<VO>(voList, pageable, doPage.getTotalElements());
        
        return voPage;
    }

    public <VO> Page<VO> toVoPage(int curVocoLevel, Map<Class<?>, Class<?>> voClassMapping, Page<?> doPage) throws Exception {
        ArrayList<VO> voList = toVoList(curVocoLevel, voClassMapping, doPage.getContent());

        Pageable pageable = new PageRequest(doPage.getNumber(), doPage.getSize(), doPage.getSort());
        Page<VO> voPage = new PageImpl<VO>(voList, pageable, doPage.getTotalElements());

        return voPage;
    }

    @SuppressWarnings("unchecked")
    public <VO> ArrayList<VO> toVoList(int curVocoLevel, Map<Class<?>, Class<?>> voClassMapping, Collection<?> doList) 
            throws Exception {
        ArrayList<VO> oList = new ArrayList<VO>(doList.size());
        for (Object o : doList) {
            oList.add((VO) toVo(curVocoLevel, voClassMapping, o));
        }
        return oList;
    }

    <VO> VO toVo(int curVocoLevel, Class<VO> voClass, Map<Class<?>, Class<?>> voClassMapping, Object... dos) 
            throws Exception {
        voClassMapping = updateVoClassMapping(voClass, voClassMapping, dos);
        return toVo(curVocoLevel, voClassMapping, dos);
    }
    
    public <VO> VO toVo(int curVocoLevel, Class<VO> voClass, Object... dos) throws Exception {
        Map<Class<?>, Class<?>> map = createVoClassMapping(voClass, dos);
        return toVo(curVocoLevel, map, dos);
    }
    
    @SuppressWarnings({ "unchecked" })
    public <VO> VO toVo(int curVocoLevel, Map<Class<?>, Class<?>> voClassMapping, Object... dos) throws Exception {

        // Unbox all input DO's
        for(int i = 0; i < dos.length; i++) {
            dos[i] = CommonUtil.unboxProxy(dos[i]);
        }
        
        // Determine the VO class
        Class<?> finalDoClass = dos[0].getClass();
        Class<VO> voClass = (Class<VO>) voClassMapping.get(finalDoClass);
        // If the mapping is not provided, try to find default Vo from the Do
        if(voClass == null) {
            voClass = (Class<VO>) VoDoUtil.getDefaultVoClass(finalDoClass);
        }
        Assert.notNull(voClass, "VO class not found in mapping for " + finalDoClass.getName());
        
        for(Object o : dos) {
            Class<?> candidate = voClassMapping.get(o.getClass());
            Assert.isTrue(candidate == null || candidate == voClass);
        }

        // Logging stuff
        StringBuffer sb = new StringBuffer();
        for(Object o1 : dos) {
            sb.append(o1.getClass().getSimpleName() + ", ");
        }
        sb = sb.length() > 0 ? sb.delete(sb.length() - 2, sb.length()) : sb;
        logger.debug(String.format("%s <= [%s]", 
                voClass.getSimpleName(), sb.toString()));
        
        // get the DO class
        Class<?> doClass = VoDoUtil.getDoClass(voClass);
        if (doClass == null) {
            return null;
        }

        // Create context
        AbstractAttrHandler.DoToVoContext ctx = new AbstractAttrHandler.DoToVoContext();
        ctx.converterDoToVo = this;
        ctx.voClassMapping = voClassMapping;
        ctx.vo = voClass.newInstance();
        Assert.notNull(ctx.vo);

        Collection<Field> voFieldList = ReflectionUtil.getAllFieldsAsMap(ctx.vo.getClass()).values();
        for (Field voField : voFieldList) {
            
            if(VoDoUtil.shouldSkipVoDoConversion(voField, VoDoIgnore.class, DoOnly.class, ContentOptimization.class)) {
                continue;
            }

            try {
                // Get the source object from the input array
                Class<?> doFieldOwnerClass = getFieldOwnerClass(doClass, voField);
                Object src = getObjectOfClass(doFieldOwnerClass, dos);
                if (src == null) {
                    warnAttrAction(voClass, voField, "source object not found in input array, skip");
                    continue;
                }

                // Get the source DO field
                String doFieldName = voField.getName();
                BindToField bf = voField.getAnnotation(BindToField.class);
                if (bf != null) {
                    doFieldName = bf.value();
                }

                // Get the source DO field value
                voField.setAccessible(true);
                
                // Will be handled separately
                if(voField.getAnnotation(ContentOptimization.class) != null) {
                    continue;
                }
                                
                FieldWrapper fw = getDoFieldValue(src, doFieldName);
                
                ctx.voField = voField;
                ctx.doField = fw.field;
                ctx.doFieldType = fw.type;
                ctx.doFieldValue = fw.value;

                boolean handled = false;
                for(AbstractAttrHandler h : handlers) {
                    if(h.doToVo(curVocoLevel, ctx)) {
                        handled = true;
                        break;
                    }
                }
                
                if(handled == false)
                    throw HandlerNotAvailableException;
            } 
            catch (Exception ex) {
                if(ValueObject.class.isAssignableFrom(voField.getType())) {
                    throw new RuntimeException(String.format(
                        "Failed to set field %s.%s, but it is derived from ValueObject. Error: %s",
                        voClass.getSimpleName(), voField.getName(), ex.getMessage()));
                }
                warnAttrAction(voClass, voField, "failed to set, " + ex.getMessage());
                handleAttrConversionError(ex);
            }
            finally {
                if (voField != null)
                    voField.setAccessible(false);
            }
        }
        
        // Invoke helper if available
        ValueObjectHelper<?, VO> helper = getVoHelper(voClass);
        VO vo = (VO) ctx.vo;
        return helper == null ? vo : helper.toVo(vo, dos);
    }

    private Class<?> getFieldOwnerClass(Class<?> bindToClass, Field fo) throws Exception {
        BindToField bindToField = fo.getAnnotation(BindToField.class);
        if (bindToField != null && ! bindToField.clazz().isEmpty()) {
            return Thread.currentThread().getContextClassLoader().loadClass(bindToField.clazz());
        }
        return bindToClass;
    }

    private Object getObjectOfClass(Class<?> srcClass, Object... srcObjects) throws Exception {
        for (Object o : srcObjects) {
            if (srcClass.isAssignableFrom(o.getClass())) {
                return o;
            }
        }
        return null;
    }

    final private class FieldWrapper {
        private Field field;
        private Class<?> type;
        private Object value;
    }

    private FieldWrapper getDoFieldValue(Object o, String fullFieldName) throws Exception {

        Assert.notNull(o);
        o = CommonUtil.unboxProxy(o);
        FieldWrapper fw = new FieldWrapper();
        if (fullFieldName.contains(".")) {
            Object v = o;
            String[] fields = fullFieldName.split("\\.");
            for (String fname : fields) {
                fw.field = ReflectionUtil.getField(v.getClass(), fname);
                v = ReflectionUtil.getFieldValue(v, fname);
                if (v == null) {
                    fw.type = fw.field.getType();
                    fw.value = null;
                    return fw;
                }
                else {
                    fw.type = v.getClass();
                    fw.value = v;
                }
            }
            return fw;
        }

        fw.field = ReflectionUtil.getField(o.getClass(), fullFieldName);
        if(fw.field == null) {
            throw new Exception("field not found on " + o.getClass().getSimpleName());
        }
        fw.value = ReflectionUtil.getFieldValue(o, fullFieldName);
        fw.type = (fw.value == null) ?
                fw.field.getType()
              : fw.value.getClass();
        return fw;
    }
    
    private <VO> Map<Class<?>, Class<?>> createVoClassMapping(Class<?> voClass, Object... dos) {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        return updateVoClassMapping(voClass, map, dos);
    }
    
    @SuppressWarnings("unchecked")
    private <VO> Map<Class<?>, Class<?>> updateVoClassMapping(Class<VO> voClass, Map<Class<?>, Class<?>> map, Object... dos) {
        if(Modifier.isAbstract(voClass.getModifiers())) {
            voClass = (Class<VO>) VoDoUtil.getDefaultVoClass(dos[0].getClass());
        }
        for(Object o : dos) {
            if(map.containsKey(o.getClass()) == false) {
                map.put(o.getClass(), voClass);
            }
        }
        return map;
    }

    public boolean isInVocoMode(int curVocoLevel, DoToVoContext ctx) {
        return curVocoLevel >= ctx.converterDoToVo.voDoUtil.getPerThreadSettings().getVocoLevel();
    }
    
}
