package de.csgis.commons.json;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geotools.geojson.geom.GeometryJSON;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import net.sf.json.JSONObject;

public class GeojsonPGHelperTest {
	private static final String ID_COLUMN = "gid";
	private static final String GEOM_COLUMN = "geom";
	private static final String TABLE = "points";
	private static final int SRID = 4326;

	private GeojsonPGHelper helper;
	private Connection conn;
	private GeometryFactory gf;

	@Before
	public void setup() {
		this.gf = new GeometryFactory();
		this.conn = mock(Connection.class);
		this.helper = new GeojsonPGHelper(conn, TABLE, ID_COLUMN, GEOM_COLUMN,
				SRID);
	}

	@Test
	public void sqlExceptionOnInsert() throws Exception {
		when(conn.prepareStatement(anyString())).thenThrow(new SQLException());
		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));
		try {
			this.helper.insert(geojson);
			fail();
		} catch (SQLException e) {
		}
	}

	@Test
	public void invalidGeomOnInsert() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));
		geojson.element("geometry", "{}");

		try {
			this.helper.insert(geojson);
			fail();
		} catch (IOException e) {
		}
	}

	@Test
	public void insertCorrectObject() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));

		this.helper.insert(geojson);

		verify(st).setObject(1, 1);
		verify(st).setObject(2, "name");
		verify(st).setObject(3, 42);
		verify(st).setInt(5, SRID);
		verify(st).executeUpdate();
	}

	@Test
	public void sqlExceptionOnUpdate() throws Exception {
		when(conn.prepareStatement(anyString())).thenThrow(new SQLException());
		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));
		try {
			this.helper.update(geojson);
			fail();
		} catch (SQLException e) {
		}

	}

	@Test
	public void invalidGeomOnUpdate() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));
		geojson.element("geometry", "{}");

		try {
			this.helper.update(geojson);
			fail();
		} catch (IOException e) {
		}
	}

	@Test
	public void updateObjectWithMissingIdProperty() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{"f1", "f2"},
				new Object[]{"name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));

		try {
			this.helper.update(geojson);
			fail();
		} catch (IOException e) {
		}
	}

	@Test
	public void updateCorrectObject() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));

		this.helper.update(geojson);

		verify(st).setObject(1, 1);
		verify(st).setObject(2, "name");
		verify(st).setObject(3, 42);
		verify(st).setInt(5, SRID);
		verify(st).setObject(6, 1);
		verify(st).executeUpdate();
	}

	@Test
	public void sqlExceptionOnDelete() throws Exception {
		when(conn.prepareStatement(anyString())).thenThrow(new SQLException());
		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));
		try {
			this.helper.delete(geojson);
			fail();
		} catch (SQLException e) {
		}

	}

	@Test
	public void deleteObjectWithMissingIdProperty() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{"f1", "f2"},
				new Object[]{"name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));

		try {
			this.helper.delete(geojson);
			fail();
		} catch (IOException e) {
		}
	}

	@Test
	public void deleteCorrectObject() throws Exception {
		PreparedStatement st = mock(PreparedStatement.class);
		when(conn.prepareStatement(anyString())).thenReturn(st);

		JSONObject geojson = geojson(new String[]{ID_COLUMN, "f1", "f2"},
				new Object[]{1, "name", 42,},
				this.gf.createPoint(new Coordinate(10, 10)));

		this.helper.delete(geojson);

		verify(st).setObject(1, 1);
		verify(st).executeUpdate();
	}

	private JSONObject geojson(String[] fields, Object[] values, Geometry geom)
			throws IOException {
		JSONObject geojson = new JSONObject();

		JSONObject properties = new JSONObject();
		for (int i = 0; i < fields.length; i++) {
			properties.element(fields[i], values[i]);
		}
		geojson.element("properties", properties);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new GeometryJSON().write(geom, bos);

		geojson.element("geometry", bos.toString());

		return geojson;
	}
}
