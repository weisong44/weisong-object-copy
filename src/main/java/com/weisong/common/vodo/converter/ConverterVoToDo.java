package com.weisong.common.vodo.converter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import com.weisong.common.data.DataObject;
import com.weisong.common.data.ModelMetadata;
import com.weisong.common.util.CommonUtil;
import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.value.BaseValueObject;
import com.weisong.common.value.ValueObject;
import com.weisong.common.vodo.ValueObjectHelper;
import com.weisong.common.vodo.VoDoUtil;
import com.weisong.common.vodo.annotation.BindToField;
import com.weisong.common.vodo.annotation.ContentOptimization;
import com.weisong.common.vodo.annotation.VoDoIgnore;
import com.weisong.common.vodo.annotation.VoOnly;

public class ConverterVoToDo extends ConverterBase {

    final private ModelMetadata metadata;
    final private HandlerFetchHiveObjectById handlerFetchHiveObjectById = new HandlerFetchHiveObjectById(); 
    
    public ConverterVoToDo(VoDoUtil vodoUtil, ApplicationContext ctx) {
        super(vodoUtil, ctx);
        metadata = ctx.getBeansOfType(ModelMetadata.class).values().iterator().next();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> toDoList(Collection<?> voList) throws Exception {
        List<T> oList = new LinkedList<T>();
        for (Object vo : voList) {
            oList.add((T) toDo(vo));
        }
        return oList;
    }
    
    public <T> List<T> toDoList(Page<?> voPage) throws Exception {
        return toDoList(voPage.getContent());
    }

    @SuppressWarnings("unchecked")
    public Object toDo(Object vo) throws Exception {

        Assert.notNull(vo);
        Class<?> voClass = vo.getClass();

        // get the DO class
        Class<?> doClass = VoDoUtil.getDoClass(voClass);
        if (doClass == null) {
            return null;
        }

        // Logging stuff
        logger.debug(String.format("%s <= [%s]", doClass.getSimpleName(), voClass.getSimpleName()));

        // Create context
        AbstractAttrHandler.VoToDoContext ctx = new AbstractAttrHandler.VoToDoContext();
        ctx.converterVoToDo = this;
        ctx.metadata = metadata;
        ctx.vo = vo;
        // Testing model to skip UT classes
        if(doClass.getName().contains("model") && vo instanceof ValueObject && ((BaseValueObject) vo).getId() != null) {
            ctx.o =  metadata.getRepository(doClass).findOne(((BaseValueObject) vo).getId());
            ctx.o =  CommonUtil.unboxProxy(ctx.o);
        }
        if(ctx.o == null) {
            ctx.o = ReflectionUtil.newInstance(doClass);
        }
        Assert.notNull(ctx.o);

        Collection<Field> voFieldList = ReflectionUtil.getAllFieldsAsMap(voClass).values();
        for (Field voField : voFieldList) {

            if(VoDoUtil.shouldSkipVoDoConversion(voField, VoDoIgnore.class, VoOnly.class, ContentOptimization.class))
                continue;

            ctx.voField = voField;
            
            // Get the target DO field name
            ctx.doFieldName = voField.getName();
            BindToField bf = voField.getAnnotation(BindToField.class);
            if (bf != null) {
                ctx.doFieldName = bf.value();
            }

            Field doField = null;
            try {
                // Get the source VO field value
                ctx.voFieldValue = ReflectionUtil.getFieldValue(vo, voField.getName());

                // Special handling
                if(ctx.doFieldName.contains(".") && handlerFetchHiveObjectById.voToDo(ctx)) {
                    continue;
                }
                
                // Get the target DO field
                doField = ReflectionUtil.getField(doClass, ctx.doFieldName);
                if (doField == null) {
                    warnAttrAction(doClass, ctx.doFieldName, "not found, skip");
                    continue;
                }

                doField.setAccessible(true);
                
                ctx.doField = doField;
                
                // Try all available handlers
                boolean handled = false;
                for(AbstractAttrHandler h : handlers) {
                    if(h.voToDo(ctx)) {
                        handled = true;
                        break;
                    }
                }
                
                if(handled == false) {
                    throw HandlerNotAvailableException;
                }
            }
            catch (Exception ex) {
            	if(doField != null) {
	                if(DataObject.class.isAssignableFrom(doField.getType())) {
	                    ex.printStackTrace();
	                    throw new RuntimeException(String.format(
	                        "Failed to set field %s.%s, but it is derived from HiveObject. Error: %s",
	                        doClass.getSimpleName(), doField.getName(), ex.getMessage()));
	                }
            	} else {
                    ex.printStackTrace();
                    throw new RuntimeException(String.format("doField is null, Error: %s", ex.getMessage()));
            	}
                warnAttrAction(doClass, doField, "failed to set, " + ex.getMessage());
                handleAttrConversionError(ex);
            }
            finally {
                if (doField != null)
                    doField.setAccessible(false);
            }
        }

        // Invoke helper if available
        @SuppressWarnings("rawtypes")
        ValueObjectHelper helper = getVoHelper(voClass);
        return helper == null ? ctx.o : helper.toDo(vo, ctx.o);
    }

    public boolean isVocoField(Field voField) {
        return voField.getAnnotation(ContentOptimization.class) != null;
    }
    
}
