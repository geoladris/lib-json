package de.csgis.geobricks;

import net.sf.json.JSONObject;

/**
 * Utility class for operations with JSON values.
 * 
 * @author vicgonco
 */
public class JSONUtils {
	private JSONUtils() {
	}

	/**
	 * Merges the two JSON objects.
	 * 
	 * @param defaultObj
	 *            The original object.
	 * @param overrides
	 *            The object used for overriding. If both objects have the same
	 *            key, this object's value is used.
	 * @return A new object with the merged values.
	 */
	public static JSONObject merge(JSONObject defaultObj, JSONObject overrides) {
		// We create a copy of the default object
		JSONObject ret = JSONObject.fromObject(defaultObj != null
				&& !defaultObj.isNullObject() ? defaultObj.toString() : "{}");

		if (overrides != null && !overrides.isEmpty()
				&& !overrides.isNullObject()) {
			mergeRecursive(ret, overrides);
		}
		return ret;
	}

	private static void mergeRecursive(JSONObject obj, JSONObject overrides) {
		for (Object key : obj.keySet()) {
			if (overrides.containsKey(key)) {
				Object defaultElement = obj.get(key);
				Object overrideElement = overrides.get(key);
				if (defaultElement instanceof JSONObject) {
					JSONObject defaultObj = (JSONObject) defaultElement;
					if (overrideElement instanceof JSONObject) {
						mergeRecursive(defaultObj, (JSONObject) overrideElement);
					} else {
						obj.put(key, overrideElement);
					}
				} else {
					obj.put(key, overrideElement);
				}
			}
		}

		for (Object key : overrides.keySet()) {
			if (!obj.containsKey(key)) {
				obj.put(key, overrides.get(key));
			}
		}
	}
}
