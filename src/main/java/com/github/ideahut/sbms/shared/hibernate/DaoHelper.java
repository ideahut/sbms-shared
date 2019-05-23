package com.github.ideahut.sbms.shared.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.query.Query;

import com.github.ideahut.sbms.client.dto.PageDto;
import com.github.ideahut.sbms.shared.entity.EntitySoftDelete;

@Deprecated
@SuppressWarnings({"rawtypes", "unchecked"})
public class DaoHelper {
	
	private DaoHelper() {}
	
	/*
	 * CRITERIA
	 */
	public static Criteria createCriteria(Session session, Class clazz) {
		return createCriteria(session, clazz, null);
	}
	
	public static Criteria createCriteria(Session session, Class clazz, OrderSpec orderSpec) {
		Criteria criteria = createOrder(session.createCriteria(clazz), orderSpec);
		if (EntitySoftDelete.class.isAssignableFrom(clazz)) {
			criteria.add(Restrictions.isNull("deleted"));
		}
		return criteria;
	}

	public static Criteria createCriteria(Session session, Class clazz, Object example, OrderSpec orderSpec, CriteriaVisitor visitor, String... excludedProperties) {
		Criteria criteria = createCriteria(session, clazz, orderSpec);
		if (example != null) {
			Example ex = ExampleHelper.createExample(example, excludedProperties);
			if (visitor != null) {
				visitor.visit(criteria, example, excludedProperties);
			}
			criteria.add(ex.enableLike(MatchMode.ANYWHERE).ignoreCase());
		}
		return criteria;
	}
	
	
	/*
	 * ORDER
	 */
	public static Criteria createOrder(Criteria criteria, OrderSpec orderSpec) {
		if (null != orderSpec) {
			List<Order> orderList = OrderSpecTranslator.asOrders(orderSpec);
			for (Order order : orderList) {
				criteria.addOrder(order);
			}
		}
		return criteria;
	}

	
	/*
	 * QUERY
	 */
	public static Query createQuery(Session session, String query) {
		return createQuery(session,query, null);
	}

	public static Query createQuery(Session session, String query, OrderSpec orderSpec) {
		if (null == orderSpec) {
			return session.createQuery(query);
		}
		return session.createQuery(query + " order by " + OrderSpecTranslator.asString(orderSpec));
	}
	
	public static SQLQuery createSQLQuery(Session session, String query) {
		return session.createSQLQuery(query);
	}
	
	
	/*
	 * PAGE
	 */
	private static long scrollResults(List dataList, int firstRowIdx, int rowSize, ScrollableResults sr) {
		if (sr.setRowNumber(firstRowIdx)) {
			int i = 0;
			do {
				dataList.add(sr.get(0));
			} while (++i < rowSize && sr.next());
		}
		sr.last();
		return sr.getRowNumber() + 1;
	}
	
	private static int getFirstRowIndex(int index, int size) {
		return (int)((index - 1) * size);
	}
	
	public static<T> PageDto<T> paginated(PageDto<T> page, Query query) {
		List<T> data = new ArrayList<T>();
		ScrollableResults sr = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
		int startIdx = getFirstRowIndex(page.getIndex(), page.getSize());
		long records = scrollResults(data, startIdx, page.getSize(), sr);
		sr.close();
		if (0 < records) {
			page.setRecords(records);
			page.setData(data);
		}
		return page;
	}

	public static<T> PageDto<T> paginated(PageDto<T> page, Criteria criteria) {
		List<T> data = new ArrayList<T>();
		int startIdx = getFirstRowIndex(page.getIndex(), page.getSize());
		ScrollableResults sr = criteria.scroll(ScrollMode.SCROLL_INSENSITIVE);
		long records = scrollResults(data, startIdx, page.getSize(), sr);
		sr.close();
		if (0 < records) {
			page.setRecords(records);
			page.setData(data);
		}
		return page;
	}
	
	
	/*
	 * NATIVE SQL
	 */
	public static NativeSQL getNativeSQL(Criteria criteria) {
		CriteriaImpl criteriaImpl = (CriteriaImpl)criteria;
		SharedSessionContractImplementor session = criteriaImpl.getSession();
		SessionFactoryImplementor factory = criteriaImpl.getSession().getFactory();
		String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());
		CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory,
				criteriaImpl, implementors[0], CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]),
                translator, factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), session.getLoadQueryInfluencers());
		
		NativeSQL nativeSQL = new NativeSQL();
		nativeSQL.setQuery(new String(walker.getSQLString()));
		
		QueryParameters queryParameters = translator.getQueryParameters();
		Object[] objectParameters = queryParameters.getPositionalParameterValues();
		if (objectParameters != null) {
			Object[] parameters = new Object[objectParameters.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = objectParameters[i];
			}
			nativeSQL.setParameters(parameters);
		}
		return nativeSQL;
	}
	
}
