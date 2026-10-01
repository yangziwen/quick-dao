package io.github.yangziwen.quickdao.core.util;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class JsonUtilTest {

    @Test
    public void testSerializeNullReturnsEmpty() {
        Assert.assertEquals("", JsonUtil.serialize(null));
    }

    @Test
    public void testSerializeObjectReturnsJson() {
        String json = JsonUtil.serialize(new Item(1, "name"));
        Assert.assertTrue(json.contains("\"id\":1"));
        Assert.assertTrue(json.contains("\"name\":\"name\""));
    }

    @Test
    public void testDeserializeBlankReturnsNull() {
        Assert.assertNull(JsonUtil.deserialize("", Item.class));
        Assert.assertNull(JsonUtil.deserialize("  ", Item.class));
        Assert.assertNull(JsonUtil.deserialize(null, Item.class));
    }

    @Test
    public void testDeserializeTypeReference() {
        Map<String, Object> map = JsonUtil.deserialize("{\"a\":1,\"b\":\"x\"}", JsonUtil.MAP_TYPE_REFERENCE);
        Assert.assertEquals(1, map.get("a"));
        Assert.assertEquals("x", map.get("b"));
    }

    @Test
    public void testDeserializeToMapWithBlankReturnsEmptyMap() {
        Map<String, Object> map = JsonUtil.deserializeToMap("");
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testDeserializeClass() {
        Item item = JsonUtil.deserialize("{\"id\":5,\"name\":\"foo\"}", Item.class);
        Assert.assertEquals(5, item.getId());
        Assert.assertEquals("foo", item.getName());
    }

    public static class Item {
        private int id;
        private String name;
        public Item() {}
        public Item(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

}
