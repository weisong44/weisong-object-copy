package com.weisong.common.vodo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.weisong.common.util.ReflectionUtil;
import com.weisong.common.vodo.Dummy.Size;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:context/vodoutil-test-context.xml")
public class TestConverterDoToVo {

    @Autowired
    private VoDoUtil voDoUtil;

    @Test
    public void testDoToVo() throws Exception {
        new WithVocoDisabled(voDoUtil) {
            @Override
            public void doExecute() throws Exception {
                Dummy o = createDummy();
                Dummy2 o2 = createDummy2();
                DummyVo vo = createDummyVo(o, o2);
                validateDummyVo(o, o2, vo);
                TestUtil.prettyPrint(vo);
            }
        };
    }

    @Test
    public void testDoToVo_VocoLevel1() throws Exception {
        new WithVocoLevel(voDoUtil, 1) {
            @Override
            public void doExecute() throws Exception {
                DummyVo vo = createDummyVo(createDummy(), createDummy2());
                Assert.assertNull(vo.getChildren2()); // cleared out
                Assert.assertNull(vo.getChildren()); // cleared out
                Assert.assertEquals(2, vo.getVocoChildrenIds().size());
                Assert.assertNull(vo.getChild());
                Assert.assertNotNull(vo.getVocoChildId());
                TestUtil.prettyPrint(vo);
            }
        };
    }

    @Test
    public void testDoToVo_VocoLevel2() throws Exception {
        new WithVocoLevel(voDoUtil, 2) {
            @Override
            public void doExecute() throws Exception {
                DummyVo vo = createDummyVo(createDummy(), createDummy2());
                Assert.assertNull(vo.getIgnored());
                Assert.assertEquals(2, vo.getChildren().size());
                Assert.assertEquals(2, vo.getChildren2().size());
                Assert.assertNull(vo.getVocoChildrenIds());
                Assert.assertNull(vo.getChild().getChildren());
                Assert.assertEquals(2, vo.getChild().getVocoChildrenIds().size());
                Assert.assertNull(vo.getChild().getChildrenSet());
                Assert.assertEquals(2, vo.getChild().getVocoChildrenSetIds().size());
                TestUtil.prettyPrint(vo);
            }
        };
    }

    @Test
    public void testDoToVo_VocoLevel3() throws Exception {
        new WithVocoLevel(voDoUtil, 3) {
            @Override
            public void doExecute() throws Exception {
                Dummy o = createDummy();
                Dummy2 o2 = createDummy2();
                DummyVo vo = createDummyVo(o, o2);
                validateDummyVo(o, o2, vo);
                TestUtil.prettyPrint(vo);
            }
        };
    }

    private DummyVo createDummyVo(Dummy o, Dummy2 o2) throws Exception {
        // Create mapping
        Map<Class<?>, Class<?>> map = new HashMap<>(5);
        map.put(Dummy.class, DummyVo.class);
        map.put(DummyCloseFriend.class, DummyCloseFriendVo.class);
        map.put(DummySocialFriend.class, DummySocialFriendVo.class);
        map.put(Hunting.class, HuntingVo.class);
        map.put(Fishing.class, FishingVo.class);
        return voDoUtil.toVo(map, o, o2);
    }

    private void validateDummyVo(Dummy o, Dummy2 o2, DummyVo vo) {
        // Dummy
        Assert.assertEquals(o.getCreatedAt(), vo.getCreatedAt());
        Assert.assertEquals(o.getUpdatedAt(), vo.getUpdatedAt());
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals("Changed by helper", vo.getName());
        Assert.assertEquals(vo.getChildId(), o.getChild().getId());
        Assert.assertEquals(vo.getChildName(), o.getChild().getChildName());
        Assert.assertEquals(o.getSize().toString(), vo.getSize());
        Assert.assertEquals("Byte Array To String Conversion", vo.getByteArrayString());
        Assert.assertNull(vo.getNullNameValue());
        Assert.assertNull(vo.getNullNameValue2());
        Assert.assertNull(vo.getNotExisting());
        Assert.assertNull(vo.getVocoChildrenIds());
        // Children
        validateDummyChildVo(o.getChild(), vo.getChild());
        for (int i = 0; i < o.getChildren().size(); i++) {
            validateDummyChildVo(o.getChildren().get(i), vo.getChildren().get(i));
            validateDummyChildVo(o.getChildren().get(i), vo.getChildren2().get(i));
        }
        Assert.assertEquals(vo.getChildrenId().size(), o.getChildren().size());
        List<Long> idList = new ArrayList<Long>(10);
        for (DummyChild c : o.getChildren()) {
            idList.add(c.getId());
        }
        Assert.assertEquals(vo.getChildrenId(), idList);

        // Friends
        validateFriendVo(o.getCloseFriend(), vo.getCloseFriend());
        Assert.assertEquals(o.getCloseFriend().getId(), vo.getFriendId());
        Assert.assertEquals(vo.getFriendIds().size(), o.getFriends().size());
        validateFriendVo(o.getSocialFriend(), vo.getSocialFriend());
        Assert.assertEquals(vo.getFriendIds().size(), o.getFriends().size());
        idList.clear();
        for (DummyFriendBase<?> f : o.getFriends()) {
            idList.add(f.getId());
        }
        Assert.assertEquals(vo.getFriendIds(), idList);
        // Null values
        Assert.assertEquals(vo.getNullNameValue(), null);
        Assert.assertEquals(vo.getNullNameValue2(), null);
        // Lists
        Assert.assertEquals(vo.getLongList(), o.getLongList());
        Assert.assertEquals(vo.getStringList(), o.getStringList());
        Assert.assertEquals(vo.getDifferentTypeList().size(), 0);
        // Dummy 2
        Assert.assertEquals(o2.getId(), vo.getDummy2Id());
        Assert.assertEquals(o2.getName(), vo.getDummy2Name());
    }

    private void validateDummyChildVo(DummyChild o, DummyChildVo vo) {
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals(o.getChildName(), vo.getChildName());
        for (int i = 0; i < o.getChildren().size(); i++) {
            validateDummyGrandChildVo(o.getChildren().get(i), vo.getChildren().get(i));
        }
        DummyGrandChild[] oArray = TestUtil.toArray(o.getChildrenSet(), new DummyGrandChild[o.getChildrenSet().size()]);
        DummyGrandChildVo[] voArray = TestUtil.toArray(vo.getChildrenSet(), new DummyGrandChildVo[vo.getChildrenSet().size()]);
        for (int i = 0; i < oArray.length; i++) {
            validateDummyGrandChildVo(oArray[i], voArray[i]);
        }
        Assert.assertNull(vo.getVocoChildrenIds());
        Assert.assertNull(vo.getVocoChildrenSetIds());
    }
    
    private void validateDummyGrandChildVo(DummyGrandChild o, DummyGrandChildVo vo) {
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals(o.getGrandChildName(), vo.getGrandChildName());
        Assert.assertNull(vo.getStrId());
    }

    private void validateFriendVo(DummyFriendBase<?> o, DummyFriendBaseVo<?> vo) {
        if (o instanceof DummyCloseFriend) {
            DummyCloseFriend co = (DummyCloseFriend) o;
            DummyCloseFriendVo cvo = (DummyCloseFriendVo) vo;
            Assert.assertEquals(co.getId(), vo.getId());
            Assert.assertEquals(co.getFriendOfDummyName(), cvo.getFriendOfDummyName());
            Assert.assertEquals(co.getFriendOfDummyName(), cvo.getName());
            Assert.assertEquals(co.getCloseFriendDescription(), cvo.getDescription());
            validateHobbyVo(co.getHobby(), cvo.getHobby());
        }
        else if (o instanceof DummySocialFriend) {
            DummySocialFriend so = (DummySocialFriend) o;
            DummySocialFriendVo svo = (DummySocialFriendVo) vo;
            Assert.assertEquals(so.getId(), vo.getId());
            Assert.assertEquals(so.getFriendOfDummyName(), svo.getFriendOfDummyName());
            Assert.assertEquals(so.getFriendOfDummyName(), svo.getName());
            Assert.assertEquals(so.getSocialFriendDescription(), svo.getDescription());
            validateHobbyVo(so.getHobby(), vo.getHobby());
        }
        else {
            Assert.assertTrue(false);
        }
    }

    private void validateHobbyVo(Object o, Object vo) {
        if (o instanceof Hunting) {
            Hunting ho = (Hunting) o;
            HuntingVo hvo = (HuntingVo) vo;
            Assert.assertEquals(ho.getPlaceToHunt(), hvo.getPlaceToHunt());
        }
        else if (o instanceof Fishing) {
            Fishing ho = (Fishing) o;
            FishingVo hvo = (FishingVo) vo;
            Assert.assertEquals(ho.getPlaceToFish(), hvo.getPlaceToFish());
        }
        else {
            Assert.assertTrue(false);
        }
    }

    private Dummy createDummy() throws Exception {
        // Dummy
        Dummy dummy = new Dummy();
        ReflectionUtil.setFieldValue(dummy, "createdAt", System.currentTimeMillis());
        ReflectionUtil.setFieldValue(dummy, "updatedAt", System.currentTimeMillis() + 1);
        setObjectId(dummy, 10L);
        dummy.setName("Dummy");
        dummy.setIgnored("Ignored attribute");
        dummy.getLongList().add(1L);
        dummy.getLongList().add(2L);
        dummy.getStringList().add("String 1");
        dummy.getStringList().add("String 2");
        dummy.setSize(Size.Medium);
        dummy.setByteArrayString("Byte Array To String Conversion".getBytes("UTF-8"));
        // Kid 1
        DummyChild child = new DummyChild();
        child.setOrder(1);
        child.setChildName("Child");
        setObjectId(child, 200L);
        dummy.getChildren().add(child);
        dummy.setChild(child);
        // Kid 2
        DummyChild child2 = new DummyChild();
        child2.setOrder(0);
        child2.setChildName("Child 2");
        setObjectId(child2, 201L);
        dummy.getChildren().add(child2);
        // Grand kid
        DummyGrandChild grandChild = new DummyGrandChild();
        grandChild.setGrandChildName("Grand child");
        setObjectId(grandChild, 101L);
        child.getChildren().add(grandChild);
        child.getChildrenId().add(grandChild.getId());
        child.getChildrenSet().add(grandChild);
        // Grand kid 2
        DummyGrandChild grandChild2 = new DummyGrandChild();
        grandChild2.setGrandChildName("Grand child 2");
        setObjectId(grandChild2, 100L);
        child.getChildren().add(grandChild2);
        child.getChildrenId().add(grandChild2.getId());
        child.getChildrenSet().add(grandChild2);
        // Close Friend
        DummyCloseFriend closeFriend = new DummyCloseFriend();
        closeFriend.setFriendOfDummyName("Close friend");
        closeFriend.setCloseFriendDescription("This is a close friend");
        Hunting hunting = new Hunting();
        hunting.setPlaceToHunt("Rocky Mountain");
        closeFriend.setHobby(hunting);
        setObjectId(closeFriend, 300L);
        dummy.setCloseFriend(closeFriend);
        dummy.getFriends().add(closeFriend);
        // Social Friend
        DummySocialFriend socialFriend = new DummySocialFriend();
        socialFriend.setFriendOfDummyName("Social friend");
        socialFriend.setSocialFriendDescription("This is a social friend");
        Fishing fishing = new Fishing();
        fishing.setPlaceToFish("Crater Lake");
        socialFriend.setHobby(fishing);
        setObjectId(socialFriend, 301L);
        dummy.setSocialFriend(socialFriend);
        dummy.getFriends().add(socialFriend);

        return dummy;
    }

    private Dummy2 createDummy2() throws Exception {
        Dummy2 dummy2 = new Dummy2();
        setObjectId(dummy2, 37L);
        dummy2.setName("This is dummy No.2");
        return dummy2;
    }

    private void setObjectId(Object o, Long id) throws Exception {
        ReflectionUtil.setFieldValue(o, "id", id);
    }
}
