package org.example.userservice.util;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class MockHibernateUtil {
    public static void mockShutdown() {
        try (MockedStatic<HibernateUtil> mocked = Mockito.mockStatic(HibernateUtil.class)) {
            mocked.when(HibernateUtil::shutdown).thenAnswer(invocation -> null);
        }
    }
}
