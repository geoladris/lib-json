package de.csgis.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.csgis.commons.JSONContentProvider;

public class JSONContentProviderTest {
	private File dir;
	private JSONContentProvider provider;

	@Before
	public void setup() throws Exception {
		this.dir = File.createTempFile("geobricks-test", "");
		this.dir.delete();
		this.dir.mkdir();
		this.provider = new JSONContentProvider(this.dir.getAbsolutePath());
	}

	@After
	public void teardown() throws Exception {
		FileUtils.deleteDirectory(this.dir);
	}

	@Test
	public void fileDeleted() throws Exception {
		createFile("a.json", "{'a1':true, 'a2':false}");
		createFile("b.json", "{'b1':42, 'b2':'string'}");

		Map<String, JSONObject> contents = this.provider.get();
		assertEquals(2, contents.size());

		new File(this.dir, "a.json").delete();
		contents = this.provider.get();
		assertEquals(1, contents.size());
		JSONObject b = contents.get("b");
		assertEquals(42, b.getInt("b1"));
		assertEquals("string", b.getString("b2"));
	}

	@Test
	public void fileAdded() throws Exception {
		createFile("a.json", "{'a1':true, 'a2':false}");

		Map<String, JSONObject> contents = this.provider.get();
		assertEquals(1, contents.size());

		createFile("b.json", "{'b1':42, 'b2':'string'}");
		contents = this.provider.get();
		assertEquals(2, contents.size());
		JSONObject a = contents.get("a");
		JSONObject b = contents.get("b");
		assertTrue(a.getBoolean("a1"));
		assertFalse(a.getBoolean("a2"));
		assertEquals(42, b.getInt("b1"));
		assertEquals("string", b.getString("b2"));
	}

	@Test
	public void fileModified() throws Exception {
		createFile("a.json", "{'a1':true, 'a2':false}");
		createFile("b.json", "{'b1':42, 'b2':'string'}");
		Map<String, JSONObject> contents = this.provider.get();
		assertEquals(2, contents.size());
		assertTrue(contents.get("a").getBoolean("a1"));
		assertFalse(contents.get("a").getBoolean("a2"));

		// Last modified only takes seconds into account, not millis. We wait
		// for at least one second.
		Thread.sleep(1000);
		createFile("a.json", "{'a1':false}");

		contents = this.provider.get();
		assertEquals(2, contents.size());
		assertFalse(contents.get("a").getBoolean("a1"));
		assertFalse(contents.get("a").has("a2"));
	}

	@Test
	public void ignoresInvalidContent() throws Exception {
		// Only JSON objects allowed
		createFile("a.json", "[]");
		assertEquals(0, this.provider.get().size());
	}

	@Test
	public void ignoresNonJSONFiles() throws Exception {
		// Only .json files allowed
		createFile("a.txt", "{'a1':false}");
		assertEquals(0, this.provider.get().size());
	}

	@Test
	public void ignoresNonReadableFiles() throws Exception {
		createFile("a.json", "{'a1':false}");
		assertEquals(1, this.provider.get().size());

		// Last modified only takes seconds into account, not millis. We wait
		// for at least one second.
		Thread.sleep(1000);
		new File(this.dir, "a.json").setReadable(false);
		new File(this.dir, "a.json")
				.setLastModified(System.currentTimeMillis());
		assertEquals(0, this.provider.get().size());
	}

	private void createFile(String name, String content) throws IOException {
		File file = new File(this.dir, name);
		FileWriter writer = new FileWriter(file);
		IOUtils.write(content, writer);
		writer.close();
	}
}
