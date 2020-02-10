package org.grapple.junit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class SchemaTestSupport {

    private static EntityManagerFactory entityManagerFactory;

    public EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            SchemaTestSupport.entityManagerFactory = Persistence.createEntityManagerFactory("grapple-sandbox");
        }
        return entityManagerFactory.createEntityManager();
    }
}
