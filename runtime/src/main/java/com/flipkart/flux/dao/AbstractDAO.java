/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.dao;

import com.flipkart.flux.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 * @author shyam.akirala
 */
public class AbstractDAO<T> {

    public T findById(Class cls, Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(cls).add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        T castedObject = null;
        if(object != null)
            castedObject = (T) object;

        tx.commit();
        return castedObject;

    }

    public T persist(T object) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        session.persist(object);

        tx.commit();
        return object;
    }

    public T save(T object) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        session.save(object);

        tx.commit();
        return object;
    }

    public void update(T object) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        session.update(object);

        tx.commit();
    }

    public T saveOrUpdate(T object) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        session.saveOrUpdate(object);

        tx.commit();
        return object;
    }

    public void delete(T object) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        session.delete(object);

        tx.commit();
    }
}