package org.grapple.query.impl;

import javax.persistence.EntityManager;

import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryProvider;

interface QueryProviderImpl extends QueryProvider {

    EntityManager getEntityManager();

    QueryBuilder getQueryBuilder(QueryBuilder source);
}
