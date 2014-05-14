package com.weisong.common.vodo.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OrderBy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;

import com.weisong.common.data.BaseDataObject;
import com.weisong.common.data.DataObject;
import com.weisong.common.util.CommonUtil;
import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.value.BaseValueObject;
import com.weisong.common.value.ValueObject;

@SuppressWarnings({ "rawtypes", "unchecked"})
abstract public class AbstractCollectionHandler<T extends Collection> extends AbstractAttrHandler {

    private Class<T> type = (Class<T>) ReflectionUtil.getClassGenericType(getClass());
    
    abstract protected T newInstance();
    
    @Override
    boolean doToVo(int curVocoLevel, DoToVoContext ctx) throws Exception {

        Assert.notNull(ctx.doFieldValue);

        if(type.isAssignableFrom(ctx.voField.getType()) == false ||  
           type.isAssignableFrom(ctx.doFieldValue.getClass()) == false) {
            return false;
        }
        
        boolean inVocoMode = ctx.converterDoToVo.isInVocoMode(curVocoLevel, ctx);
        Field vocoField = ctx.converterDoToVo.getVocoField(ctx.voField);
        
        T collection = (T) ctx.doFieldValue;
        if(collection.isEmpty()) {
            if(inVocoMode && vocoField != null) {
                debugAttrAction(ctx.vo.getClass(), ctx.voField, String.format("in VOCO mode (%s), set to empty %s", 
                        vocoField.getName(), type.getSimpleName()));
                ReflectionUtil.setFieldValue(ctx.vo, vocoField, newInstance());
                ctx.voField.set(ctx.vo, null);
            }
            else {
                debugAttrAction(ctx.vo.getClass(), ctx.voField, String.format(
                        "source is empty %s, set to empty %s", collection.getClass().getSimpleName(), type.getSimpleName()));
                ctx.voField.set(ctx.vo, newInstance());
                if(vocoField != null) {
                    ReflectionUtil.setFieldValue(ctx.vo, vocoField, null);
                }
            }
            return true;
        }
        
        // Get list element type
        Class<?> collectionElemClass = ReflectionUtil.getFieldGenericType(ctx.voField);
        if(ValueObject.class.isAssignableFrom(collectionElemClass)) { // collection of value object
            if(inVocoMode && vocoField != null) {
                String action = String.format("in VOCO mode (%s), retrieve and set id", vocoField.getName());
                debugAttrAction(ctx.vo.getClass(), ctx.voField, action);
                Collection idCollection = newInstance();
                for(Object o : collection) {
                    Long id = (Long) ReflectionUtil.getFieldValue(o, "id");
                    if(id != null) {
                        idCollection.add(id);
                    }
                }
                // Set the new Id collection and clear out the original collection
                if(idCollection instanceof List) {
                    Collections.sort((List<Long>) idCollection);
                }
                ReflectionUtil.setFieldValue(ctx.vo, vocoField, idCollection);
                ctx.voField.set(ctx.vo, null);
            }
            else {
                String action = String.format("type is %s<%s>, set recursively", 
                        type.getSimpleName(), collectionElemClass.getSimpleName());
                debugAttrAction(ctx.vo.getClass(), ctx.voField, action);
                // Set the original field value and clear out vocoField
                List theList = ctx.converterDoToVo.toVoList(
                        curVocoLevel + 1, collectionElemClass, (Map) ctx.voClassMapping, collection);
                Collection theCollection = newInstance();
                theCollection.addAll(theList);
                ctx.voField.set(ctx.vo, theCollection);
                if(vocoField != null) {
                    ReflectionUtil.setFieldValue(ctx.vo, vocoField, null);
                }
            }
            return true;
        }
        else if(collectionElemClass.isAssignableFrom(collection.iterator().next().getClass())) { // compatible type
            debugAttrAction(ctx.vo.getClass(), ctx.voField, String.format(
                    "compatible type, convert to %s and set", type.getSimpleName()));
            Collection voFieldValueCollection = newInstance();
            voFieldValueCollection.addAll(collection);
            ctx.voField.set(ctx.vo, voFieldValueCollection);
            return true;
        }
        else if(Long.class.isAssignableFrom(collectionElemClass) &&
                DataObject.class.isAssignableFrom(collection.iterator().next().getClass())) {
            // Special handling: if VO has a Collection<Long>, which is mapped to a Collection<DO>,
            // the Collection<Long> will be populated with DO Id's. 
            // TODO wei.song We assume DO PK are Long, which is hopefully true :)
            debugAttrAction(ctx.vo.getClass(), ctx.voField, "source is DataObject, extract id");
            Collection idCollection = newInstance();
            for(Object o : collection) {
                Long id = (Long) ReflectionUtil.getFieldValue(o, "id");
                if(id != null) {
                    idCollection.add(id);
                }
            }
            ctx.voField.set(ctx.vo, idCollection);
            return true;
        }
        else if (Enum.class.isAssignableFrom(collection.iterator().next().getClass()) &&
                    String.class.isAssignableFrom(collectionElemClass)) {
            List theList = new ArrayList();
            String action = String.format("convert List<%s> to List<String>", collection.iterator().next().getClass().getSimpleName());
            debugAttrAction(ctx.vo.getClass(), ctx.voField, action);
            for (Enum e : (Collection<Enum>) collection) {
                theList.add(e.toString());
            }
            ctx.voField.set(ctx.vo, theList);
            return true;
        }
        
        throw new Exception("setting of different element type not supported");

    }
    
    @Override
    boolean voToDo(VoToDoContext ctx) throws Exception {
        
        if(type.isAssignableFrom(ctx.voField.getType()) == false) {
             return false;
        }
        
        Field vocoField = ctx.converterVoToDo.getVocoField(ctx.voField);
        return vocoField == null ?
                handleVoField(ctx)
              : handleVocoFieldPair(ctx, vocoField);
        
    }

    private boolean handleVocoFieldPair(VoToDoContext ctx, Field vocoField) throws Exception {
        
        debugAttrAction(ctx.vo.getClass(), ctx.voField, String.format(
                "combine VO filed %s (recursive) and VOCO field %s (retrieval)",
                ctx.voField.getName(), vocoField.getName()));
        
        Collection<BaseDataObject> doCollection = (Collection) ctx.doField.get(ctx.o);
        if(doCollection == null) {
            doCollection = newInstance();
            ctx.doField.set(ctx.o, doCollection);
        }
        
        // Produce the VO collection
        Collection<BaseValueObject> voCollection = (Collection) ctx.voFieldValue;
        
        // Produce the VOCO collection
        Collection<Long> vocoIdCollection = (Collection) ReflectionUtil.getFieldValue(ctx.vo, vocoField.getName());

        // Merge both into the DO collection
        applyToDoCollection(ctx, doCollection, voCollection, vocoIdCollection);
        
        return true;
    }

    private boolean handleVoField(VoToDoContext ctx) throws Exception {
                
        Assert.notNull(ctx.voFieldValue, "VO field must not be null");
        
        if (ctx.doField.getType().isAssignableFrom(ctx.voFieldValue.getClass()) == false
                          || type.isAssignableFrom(ctx.voFieldValue.getClass()) == false) {
            return false;
        }

        Collection voCollection = (Collection) ctx.voFieldValue;
        if (voCollection.isEmpty()) {
            debugAttrAction(ctx.o.getClass(), ctx.doField, String.format(
                    "source is empty %s, set to empty %s", ctx.doField.getType().getSimpleName(), type.getSimpleName()));
            ctx.doField.set(ctx.o, newInstance());
            return true;
        }
        
        // Get collection element type
        Class<?> doCollectionElemClass = ReflectionUtil.getFieldGenericType(ctx.doField);
        if (DataObject.class.isAssignableFrom(doCollectionElemClass)) {
            Collection<BaseDataObject> doCollection = (Collection) ctx.doField.get(ctx.o);
            if(doCollection == null) {
                doCollection = newInstance();
                ctx.doField.set(ctx.o, doCollection);
            }
            Class<?> voCollectionElemClass = ReflectionUtil.getFieldGenericType(ctx.voField);
            if(Long.class.isAssignableFrom(voCollectionElemClass)) {
                // Special handling: if VO has a Collection<Long>, which is mapped to a Collection<DO>,
                // the Collection<Long> will be populated with DO Id's. 
                // TODO wei.song We assume DO PK are Long, which is hopefully true :)
                debugAttrAction(ctx.o.getClass(), ctx.doFieldName, String.format(
                        "type is %s<Long> and mapped to Collection<%s>, retrieve from database", 
                        ctx.doField.getType().getSimpleName(), doCollectionElemClass.getSimpleName()));
                applyToDoCollection(ctx, doCollection, null, voCollection);
                return true;
            }
            else {
                String action = String.format("type is %s<%s>, set recursively", 
                        ctx.doField.getType().getSimpleName(), doCollectionElemClass.getSimpleName());
                debugAttrAction(ctx.o.getClass(), ctx.doField, action);
                applyToDoCollection(ctx, doCollection, voCollection, null);
                return true;
            }
        }
        else if (doCollectionElemClass.isAssignableFrom(voCollection.iterator().next().getClass())) {
            debugAttrAction(ctx.o.getClass(), ctx.doField, "compatible type, set directly");
            ctx.doField.set(ctx.o, ctx.voFieldValue);
            return true;
        }
        else if (Enum.class.isAssignableFrom(doCollectionElemClass) && 
                    String.class.isAssignableFrom(voCollection.iterator().next().getClass())) {
            List theList = new ArrayList();
            String action = String.format("convert List<String> to List<%s>", doCollectionElemClass.getSimpleName());
            debugAttrAction(ctx.o.getClass(), ctx.doField, action);
            Class<?>[] parameterClass = {String.class};
            for (String str : (Collection<String>) voCollection) {
                Method valueOfMethod = ReflectionUtil.getMethod(doCollectionElemClass, "valueOf", parameterClass);
                Object value = valueOfMethod.invoke(null, str);
                theList.add(value);
            }
            ctx.doField.set(ctx.o, theList);
            return true;
        }

        throw new Exception("setting of different type not supported");

    }
    
    private void applyToDoCollection(VoToDoContext ctx, Collection<BaseDataObject> doCollection, 
            Collection<BaseValueObject> voCollection, Collection<Long> idCollection) throws Exception {

        // Produce the current Id set
        Set<Serializable> idSet = new HashSet<>(10);
        for(BaseDataObject o : doCollection) {
            if(o.getId() != null) {
                idSet.add(o.getId());
            }
        }
        
        int position = 0;
        String orderColumnName = getCollectionOrderByColumn(ctx.doField);
        
        // Add case for Id collection
        if(idCollection != null) {
            if(idCollection.size() > 0 && voCollection != null && voCollection.size() > 0 
                    && orderColumnName != null) {
                throw new Exception("Both idCollection and voCollection cannot be present on @OneToMany with @OrderBy");
            }
            Class<?> doCollectionElemClass = ReflectionUtil.getFieldGenericType(ctx.doField);
            JpaRepository repo = ctx.metadata.getRepository(doCollectionElemClass);
            for(Long id : idCollection) {
                if(idSet.contains(id) == false) {
                    BaseDataObject o = (BaseDataObject) CommonUtil.unboxProxy(repo.findOne(id));
                    if(o != null) {
                        if(orderColumnName != null) {
                            // This is to ensure doObj are ordered according to the VO list
                            ReflectionUtil.setFieldValue(o, orderColumnName, position++);
                        }
                        doCollection.add(o); // New member
                    }
                }
                else {
                    idSet.remove(id);
                }
            }
        }

        // Add/update for VO collection
        if(voCollection != null) {
            for(BaseValueObject vo : voCollection) {
                BaseDataObject doObj = (BaseDataObject) ctx.converterVoToDo.toDo(vo);
                if(orderColumnName != null) {
                    // This is to ensure doObj are ordered according to the VO list
                    ReflectionUtil.setFieldValue(doObj, orderColumnName, position++);
                }
                if(vo.getId() == null // New member, new object
                        || idSet.contains(vo.getId()) == false) { // New member, existing object 
                    doCollection.add(doObj);
                }
                else {
                    // The doObj is in session, and already carries changes from VO, no merge required
                    idSet.remove(vo.getId()); // Existing member
                }
            }
        }
        
        // Removal case. Implementation is inefficient due to bug 
        // https://hibernate.atlassian.net/browse/HHH-3799
        //
        if(idSet.isEmpty() == false) {
            LinkedList<BaseDataObject> tempList = new LinkedList<>();
            Iterator<BaseDataObject> iter = doCollection.iterator();
            while(iter.hasNext()) {
                BaseDataObject o = iter.next();
                if(idSet.contains(o.getId()) == false) {
                    tempList.add(o);
                }
            }
            doCollection.clear();
            doCollection.addAll(tempList);
        }
    }
    
    private String getCollectionOrderByColumn(Field doCollectionField) throws Exception {
        if(List.class.isAssignableFrom(doCollectionField.getType())) {
            OrderBy anno = doCollectionField.getAnnotation(OrderBy.class);
            if(anno != null) {
                // Caveats:
                //   1. Empty value is treated as null
                //   2. "dot" notated attribute not handled
                return anno.value() == null || anno.value().length() <= 0 || anno.value().contains(".") ?
                        null : anno.value();
            }
        }
        return null;
    }
}
