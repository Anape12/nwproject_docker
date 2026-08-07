package jp.nw.parts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class DBBase {

	String host = System.getenv("DB_HOST");
	String db = System.getenv("DB_NAME");

	String URL = "jdbc:mysql://" + host + ":3306/" + db
			+ "?connectionTimeZone=LOCAL" +
			"&forceConnectionTimeZoneToSession=true" +
			"&preserveInstants=false" +
			"&allowPublicKeyRetrieval=true" +
			"&useSSL=false";

	private final String USER = "root";
	private final String PASSWORD = "root";

	private Connection con;

	public DBBase() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.con = DriverManager.getConnection(
					URL,
					USER,
					PASSWORD);

			try (Statement st = con.createStatement()) {
            	st.execute("SET time_zone = '+09:00'");
        	}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Connection getConnection() {
		return this.con;
	}

	public Object execute(Query query) {

		try {
			SqlBuilder builder = new SqlBuilder();
			String sql = builder.build(query);

			PreparedStatement ps = con.prepareStatement(sql);

			this.con = DriverManager.getConnection(URL, USER, PASSWORD);



			bindParameter(ps, query);

			switch (query.getSqlType()) {
				case SELECT:
					ResultSet rs = ps.executeQuery();
					return getResultList(rs);
				default:
					return ps.executeUpdate();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <T> List<T> execute(Query query, Class<T> clazz) {

		try {
			SqlBuilder builder = new SqlBuilder();
			String sql = builder.build(query);

			PreparedStatement ps = con.prepareStatement(sql);

			bindParameter(ps, query);

			ResultSet rs = ps.executeQuery();

			ResultMapper resultMapper = new ResultMapper();

			List<Map<String, Object>> rows =
					resultMapper.toList(rs);

			EntityMapper entityMapper = new EntityMapper();

			return entityMapper.toEntityList(rows, clazz);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public long executeInsert(Query query) {

		try {
			SqlBuilder builder = new SqlBuilder();
			String sql = builder.build(query);

			PreparedStatement ps = con.prepareStatement(
					sql,
					Statement.RETURN_GENERATED_KEYS);

			bindParameter(ps, query);

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();

			if (rs.next()) {
				return rs.getLong(1);
			}

			throw new RuntimeException("Generated key not found.");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * PreparedStatementへ値を設定
	 */
	private void bindParameter(
			PreparedStatement ps,
			Query query) throws Exception {

		int index = 1;

		switch (query.getSqlType()) {

			case INSERT:
				for (Object value : query.getValues().values()) {
					ps.setObject(index++, value);
				}
				break;
			case UPDATE:
				for (Object value : query.getValues().values()) {
					ps.setObject(index++, value);
				}
				for (Object value : query.getConditions().values()) {
					ps.setObject(index++, value);
				}
				break;
			case DELETE:
			case SELECT:
				for (Object value : query.getConditions().values()) {
					ps.setObject(index++, value);
				}
				break;
		}
	}

	/**
	 * ResultSet → List<Map>
	 */
	private List<Map<String, Object>> getResultList(ResultSet rs) throws Exception {

		ResultMapper mapper = new ResultMapper();

		return mapper.toList(rs);
	}

}