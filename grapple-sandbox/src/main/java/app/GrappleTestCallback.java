package app;

import javax.persistence.EntityManager;

public interface GrappleTestCallback {

    void execute(EntityManager entityManager) throws Exception;

}
