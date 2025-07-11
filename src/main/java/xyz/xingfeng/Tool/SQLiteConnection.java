package xyz.xingfeng.Tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection {
    private static String dbPath = "data.db"; // 默认数据库路径

    // 私有构造方法防止实例化
    private SQLiteConnection() {}

    /**
     * 设置数据库路径
     * @param path 数据库文件路径
     */
    public static void setDbPath(String path) {
        dbPath = path;
    }

    /**
     * 获取数据库连接，如果数据库文件不存在会自动创建
     * @return Connection对象
     * @throws SQLException 如果连接失败
     */
    public static Connection getConnection() throws SQLException {
        try {
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }

        // 创建连接字符串
        String url = "jdbc:sqlite:" + dbPath;

        // 检查数据库文件是否已存在
        java.io.File dbFile = new java.io.File(dbPath);
        boolean isNewDatabase = !dbFile.exists();

        // 获取连接（如果数据库文件不存在会自动创建）
        Connection conn = DriverManager.getConnection(url);

        if (isNewDatabase) {
            String sql = """
                    CREATE TABLE cookies (
                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                          name TEXT NOT NULL,
                          value TEXT NOT NULL,
                          domain TEXT,
                          path TEXT DEFAULT '/',
                          expires INTEGER,           -- UNIX timestamp
                          max_age INTEGER,           -- in seconds
                          secure BOOLEAN DEFAULT 0,
                          http_only BOOLEAN DEFAULT 0,
                          same_site TEXT,            -- 'Strict'/'Lax'/'None'
                          creation_time INTEGER DEFAULT (strftime('%s', 'now')),
                          UNIQUE(name, domain, path)
                      );
                    """;
            initTables(conn,sql);
        }

        // 检查是否成功连接
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON"); // 启用外键支持
            stmt.execute("PRAGMA journal_mode = WAL"); // 设置WAL模式提高性能
        }

        return conn;
    }



    /**
     * 初始化数据库表结构（如果表不存在则创建）
     * @param conn 数据库连接
     * @param createTableSQLs 创建表的SQL语句
     * @throws SQLException 如果执行SQL出错
     */
    public static void initTables(Connection conn, String... createTableSQLs) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 执行每条建表SQL
            for (String sql : createTableSQLs) {
                stmt.execute(sql);
            }
        }
    }

    /**
     * 关闭连接
     * @param conn 要关闭的连接
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }

    /**
     * 关闭多个资源对象
     * @param resources 可变参数，可传入多个AutoCloseable对象
     */
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    if (resource instanceof Connection) {
                        if (!((Connection) resource).isClosed()) {
                            resource.close();
                        }
                    } else {
                        resource.close();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to close resource: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 测试连接是否有效
     * @return true如果连接成功，false如果失败
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        } finally {
            close(conn);
        }
    }
}
