package base;

import helper.PropertyProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class MainBase {

    private static String url = PropertyProvider.getInstance().getProperty("db.url");
    private static String username = PropertyProvider.getInstance().getProperty("db.user");
    private static String password = PropertyProvider.getInstance().getProperty("db.password");

    public static void getPostById(Integer id) {
        String getPostById = "select ID, post_title from wp_posts where ID = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(getPostById)) {
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("ID");
                String title = resultSet.getString("post_title");
                System.out.println("PostId " + postId + " title: " + title);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> row(String sql, Object... params) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = prepare(connection, sql, params);
             ResultSet rs = preparedStatement.executeQuery()) {

            if (!rs.next()) {
                return Collections.emptyMap();
            }
            return toMap(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long insertAndGetId(String sql, Object... params) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (nonNull(params)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            preparedStatement.executeUpdate();
            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return 0L;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String sql, Object... params) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = prepare(connection, sql, params)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepare(Connection connection, String sql, Object... params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (nonNull(params)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> toMap(ResultSet resultSet) {
        try {
            ResultSetMetaData md = resultSet.getMetaData();
            Map<String, Object> map = new LinkedHashMap<>(md.getColumnCount());
            for (int i = 1; i <= md.getColumnCount(); i++) {
                map.put(md.getColumnLabel(i), resultSet.getObject(i));
            }
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
