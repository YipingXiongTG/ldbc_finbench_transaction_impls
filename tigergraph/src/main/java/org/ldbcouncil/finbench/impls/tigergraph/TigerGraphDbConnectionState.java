package org.ldbcouncil.finbench.impls.tigergraph;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ldbcouncil.finbench.driver.DbConnectionState;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class TigerGraphDbConnectionState extends DbConnectionState {
    static Logger logger = LogManager.getLogger("TigerGraphDbConnectionState");
    private static HikariDataSource dataSource; // Singleton instance

    private Connection conn;

    public TigerGraphDbConnectionState(Map<String, String> properties) throws IOException {
        if (dataSource == null) {
            initializeDataSource(properties);
        }

        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection from the connection pool.", e);
        }
    }

    private void initializeDataSource(Map<String, String> properties) {
        String ipAddr = properties.get("ipAddr");
        String port = properties.get("port");
        String user = properties.get("user");
        String pass = properties.get("pass");
        String graph = properties.get("graph");

        HikariConfig config = new HikariConfig();
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:tg:http://").append(ipAddr).append(":").append(port);
        config.setJdbcUrl(sb.toString());
        config.setDriverClassName("com.tigergraph.jdbc.Driver");
        config.setUsername(user);
        config.setPassword(pass);
        config.addDataSourceProperty("graph", graph);
        config.addDataSourceProperty("debug", 0);

        dataSource = new HikariDataSource(config);
    }

    public Connection getConn() {
        return conn;
    }

    @Override
    public void close() throws IOException {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to close connection", e);
        }

        // Close the data source after all connections have been closed
        if (dataSource != null) {
            dataSource.close();
        }
    }
}