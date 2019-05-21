package com.github.ideahut.sbms.shared.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

import com.github.ideahut.sbms.client.dto.PageDto;
import com.github.ideahut.sbms.shared.dao.DaoBase;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.entity.EntitySoftDelete;
import com.github.ideahut.sbms.shared.hibernate.CriteriaVisitor;
import com.github.ideahut.sbms.shared.hibernate.ExampleHelper;
import com.github.ideahut.sbms.shared.hibernate.OrderSpec;
import com.github.ideahut.sbms.shared.hibernate.OrderSpecTranslator;

@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
public abstract class DaoBaseImpl<ET extends EntityBase<ID>, ID extends Serializable> implements DaoBase<ET, ID> {
	
	@Autowired(required = false)
	private SessionFactory sessionFactory;
	
	
	private Class<ET> entityClass;
	
	private final List<CriteriaVisitor<ET>> visitors = new LinkedList<CriteriaVisitor<ET>>();
	
	
	public DaoBaseImpl() {
		Type[] actualTypeArguments = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments();
		entityClass = (Class<ET>) actualTypeArguments[0];
	}

	
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	private Criteria createOrder(Criteria criteria, OrderSpec orderSpec) {
		if (null != orderSpec) {
			List<Order> orderList = OrderSpecTranslator.asOrders(orderSpec);
			for (Order order : orderList) {
				criteria.addOrder(order);
			}
		}
		return criteria;
	}
	
	private <T> PageDto<T> executeFind(PageDto<T> page, Query query, Class<T> transformTo) {
		if (null != transformTo) {
			query.setResultTransformer(Transformers.aliasToBean(transformTo));
		}
		return paginated(page, query);
	}

	private <T> PageDto<T> executeFind(PageDto<T> page, Query query, Query queryRowCount, Class<T> transformTo) {
		if (null != transformTo) {
			query.setResultTransformer(Transformers.aliasToBean(transformTo));
		}
		return paginated(page, query, queryRowCount);
	}

	private int scrollResults(List dataList, int firstRowIdx, int rowSize, ScrollableResults sr) {
		if (sr.setRowNumber(firstRowIdx)) {
			int i = 0;
			do {
				dataList.add(sr.get(0));
			} while (++i < rowSize && sr.next());
		}
		sr.last();
		return sr.getRowNumber() + 1;
	}
	
	private int getFirstRowIndex(int index, int size) {
		return (int)((index - 1) * size);
	}

	private <T> PageDto<T> paginated(PageDto<T> page, Query query) {
		List<T> data = new ArrayList<T>();
		ScrollableResults sr = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
		int startIdx = getFirstRowIndex(page.getIndex(), page.getSize());
		long records = scrollResults(data, startIdx, page.getSize(), sr);
		sr.close();
		if (0 < records) {
			page.setRecords(records);
			page.setData(data);
			return page;
		}
		return PageDto.createEmpty();
	}

	private <T> PageDto<T> paginated(PageDto<T> page, Query query, Query queryRowCount) {
		int records = ((Long) queryRowCount.uniqueResult()).intValue();
		int startIdx = getFirstRowIndex(page.getIndex(), page.getSize());
		query.setFirstResult(startIdx);
		query.setMaxResults(page.getSize());
		List<T> data = (List<T>) query.list();
		if (0 < records) {
			page.setRecords((long) records);
			page.setData(data);
			return page;
		}
		return PageDto.createEmpty();
	}

	private <T> PageDto<T> paginated(PageDto<T> page, Criteria criteria) {
		List<T> data = new ArrayList<T>();
		int startIdx = getFirstRowIndex(page.getIndex(), page.getSize());
		ScrollableResults sr = criteria.scroll(ScrollMode.SCROLL_INSENSITIVE);
		long records = scrollResults(data, startIdx, page.getSize(), sr);
		sr.close();
		if (0 < records) {
			page.setRecords(records);
			page.setData(data);
			return page;
		}
		return PageDto.createEmpty();
	}

	private <T> PageDto<T> executeFind(PageDto<T> page, Criteria criteria, Class<T> transformTo) {
		if (null != transformTo) {
			criteria.setResultTransformer(Transformers.aliasToBean(transformTo));
		}
		return paginated(page, criteria);
	}
	
	
	

	
	protected List<CriteriaVisitor<ET>> getVisitors() {
		return visitors;
	}

	protected Criteria createCriteria(Class<?> clazz) {
		return createCriteria(clazz, null);
	}
	
	protected Criteria createCriteriaWithAlias(Class<?> clazz, String alias) {
		return getSession().createCriteria(clazz, alias);
	}

	protected Criteria createCriteria(Class<?> clazz, OrderSpec orderSpec) {
		Criteria criteria = createOrder(getSession().createCriteria(clazz), orderSpec);
		if (EntitySoftDelete.class.isAssignableFrom(clazz)) {
			criteria.add(Restrictions.isNull("deleted"));
		}
		return criteria;
	}

	protected Criteria createCriteria(ET example, OrderSpec orderSpec, String... excludedProperties) {
		Criteria criteria = createCriteria(example.getClass(), orderSpec);
		if (example != null) {
			Example ex = ExampleHelper.createExample(example, excludedProperties);
			List<CriteriaVisitor<ET>> v = getVisitors();
			for (CriteriaVisitor<ET> criteriaVisitor : v) {
				criteriaVisitor.visit(criteria, example, excludedProperties);
			}
			criteria.add(ex.enableLike(MatchMode.ANYWHERE).ignoreCase());
		}
		return criteria;
	}	

	protected Query createQuery(String query) {
		return createQuery(query, null);
	}

	protected Query createQuery(String query, OrderSpec orderSpec) {
		if (null == orderSpec) {
			return getSession().createQuery(query);
		}
		return getSession().createQuery(query + " order by " + OrderSpecTranslator.asString(orderSpec));
	}
	
	protected SQLQuery createSQLQuery(String query) {
		return getSession().createSQLQuery(query);
	}

	protected String buildSearchText(String searchText) {
		return buildSearchText(searchText, true);
	}

	protected String buildSearchText(String searchText, boolean enableLike) {
		String s = null == searchText ? "" : searchText.toUpperCase();
		if (enableLike) {
			return "%" + s + "%";
		}
		return s;
	}
	
	protected List<ET> find(Query query) {
		return query.list();
	}

	protected <T> PageDto<T> find(PageDto<T> page, Query query) {
		return find(page, query, null);
	}

	protected <T> PageDto<T> find(PageDto<T> page, Query query, Class<T> transformTo) {
		return executeFind(page, query, transformTo);
	}

	protected <T> PageDto<T> find(PageDto<T> page, Query query, Query queryRowCount, Class<T> transformTo) {
		return executeFind(page, query, queryRowCount, transformTo);
	}

	protected List<ET> find(Criteria criteria) {
		return criteria.list();
	}

	protected List<ET> find(Criteria criteria, OrderSpec orderSpec) {
		createOrder(criteria, orderSpec);
		return criteria.list();
	}

	protected <T> PageDto<T> find(PageDto<T> page, Criteria criteria) {
		return executeFind(page, criteria, null);
	}

	protected <T> PageDto<T> find(PageDto<T> page, Criteria criteria, Class<T> transformTo) {
		return executeFind(page, criteria, transformTo);
	}

	protected <T> PageDto<T> find(PageDto<T> page, Criteria criteria, OrderSpec orderSpec, Class<T> transformTo) {
		createOrder(criteria, orderSpec);
		return executeFind(page, criteria, transformTo);
	}
	
	protected ET delete(ET entity) {
		if (entity == null) {
			return null;
		}
		if (EntitySoftDelete.class.isAssignableFrom(entity.getClass())) {
			((EntitySoftDelete) entity).setDeleted(new Boolean(true));
			save(entity);
		} else {
			getSession().delete(entity);
		}
		return entity;
	}
	

	public ET get(ID id) {
		return (ET) getSession().get(entityClass, id);
	}

	public ET save(ET entity) {
		try {
			getSession().saveOrUpdate(entity);
			return entity;
		} catch (HibernateException e) {
			throw SessionFactoryUtils.convertHibernateAccessException(e);
		}
	}

	public ET delete(ID id) {
		try {
			ET entity = (ET) getSession().load(entityClass, id);
			return delete(entity);
		} catch (HibernateException e) {
			throw SessionFactoryUtils.convertHibernateAccessException(e);
		}
	}

	public List<ET> find(ET entity, String... excludedProperties) {
		return createCriteria(entity, null, excludedProperties).list();
	}

	public List<ET> find(ET entity, OrderSpec orderSpec, String... excludedProperties) {
		return createCriteria(entity, orderSpec, excludedProperties).list();
	}

	public PageDto<ET> find(PageDto<ET> page, ET entity, String... excludedProperties) {
		return executeFind(page, createCriteria(entity, null, excludedProperties), null);
	}

	public PageDto<ET> find(PageDto<ET> page, ET entity, OrderSpec orderSpec, String... excludedProperties) {
		return executeFind(page, createCriteria(entity, orderSpec, excludedProperties), null);
	}

	public boolean isExists(ID id) {
		return get(id) != null;
	}
}