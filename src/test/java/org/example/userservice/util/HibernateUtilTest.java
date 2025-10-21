package org.example.userservice.util;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

@Disabled("HibernateUtil tests require database configuration")
class HibernateUtilTest {

    @Test
    void getSessionFactory_ShouldReturnSessionFactory() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        assertNotNull(sessionFactory);
    }

    @Test
    void shutdown_ShouldNotThrowException() {
        assertDoesNotThrow(() -> HibernateUtil.shutdown());
    }

    @Test
    void getSessionFactory_ShouldReturnSameInstance() {
        SessionFactory firstInstance = HibernateUtil.getSessionFactory();
        SessionFactory secondInstance = HibernateUtil.getSessionFactory();

        assertSame(firstInstance, secondInstance);
    }

    @Test
    void hibernateUtil_ClassShouldLoad() {
        assertDoesNotThrow(() -> Class.forName("org.example.userservice.util.HibernateUtil"));
    }

    @Test
    void buildSessionFactory_ShouldInitializeOnFirstCall() {
        assertNotNull(HibernateUtil.getSessionFactory());
    }

}
