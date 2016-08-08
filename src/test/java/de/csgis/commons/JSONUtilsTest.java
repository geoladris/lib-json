package de.csgis.commons;

import static org.junit.Assert.assertEquals;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

import de.csgis.commons.JSONUtils;

public class JSONUtilsTest {
	@Test
	public void mergeSimpleAttributes() throws Exception {
		JSONObject original = JSONObject.fromObject("{'a' : 1, 'b' : 2}");
		JSONObject overrides = JSONObject.fromObject("{'a' : 7}");
		JSONObject merged = JSONUtils.merge(original, overrides);
		assertEquals(7, merged.getInt("a"));
		assertEquals(2, merged.getInt("b"));
	}

	@Test
	public void mergeArray() throws Exception {
		JSONObject original = JSONObject.fromObject("{'a' : [1,2,3], 'b' : 2}");
		JSONObject overrides = JSONObject.fromObject("{'a' : [7,8]}");
		JSONObject merged = JSONUtils.merge(original, overrides);
		JSONArray array = merged.getJSONArray("a");
		assertEquals(2, array.size());
		assertEquals(7, array.get(0));
		assertEquals(8, array.get(1));
		assertEquals(2, merged.getInt("b"));
	}

	@Test
	public void addMissingElements() throws Exception {
		JSONObject original = JSONObject.fromObject("{'a' : 1, 'b' : 2}");
		JSONObject overrides = JSONObject.fromObject("{'c' : 47}");
		JSONObject merged = JSONUtils.merge(original, overrides);
		assertEquals(1, merged.getInt("a"));
		assertEquals(2, merged.getInt("b"));
		assertEquals(47, merged.get("c"));
	}

	@Test
	public void mergeSimpleAttributesOnNestedObject() throws Exception {
		JSONObject original = JSONObject.fromObject("{a : 1, "//
				+ "b : { "//
				+ "   b1 : 'one',"//
				+ "   b2 : {"//
				+ "     b21 : 'two-one',"//
				+ "     b22 : 'two-two'"//
				+ "   },"//
				+ "   b3 : 'three',"//
				+ "}}");
		JSONObject overrides = JSONObject.fromObject("{'a' : 17, "//
				+ "b : { "//
				+ "   b2 : {"//
				+ "     b22 : 'ooo'"//
				+ "   },"//
				+ "   b3 : '333',"//
				+ "}}");
		JSONObject merged = JSONUtils.merge(original, overrides);
		assertEquals(17, merged.getInt("a"));
		JSONObject b = merged.getJSONObject("b");
		assertEquals("one", b.getString("b1"));
		assertEquals("333", b.getString("b3"));
		JSONObject b2 = b.getJSONObject("b2");
		assertEquals("two-one", b2.getString("b21"));
		assertEquals("ooo", b2.getString("b22"));
	}

	@Test
	public void mergeNullDefaultObject() {
		JSONObject overrides = JSONObject.fromObject("{'a' : [7,8]}");

		assertEquals(overrides, JSONUtils.merge(null, overrides));
		assertEquals(overrides,
				JSONUtils.merge(new JSONObject(true), overrides));
	}

	@Test
	public void mergeNullOverrides() {
		JSONObject defaultObj = JSONObject.fromObject("{'a' : [7,8]}");
		JSONObject merged = JSONUtils.merge(defaultObj, null);
		assertEquals(defaultObj, merged);
	}
}
