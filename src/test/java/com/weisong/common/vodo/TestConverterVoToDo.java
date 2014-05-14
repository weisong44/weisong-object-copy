package com.weisong.common.vodo;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.weisong.common.util.ReflectionUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:context/vodoutil-test-context.xml")
public class TestConverterVoToDo {

    @Autowired
    private VoDoUtil voDoUtil;

    @Test
    public void testVoToDo() throws Exception {
        DummyVo vo = createDummyVO();
        TestUtil.prettyPrint(vo);
        Dummy o = (Dummy) voDoUtil.toDo(vo);
        validateDummy(o, vo);
        TestUtil.prettyPrint(o);
    }

    private DummyVo createDummyVO() throws Exception {
        // DummyVo
        DummyVo dummyVo = new DummyVo();
        Long t = System.currentTimeMillis();
        dummyVo.setCreatedAt(t);
        dummyVo.setUpdatedAt(t + 1L);
        dummyVo.setIgnored("Ignored attribute");
        setObjectId(dummyVo, 10L);
        // Commented out to avoid failure, no repositories avail in core
        // dummyVo.setChildId(100L);
        dummyVo.setChildName("blabla");
        dummyVo.setName("DummyVo");
        dummyVo.setSize("Large");
        dummyVo.setByteArrayString("Byte Array To String Conversion");
        dummyVo.getLongList().add(1L);
        dummyVo.getLongList().add(2L);
        dummyVo.getStringList().add("String 1");
        dummyVo.getStringList().add("String 2");

        // Kid 1
        DummyChildVo childVo = new DummyChildVo();
        childVo.setChildName("Child");
        setObjectId(childVo, 200L);
        dummyVo.setChildren(new ArrayList<DummyChildVo>(5));
        dummyVo.getChildren().add(childVo);
        dummyVo.setChild(childVo);
        // TODO wei.song
        // dummyVo.setChildId(childVo.getId());
        // Kid 2
        DummyChildVo childVo2 = new DummyChildVo();
        childVo2.setChildName("Child 2");
        setObjectId(childVo2, 201L);
        dummyVo.getChildren().add(childVo2);
        // Grand kid
        DummyGrandChildVo grandChildVo = new DummyGrandChildVo();
        grandChildVo.setGrandChildName("Grand child");
        setObjectId(grandChildVo, 101L);
        childVo.setChildren(new ArrayList<DummyGrandChildVo>(5));
        childVo.getChildren().add(grandChildVo);
        childVo.getChildrenId().add(grandChildVo.getId());
        childVo.getChildrenSet().add(grandChildVo);
        // Grand kid 2
        DummyGrandChildVo grandChildVo2 = new DummyGrandChildVo();
        grandChildVo2.setGrandChildName("Grand child 2");
        setObjectId(grandChildVo2, 100L);
        childVo.getChildren().add(grandChildVo2);
        childVo.getChildrenId().add(grandChildVo2.getId());
        childVo.getChildrenSet().add(grandChildVo2);
        // Close Friend
        DummyCloseFriendVo friendVo = new DummyCloseFriendVo();
        friendVo.setFriendOfDummyName("Close friend");
        friendVo.setDescription("This is a close friend");
        HuntingVo huntingVO = new HuntingVo();
        huntingVO.setPlaceToHunt("Black forest");
        friendVo.setHobby(huntingVO);
        setObjectId(friendVo, 300L);
        dummyVo.setCloseFriend(friendVo);
        dummyVo.getFriends().add(friendVo);
        // Social Friend
        DummySocialFriendVo socialFriendVo = new DummySocialFriendVo();
        socialFriendVo.setFriendOfDummyName("Social friend");
        socialFriendVo.setDescription("This is a social friend");
        FishingVo fishingVO = new FishingVo();
        fishingVO.setPlaceToFish("Red sea");
        socialFriendVo.setHobby(fishingVO);
        setObjectId(socialFriendVo, 301L);
        dummyVo.setSocialFriend(socialFriendVo);
        dummyVo.getFriends().add(socialFriendVo);

        return dummyVo;
    }

    private void validateDummy(Dummy o, DummyVo vo) throws Exception {
        // Dummy
        Assert.assertNull(o.getIgnored());
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals(o.getCreatedAt(), vo.getCreatedAt());
        Assert.assertEquals(o.getUpdatedAt(), vo.getUpdatedAt());
        Assert.assertEquals(o.getName(), "Changed by helper");
        Assert.assertEquals(o.getSize().toString(), vo.getSize());
        Assert.assertEquals(vo.getByteArrayString(), new String(o.getByteArrayString()));
        Assert.assertNull(vo.getNullNameValue());
        Assert.assertNull(vo.getNullNameValue2());
        Assert.assertNull(vo.getNotExisting());
        Assert.assertTrue(vo.getFriendIds().isEmpty());
        // Children
        validateDummyChild(o.getChild(), vo.getChild());
        for (int i = 0; i < o.getChildren().size(); i++) {
            validateDummyChild(o.getChildren().get(i), vo.getChildren().get(i));
        }

        // Friends
        validateDummyFriend(o.getCloseFriend(), vo.getCloseFriend());
        // Null values
        Assert.assertEquals(vo.getNullNameValue(), null);
        // Lists
        Assert.assertEquals(vo.getLongList(), o.getLongList());
        Assert.assertEquals(vo.getStringList(), o.getStringList());
    }

    private void validateDummyChild(DummyChild o, DummyChildVo vo) {
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals(o.getChildName(), vo.getChildName());
        if (o.getChildren() != null) {
            for (int i = 0; i < o.getChildren().size(); i++) {
                validateDummyGrandChild(o.getChildren().get(i), vo.getChildren().get(i));
            }
        }
        DummyGrandChild[] oArray = TestUtil.toArray(o.getChildrenSet(), new DummyGrandChild[o.getChildrenSet().size()]);
        DummyGrandChildVo[] voArray = TestUtil.toArray(vo.getChildrenSet(), new DummyGrandChildVo[vo.getChildrenSet().size()]);
        for (int i = 0; i < oArray.length; i++) {
            validateDummyGrandChild(oArray[i], voArray[i]);
        }
    }

    private void validateDummyGrandChild(DummyGrandChild o, DummyGrandChildVo vo) {
        Assert.assertEquals(o.getId(), vo.getId());
        Assert.assertEquals(o.getGrandChildName(), vo.getGrandChildName());
    }

    private void validateDummyFriend(DummyFriendBase<?> o, DummyFriendBaseVo<?> vo) {
        if (vo instanceof DummyCloseFriendVo) {
            DummyCloseFriend co = (DummyCloseFriend) o;
            DummyCloseFriendVo cvo = (DummyCloseFriendVo) vo;
            Assert.assertEquals(co.getId(), cvo.getId());
            Assert.assertEquals(co.getFriendOfDummyName(), cvo.getFriendOfDummyName());
            Assert.assertEquals(co.getCloseFriendDescription(), cvo.getDescription());
        }
        else if (vo instanceof DummySocialFriendVo) {
            DummySocialFriend so = (DummySocialFriend) o;
            DummySocialFriendVo svo = (DummySocialFriendVo) vo;
            Assert.assertEquals(so.getId(), svo.getId());
            Assert.assertEquals(so.getFriendOfDummyName(), svo.getFriendOfDummyName());
            Assert.assertEquals(so.getSocialFriendDescription(), svo.getDescription());
        }
        else {
            Assert.assertTrue(false);
        }
    }

    private void setObjectId(Object o, Long id) throws Exception {
        ReflectionUtil.setFieldValue(o, "id", id);
    }

}
