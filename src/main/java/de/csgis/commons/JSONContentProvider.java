package de.csgis.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Class for providing the contents of all <code>.json</code> files in a
 * directory. It only reads each file when necessary (it has not been read yet
 * or it has changed since the last reading).
 * 
 * @author vicgonco
 */
public class JSONContentProvider {
	private static final Logger logger = Logger
			.getLogger(JSONContentProvider.class);

	private File directory;
	private Map<String, JSONObject> contents;
	private Map<String, Long> lastFileAccesses = new HashMap<String, Long>();

	/**
	 * Creates a new JSON content provider.
	 * 
	 * @param directory
	 *            Path to the directory containing the <code>.json</code> files.
	 */
	public JSONContentProvider(String directory) {
		this.directory = new File(directory);
		this.contents = new HashMap<String, JSONObject>();
		this.lastFileAccesses = new HashMap<String, Long>();
	}

	/**
	 * Gets all the JSON contents.
	 * 
	 * @return A map with the JSON contents. Keys are file names without the
	 *         <code>.json</code> extension. Values are file contents.
	 */
	public Map<String, JSONObject> get() {
		updateContentsIfNeeded();
		return contents;
	}

	private void updateContentsIfNeeded() {
		File[] files = this.directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});

		if (files == null) {
			this.contents.clear();
			return;
		}

		List<String> toRemove = new ArrayList<String>();
		for (String path : this.contents.keySet()) {
			File file = new File(this.directory, path + ".json");
			if (!file.exists()) {
				toRemove.add(path);
			}
		}

		for (String path : toRemove) {
			this.contents.remove(path);
		}

		for (File file : files) {
			String name = file.getName();
			String basename = name.substring(0, name.lastIndexOf('.'));
			JSONObject jsonContent = this.contents.get(basename);
			Long lastAccess = this.lastFileAccesses.get(basename);

			if (jsonContent != null && lastAccess != null
					&& file.lastModified() < lastAccess) {
				continue;
			}

			try {
				String content = IOUtils.toString(new FileInputStream(file));
				this.contents.put(basename, JSONObject.fromObject(content));
				this.lastFileAccesses.put(basename,
						new Long(System.currentTimeMillis()));
			} catch (JSONException e) {
				logger.error("Cannot read JSON plugin "
						+ "config from config dir", e);
				this.contents.remove(basename);
			} catch (IOException e) {
				logger.error("Cannot read JSON plugin "
						+ "config from config dir", e);
				this.contents.remove(basename);
			}
		}
	}
}
