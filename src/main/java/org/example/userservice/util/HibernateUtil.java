package org.example.userservice.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            logger.info("Creating SessionFactory");
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            SessionFactory factory = configuration.buildSessionFactory();
            logger.info("SessionFactory created successfully");
            return factory;
        } catch (Throwable e) {
            logger.error(e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        logger.info("Shutting down SessionFactory");
        getSessionFactory().close();
        logger.info("SessionFactory closed successfully");
    }
}
