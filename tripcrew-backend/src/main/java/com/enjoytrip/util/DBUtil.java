package com.enjoytrip.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final String DRIVER;
    private static final String URL;
    private static final String USER;
    private static final String PWD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties를 찾을 수 없습니다.");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("db.properties 로드 실패", e);
        }
        DRIVER = props.getProperty("db.driver");
        URL    = props.getProperty("db.url");
        USER   = props.getProperty("db.user");
        PWD    = props.getProperty("db.password");

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC 드라이버 로드 실패", e);
        }
    }

   
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignored) {}
            }
        }
    }
}
