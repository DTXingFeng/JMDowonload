package xyz.xingfeng.Tool;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CookieManager {
    private static final SimpleDateFormat EXPIRES_DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);

    static {
        EXPIRES_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 解析 Set-Cookie 头部并存储到数据库
     * @param setCookieHeader Set-Cookie 头部字符串
     * @param domain 当前域名（用于设置cookie的domain属性）
     */
    public static void parseAndStoreCookie(String setCookieHeader, String domain) {
        Connection conn = null;
        try {
            conn = SQLiteConnection.getConnection();

            // 解析cookie各部分
            String[] parts = setCookieHeader.split(";");
            String[] nameValue = parts[0].trim().split("=", 2);
            if (nameValue.length != 2) {
                throw new IllegalArgumentException("Invalid cookie format: " + setCookieHeader);
            }

            String name = nameValue[0].trim();
            String value = nameValue[1].trim();
            String path = "/";
            Long expires = null;
            Integer maxAge = null;
            boolean secure = false;
            boolean httpOnly = false;
            String sameSite = null;

            // 解析属性
            for (int i = 1; i < parts.length; i++) {
                String attr = parts[i].trim().toLowerCase();
                if (attr.startsWith("path=")) {
                    path = attr.substring(5).trim();
                } else if (attr.startsWith("expires=")) {
                    String dateStr = attr.substring(8).trim();
                    try {
                        Date date = EXPIRES_DATE_FORMAT.parse(dateStr);
                        expires = date.getTime() / 1000; // 转为Unix时间戳
                    } catch (ParseException e) {
                        System.err.println("Failed to parse expires date: " + dateStr);
                    }
                } else if (attr.startsWith("max-age=")) {
                    try {
                        maxAge = Integer.parseInt(attr.substring(8).trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid max-age: " + attr);
                    }
                } else if (attr.equals("secure")) {
                    secure = true;
                } else if (attr.equals("httponly")) {
                    httpOnly = true;
                } else if (attr.startsWith("samesite=")) {
                    sameSite = attr.substring(9).trim().toLowerCase();
                    if (!sameSite.equals("strict") && !sameSite.equals("lax") && !sameSite.equals("none")) {
                        sameSite = null;
                    }
                }
            }

            // 计算最终过期时间（优先使用max-age）
            Long finalExpires = null;
            if (maxAge != null) {
                finalExpires = (System.currentTimeMillis() / 1000) + maxAge;
            } else if (expires != null) {
                finalExpires = expires;
            }

            // 存储到数据库
            storeCookie(conn, name, value, domain, path, finalExpires, maxAge, secure, httpOnly, sameSite);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } finally {
            SQLiteConnection.close(conn);
        }
    }

    private static void storeCookie(Connection conn, String name, String value, String domain,
                                    String path, Long expires, Integer maxAge, boolean secure,
                                    boolean httpOnly, String sameSite) throws SQLException {
        // 使用REPLACE策略处理冲突（相同name+domain+path的cookie）
        String sql = "INSERT OR REPLACE INTO cookies (name, value, domain, path, expires, " +
                "max_age, secure, http_only, same_site) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, value);
            pstmt.setString(3, domain);
            pstmt.setString(4, path);
            if (expires != null) {
                pstmt.setLong(5, expires);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            if (maxAge != null) {
                pstmt.setInt(6, maxAge);
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setBoolean(7, secure);
            pstmt.setBoolean(8, httpOnly);
            pstmt.setString(9, sameSite);

            pstmt.executeUpdate();
        }
    }

    public static String getCookie(String path){
        try (Connection connection = SQLiteConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("select name,value from cookies where path = '/'")){
            // 获取结果集的元数据（列信息）
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 遍历列名并添加到请求头
            StringBuilder sb = new StringBuilder();
            if (resultSet.next()) {  // 先移动光标到第一行
                do {
                    String name = resultSet.getString("name");
                    String value = resultSet.getString("value");
                    sb.append(name).append("=").append(value);

                    // 如果不是最后一行，则添加分号
                    if (!resultSet.isAfterLast()) {  // 也可以用 resultSet.next() 的返回值判断
                        sb.append("; ");
                    }
                } while (resultSet.next());  // 移动到下一行
            }
            return sb.toString();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }


}

