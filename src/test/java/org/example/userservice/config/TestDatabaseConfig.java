package org.example.userservice.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class TestDatabaseConfig {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine").withDatabaseName("userdb").withUsername("postgres").withPassword("test");

    private static SessionFactory sessionFactory;

    static {
        postgres.start();
        setupSessionFactory();
    }

    private static void setupSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
            configuration.setProperty("hibernate.connection.username", postgres.getUsername());
            configuration.setProperty("hibernate.connection.password", postgres.getPassword());
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");

            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test SessionFactory", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        postgres.stop();
    }
}
