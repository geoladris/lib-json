package de.csgis.commons.json;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.geotools.geojson.geom.GeometryJSON;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * A helper to insert, update and/or delete database rows from GeoJSON objects.
 * A helper instance is meant to work on a single table in a PostGIS database.
 */
public class GeojsonPGHelper {
	private static final String GEOJSON_PROPS = "properties";
	private static final String GEOJSON_GEOM = "geometry";

	private Connection conn;
	private String table;
	private String idColumn, geomColumn;
	private int srid;

	// Variables containing temporary values for building SQL queries
	private String fields, values;

	// Date management
	private SimpleDateFormat[] formats;

	/**
	 * Creates a new helper to insert, update and/or delete database rows from
	 * GeoJSON objects. A helper is meant to work on a single table in a PostGIS
	 * database.
	 * 
	 * @param table
	 *            The table to use for inserts, updates and deletes.
	 * @param idColumn
	 *            The name of the primary key column. The helper does not
	 *            support primary keys with more than one column.
	 * @param geomColumn
	 *            The name of the geometry column. The helper does not support
	 *            more than one geometry column.
	 * @param srid
	 *            The SRID for the geometries.
	 */
	public GeojsonPGHelper(String table, String idColumn, String geomColumn,
			int srid) {
		super();
		this.idColumn = idColumn;
		this.geomColumn = geomColumn;
		this.table = table;
		this.srid = srid;

		this.formats = new SimpleDateFormat[]{
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
				new SimpleDateFormat("yyyy-MM-ddX")};
		for (SimpleDateFormat format : this.formats) {
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
	}

	/**
	 * Sets the connection to use <b>before</b> calling
	 * {@link #insert(JSONObject)}, {@link #update(JSONObject)} and/or
	 * {@link #delete(JSONObject)}.
	 * 
	 * @param connection
	 *            The connection to use when inserting/updating/deleting
	 *            objects.
	 */
	public void setConnection(Connection connection) {
		this.conn = connection;
	}

	/**
	 * Inserts the given object in the database.
	 * 
	 * @param geojson
	 *            The object to insert.
	 * @throws SQLException
	 *             if the object cannot be updated.
	 * @throws IOException
	 *             if the geometry contained in the GeoJSON cannot be translated
	 *             into WKT.
	 * @throws NullPointerException
	 *             if the connection has not been previously configured by
	 *             calling {@link #setConnection(Connection)}.
	 */
	public void insert(JSONObject geojson) throws SQLException, IOException {
		processFields(geojson);
		String sql = "INSERT INTO " + this.table + " (" + this.fields
				+ ") VALUES (" + this.values + ")";
		PreparedStatement st = prepareStatement(geojson, sql);
		st.executeUpdate();
	}

	/**
	 * Updates the given object in the database. The <code>idField</code>
	 * property is used for the <code>WHERE</code> clause to update only the
	 * specific object.
	 * 
	 * @param geojson
	 *            The object to update.
	 * @throws SQLException
	 *             if the object cannot be updated.
	 * @throws IOException
	 *             if the geometry contained in the GeoJSON cannot be translated
	 *             into WKT or the GeoJSON object does not have a
	 *             {@link #idColumn} property.
	 * @throws NullPointerException
	 *             if the connection has not been previously configured by
	 *             calling {@link #setConnection(Connection)}.
	 */
	public void update(JSONObject geojson) throws SQLException, IOException {
		processFields(geojson);
		String sql = "UPDATE " + this.table + " SET (" + this.fields + ") = ("
				+ this.values + ") WHERE " + idColumn + " = ?";
		PreparedStatement st = prepareStatement(geojson, sql);
		JSONObject properties = geojson.getJSONObject(GEOJSON_PROPS);
		Object id = properties.get(idColumn);
		if (id == null) {
			throw new IOException(
					"GeoJSON missing id('" + idColumn + "') property");
		}

		// +1 because index starts at 1; +2 because of geom and srid
		st.setObject(properties.size() + 3, id);
		st.executeUpdate();
	}

	/**
	 * It deletes the given object from the database.
	 * 
	 * @param geojson
	 *            The GeoJSON object to delete. Only the <code>idField</code>
	 *            (see {@link #GeojsonPGHelper(String, String, String, int)})
	 *            property is used.
	 * @throws SQLException
	 *             if the object cannot be deleted.
	 * @throws IOException
	 *             if the GeoJSON object does not have an {@link #idColumn}
	 *             property.
	 * @throws NullPointerException
	 *             if the connection has not been previously configured by
	 *             calling {@link #setConnection(Connection)}.
	 */
	public void delete(JSONObject geojson) throws SQLException, IOException {
		String sql = "DELETE FROM " + this.table + " WHERE " + this.idColumn
				+ " = ?";
		PreparedStatement st = this.conn.prepareStatement(sql);
		Object id = geojson.getJSONObject(GEOJSON_PROPS).get(this.idColumn);
		if (id == null) {
			throw new IOException(
					"GeoJSON missing id('" + idColumn + "') property");
		}

		st.setObject(1, id);
		st.executeUpdate();
	}

	private void processFields(JSONObject geojson) {
		JSONObject properties = geojson.getJSONObject("properties");

		this.values = "";
		this.fields = "";
		for (Object key : properties.keySet()) {
			this.fields += key.toString() + ", ";
			this.values += "?, ";
		}

		this.fields += geomColumn;
		this.values += "ST_GeomFromText(?, ?)";
	}

	private PreparedStatement prepareStatement(JSONObject geojson, String sql)
			throws SQLException, IOException {
		PreparedStatement st = this.conn.prepareStatement(sql);
		JSONObject properties = geojson.getJSONObject(GEOJSON_PROPS);

		int j = 1;
		for (Object key : properties.keySet()) {
			Object value = properties.get(key);
			Date date = null;
			for (SimpleDateFormat format : this.formats) {
				try {
					date = format.parse(value.toString());
					break;
				} catch (ParseException e) {
				}
			}

			if (date != null) {
				st.setDate(j++, new java.sql.Date(date.getTime()));
			} else {
				st.setObject(j++, value);
			}
		}

		String geoJsonGeom = geojson.getJSONObject(GEOJSON_GEOM).toString();
		Geometry geom = new GeometryJSON().read(geoJsonGeom);

		if (geom == null) {
			throw new IOException("Invalid GeoJSON geometry");
		}

		st.setString(j++, geom.toText());
		st.setInt(j++, srid);

		return st;
	}

	public String getTable() {
		return table;
	}
}
