import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Mini SQL runner - 不依赖任何数据库客户端,纯 JDBC 执行 SQL 脚本。
 * - 处理 --  / #  / *!* MySQL 条件注释之外的 /* *!/ 块注释
 * - 处理字符串/反引号字面量内的分号
 * - 单条执行,失败不中断,只统计
 */
public class RunSql {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: RunSql <sql-file> <jdbc-url> <user> <pass>");
            System.exit(2);
        }
        String file  = args[0];
        String url   = args[1];
        String user  = args[2];
        String pass  = args[3];

        // 增加必要的连接参数(若已存在则忽略 - MySQL connector 会解析同名参数取最后一个非空)
        if (!url.contains("useUnicode"))   url += (url.contains("?") ? "&" : "?") + "useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowMultiQueries=true";
        if (!url.contains("allowMultiQueries")) url += "&allowMultiQueries=true";

        Class.forName("com.mysql.cj.jdbc.Driver");
        String sql = Files.readString(Paths.get(file), StandardCharsets.UTF_8);
        // 移除 BOM
        if (!sql.isEmpty() && sql.charAt(0) == '\uFEFF') sql = sql.substring(1);

        List<String> stmts = split(sql);

        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement()) {

            int ok = 0, fail = 0;
            int idx = 0;
            for (String st : stmts) {
                if (st.trim().isEmpty()) continue;
                idx++;
                try {
                    s.execute(st);
                    ok++;
                } catch (Exception e) {
                    fail++;
                    String preview = st.length() > 240 ? st.substring(0, 240) + "..." : st;
                    // 替换换行方便看
                    preview = preview.replace('\n', ' ').replace('\r', ' ');
                    System.err.println("[FAIL #" + idx + " sql=" + fail + "] " + e.getMessage());
                    System.err.println("  >> " + preview);
                }
            }
            System.out.println("=== OK=" + ok + "  FAIL=" + fail + "  TOTAL=" + (ok + fail) + " ===");

            // 验证下建出来的表数量
            try (ResultSet rs = c.createStatement().executeQuery(
                    "SELECT TABLE_NAME, IFNULL(TABLE_ROWS,0) r FROM information_schema.tables " +
                    "WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME")) {
                System.out.println("--- 当前数据库表清单 ---");
                int n = 0;
                while (rs.next()) {
                    System.out.println("  " + String.format("%-40s", rs.getString(1)) + " rows~" + rs.getLong(2));
                    n++;
                }
                System.out.println("--- 共 " + n + " 张表 ---");
            }
        }
    }

    /** 简易 SQL 切分:按 ; 切,保留 MySQL 条件注释 /*! ... * /,忽略其他注释和字符串内的分号 */
    static List<String> split(String sql) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inS = false, inD = false, inB = false, inLine = false, inBlock = false;
        int n = sql.length();
        for (int i = 0; i < n; i++) {
            char c = sql.charAt(i);
            char nx = (i + 1 < n) ? sql.charAt(i + 1) : '\0';

            if (inLine) {
                if (c == '\n') { inLine = false; sb.append('\n'); }
                continue;
            }
            if (inBlock) {
                if (c == '*' && nx == '/') { inBlock = false; i++; }
                continue;
            }
            if (inS) {
                sb.append(c);
                if (c == '\\' && nx != '\0') { sb.append(nx); i++; }
                else if (c == '\'' && nx == '\'') { sb.append(nx); i++; }
                else if (c == '\'') inS = false;
                continue;
            }
            if (inD) {
                sb.append(c);
                if (c == '\\' && nx != '\0') { sb.append(nx); i++; }
                else if (c == '"') inD = false;
                continue;
            }
            if (inB) {
                sb.append(c);
                if (c == '`') {
                    // 反引号允许 `` 转义
                    if (nx == '`') { sb.append(nx); i++; }
                    else inB = false;
                }
                continue;
            }

            // 注释起始
            if (c == '-' && nx == '-') { inLine = true; i++; continue; }
            if (c == '#')              { inLine = true; continue; }
            if (c == '/' && nx == '*') {
                // MySQL 条件注释 /*! ... */ - 保留内容
                if (i + 2 < n && sql.charAt(i + 2) == '!') {
                    sb.append(c);  // 保留 / 让后续 MySQL 当 /*! 识别
                    continue;
                }
                inBlock = true; i++; continue;
            }

            if (c == '\'') { inS = true; sb.append(c); continue; }
            if (c == '"')  { inD = true; sb.append(c); continue; }
            if (c == '`')  { inB = true; sb.append(c); continue; }

            if (c == ';') { list.add(sb.toString()); sb.setLength(0); continue; }
            sb.append(c);
        }
        if (sb.length() > 0) list.add(sb.toString());
        return list;
    }
}
