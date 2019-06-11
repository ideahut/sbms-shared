package com.github.ideahut.sbms.shared.audit.handler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.ideahut.sbms.shared.annotation.Auditable;
import com.github.ideahut.sbms.shared.audit.AuditExecutor.ContentType;
import com.github.ideahut.sbms.shared.audit.AuditHandler;
import com.github.ideahut.sbms.shared.audit.AuditObject;
import com.github.ideahut.sbms.shared.audit.Auditor;
import com.github.ideahut.sbms.shared.entity.EntityBase;
import com.github.ideahut.sbms.shared.hibernate.MetadataIntegrator;
import com.github.ideahut.sbms.shared.optional.audit.Audit;

public class TransactionManagerAuditHandler implements AuditHandler {
	
	private static final String DEFAULT_TABLE_SUFFIX = "audit"; 
	
	private final Map<PlatformTransactionManager, Map<Class<?>, TableAccessible>> mapTrxManager = new HashMap<PlatformTransactionManager, Map<Class<?>, TableAccessible>>();
	
	private final ObjectMapper objectMapper;
	
	
	private ApplicationContext applicationContext;
	
	private boolean createEntityAuditTable = false; 
	
	private String entityAuditTableSuffix;
	
	private boolean useDefaultTransactionManager = false;
	
	
	private PlatformTransactionManager defaultTransactionManager; // yang menghandle entity Audit
	
	private MetadataIntegrator defaultIntegrator;
	
	private boolean initialize = false;
	
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setCreateEntityAuditTable(boolean createEntityAuditTable) {
		this.createEntityAuditTable = createEntityAuditTable;
	}

	public void setEntityAuditTableSuffix(String entityAuditTableSuffix) {
		this.entityAuditTableSuffix = entityAuditTableSuffix;
	}

	public void setUseDefaultTransactionManager(boolean useDefaultTransactionManager) {
		this.useDefaultTransactionManager = useDefaultTransactionManager;
	}


	@SuppressWarnings("serial")
	public TransactionManagerAuditHandler() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
			@Override
			public boolean hasIgnoreMarker(AnnotatedMember m) {
				Auditable auditable = _findAnnotation(m, Auditable.class);
				if (auditable != null && !auditable.value()) {
					return true;
				}
				return super.hasIgnoreMarker(m);
			}
		});
	}	
	

	@Override
	public void initialize() throws Exception {
		if (initialize) {
			return;
		}
		entityAuditTableSuffix = entityAuditTableSuffix != null ? entityAuditTableSuffix.trim() : "";
		if (createEntityAuditTable && entityAuditTableSuffix.isEmpty()) {
			entityAuditTableSuffix = DEFAULT_TABLE_SUFFIX;
		}
		mapTrxManager.clear();
		List<EntityTableInfo> entityTableInfoList = new ArrayList<EntityTableInfo>();
		Map<String, PlatformTransactionManager> mapContextTrxManager = applicationContext.getBeansOfType(PlatformTransactionManager.class);
		for (PlatformTransactionManager transactionManager : mapContextTrxManager.values()) {			
			MetadataIntegrator integrator = MetadataIntegrator.create(transactionManager);
			Collection<Class<?>> annotatedClasses = integrator.getAnnotatedClasses();
			if (annotatedClasses.remove(Audit.class)) {
				if (defaultTransactionManager != null) {
					throw new Exception("Cannot assigned entity " + Audit.class.getName() + " to many transaction manager.");
				}
				defaultTransactionManager = transactionManager;
				defaultIntegrator = integrator;
				if (!createEntityAuditTable) break;
			}
			
			mapTrxManager.put(transactionManager, new HashMap<Class<?>, TableAccessible>());
			
			EntityTableInfo entityTableInfo = new EntityTableInfo();
			entityTableInfo.transactionManager = transactionManager;
			entityTableInfo.integrator = integrator;
			for (Class<?> clazz : annotatedClasses) {
				if (!EntityBase.class.isAssignableFrom(clazz)) continue;
				Auditable auditable = clazz.getAnnotation(Auditable.class);
				if (auditable != null && auditable.value()) {
					javax.persistence.Table table = clazz.getAnnotation(javax.persistence.Table.class);
					if (table != null) {
						String tableName = table.name().toLowerCase();
						entityTableInfo.entityTable.put(clazz, tableName);
						entityTableInfo.tableAuditable.put(tableName, auditable);
						entityTableInfo.tableEntity.put(tableName, clazz);
					}
				}
			}
			if (!entityTableInfo.entityTable.isEmpty()) {
				entityTableInfoList.add(entityTableInfo);
			}								
		}
		if (defaultTransactionManager == null) {
			throw new Exception("Default transaction manager is not found, please add entity class: " + Audit.class.getName());
		}
		
		if (createEntityAuditTable) {
			for (EntityTableInfo entityTableInfo : entityTableInfoList) {
				prepare(entityTableInfo);
			}
		}
		entityTableInfoList.clear();
		initialize = true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void save(AuditObject auditObject, ContentType contentType) throws Exception {
		Object object = auditObject.getObject();
		boolean executed = false;
		if (object instanceof EntityBase && createEntityAuditTable) {
			PlatformTransactionManager transactionManager = auditObject.getTransactionManager();
			if (transactionManager == null) {
				transactionManager = defaultTransactionManager;
			}
			TableAccessible tableAccessible = mapTrxManager.get(transactionManager).get(object.getClass());
			if (tableAccessible != null) {
				List<Object> parameters = new ArrayList<Object>();
				for (ColumnAccessible columnAccessible : tableAccessible.parameters) {
					Object value = getValue(object, columnAccessible);
					parameters.add(value);
				}
				Auditor auditor = auditObject.getAuditor();
				if (auditor != null) {
					parameters.add(auditor.getId());
					parameters.add(auditor.getName());
				} else {
					parameters.add(null);
					parameters.add(null);
				}
				String action = auditObject.getAction();
				if (action == null) {
					action = "__UNDEFINED__";
				}
				parameters.add(action);
				parameters.add(auditObject.getInfo());
				parameters.add(auditObject.getEntry());
				
				Session session = null;
		    	try {
		    		session = tableAccessible.integrator.getSessionFactory().openSession();			    		
					session.beginTransaction();
					NativeQuery query = session.createNativeQuery(tableAccessible.sqlInsert);
					for (int i = 0; i < parameters.size(); i++) {
						query.setParameter(i + 1, parameters.get(i));
					}
					query.executeUpdate();					
					session.getTransaction().commit();
				} catch (Exception e) {
					if (session != null) {
						session.getTransaction().rollback();
					}
					throw e;
				} finally {
					try { session.close(); } catch (Exception e) {}
				}
		    	executed = true;
			}
			
		}
		if (executed) {
			return;
		}		
		Session session = null;
		try {
			String type = object.getClass().getName();
			Auditor auditor = auditObject.getAuditor();
			
			Audit audit = new Audit();
			String action = auditObject.getAction();
			if (action == null) {
				action = "__UNDEFINED__";
			} else {
				action = (object instanceof EntityBase ? "ENTITY_" : "") + action;
			}
			audit.setAction(action);
			if (auditor != null) {
				audit.setAuditorId(auditor.getId());
				audit.setAuditorName(auditor.getName());
			}
			audit.setEntry(auditObject.getEntry());
			audit.setInfo(auditObject.getInfo());
			audit.setType(type);
			
			String content = null;
			byte[] bytes = null;
			if (object instanceof byte[]) {
				bytes = (byte[])object;
			} else {
				if (ContentType.BYTES.equals(contentType)) {
					bytes = objectMapper.writeValueAsBytes(object);					
				} else if (ContentType.STRING_AND_BYTES.equals(contentType)) {
					bytes = objectMapper.writeValueAsBytes(object);
					content = objectMapper.writeValueAsString(object);
				} else {
					content = objectMapper.writeValueAsString(object);
				}
			}
			audit.setBytes(bytes);
			audit.setContent(content);				
			session = defaultIntegrator.getSessionFactory().openSession();
			session.beginTransaction();
			session.persist(audit);
			session.getTransaction().commit();
		} catch (Exception e) {
			if (session != null) {
				session.getTransaction().rollback();
			}
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	private void prepare(EntityTableInfo entityTableInfo) throws Exception {
		MetadataIntegrator srcIntegrator = entityTableInfo.integrator;
		Database srcDatabase = srcIntegrator.getMetadata().getDatabase();
		//Dialect srcDialect = srcIntegrator.getSessionFactory().getDialect();
		
		MetadataIntegrator dstIntegrator = useDefaultTransactionManager ? defaultIntegrator : srcIntegrator;
		//Database dstDatabase = dstIntegrator.getMetadata().getDatabase();
		Dialect dstDialect = dstIntegrator.getSessionFactory().getDialect();
		
		for (Namespace namespace : srcDatabase.getNamespaces()) {
		    for (Table table : namespace.getTables()) {
		    	String lcaseName = table.getName().toLowerCase();
		    	if (!entityTableInfo.tableEntity.containsKey(lcaseName)) continue;
		    	Auditable auditable = entityTableInfo.tableAuditable.get(lcaseName);
		    	Class<?> entity = entityTableInfo.tableEntity.get(lcaseName);
		    	
		    	Map<String, ColumnAccessible> mapColumnAccessible = getColumnAccessibleObject(entity);
		    	
		    	Table newTable = new Table();
		    	newTable.setName(table.getName() + "_" + entityAuditTableSuffix);
		    	newTable.setSchema(table.getSchema());
		    	newTable.setCatalog(table.getCatalog());
		    	newTable.setComment(table.getComment());
		    	newTable.setAbstract(table.isAbstract());								
		    	newTable.setSubselect(table.getSubselect());		    	
		    	
		    	List<ColumnAccessible> parameters = new ArrayList<ColumnAccessible>();
		    	StringBuilder sqlInsert = new StringBuilder("insert into ")
		    	.append(newTable.getQualifiedTableName()).append("(");
		    	
		    	String catalog = newTable.getCatalog();
				String schema = newTable.getSchema();
		    	Iterator<Column> iterator = table.getColumnIterator();
		    	int countparam = 0;
		    	while (iterator.hasNext()) {
		    		Column column = iterator.next();
					Column newColumn = column.clone();
					newColumn.setUnique(false); // set selalu false
					newTable.addColumn(newColumn);
					sqlInsert.append(newColumn.getQuotedName(dstDialect)).append(",");
					parameters.add(mapColumnAccessible.get(newColumn.getName().toLowerCase()));
					countparam++;
				}
		    	// Tambah column audit
				addAuditTableColumn(dstIntegrator, newTable, sqlInsert);
				
				countparam += 5;
				sqlInsert.delete(sqlInsert.length() - 1, sqlInsert.length()).append(") values (");
				for (int i = 0; i < countparam; i++) {
					sqlInsert.append("?,");
				}
				sqlInsert.delete(sqlInsert.length() - 1, sqlInsert.length()).append(")");
				
		    	if (auditable.enableRowId()) {
		    		newTable.setRowId(table.getRowId());
				}
		    	List<String> sqlCreateIndexList = new ArrayList<String>();
		    	if (auditable.enableIndexes()) {
					Iterator<Index> iterIndex = table.getIndexIterator();
					while (iterIndex.hasNext()) {
						Index index = iterIndex.next();
						Index newIndex = new Index();
						newIndex.setName(index.getName() + "_" + entityAuditTableSuffix);
						newIndex.setTable(newTable);
						Iterator<Column> iterColumn = index.getColumnIterator();
						while (iterColumn.hasNext()) {
							Column column = iterColumn.next();
							Column newColumn = column.clone();
							newColumn.setUnique(false);
							newIndex.addColumn(newColumn);
						}
						newTable.addIndex(newIndex);
						String sql = newIndex.sqlCreateString(dstDialect, dstIntegrator.getMetadata(), catalog, schema);
						sqlCreateIndexList.add(sql);
					}
				}
		    	
		    	boolean exists = isTableExist(dstIntegrator, newTable);
		    	if (!exists) {
			    	Session session = null;
			    	try {
			    		String sqlCreate = newTable.sqlCreateString(dstDialect, srcIntegrator.getMetadata(), catalog, schema);
			    		session = dstIntegrator.getSessionFactory().openSession();			    		
						session.beginTransaction();
						NativeQuery query = session.createNativeQuery(sqlCreate);
						query.executeUpdate();
						for (String sql : sqlCreateIndexList) {
							query = session.createNativeQuery(sql);
							query.executeUpdate();
						}
						session.getTransaction().commit();
					} catch (Exception e) {
						if (session != null) {
							session.getTransaction().rollback();
						}
						throw e;
					} finally {
						try { session.close(); } catch (Exception e) {}
					}
		    	}
		    	TableAccessible tableAccessible = new TableAccessible();
		    	tableAccessible.integrator = dstIntegrator;
		    	tableAccessible.parameters = parameters;
		    	tableAccessible.sqlInsert = sqlInsert.toString();
		    	mapTrxManager.get(entityTableInfo.transactionManager).put(entity, tableAccessible);
		    }
		}
		
		
	}
	
	private Map<String, ColumnAccessible> getColumnAccessibleObject(Class<?> entity) {
		Map<String, ColumnAccessible> result = new HashMap<String, ColumnAccessible>();
		for (Field field : entity.getDeclaredFields()) {
			field.setAccessible(true);
			javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
			javax.persistence.JoinColumn joinColumn = field.getAnnotation(javax.persistence.JoinColumn.class);
			if (column != null) {
				String name = column.name();
				if (name.isEmpty()) {
					name = field.getName();
				}				
				ColumnAccessible columnAccessible = new ColumnAccessible();
				columnAccessible.column = field;
				result.put(name.toLowerCase(), columnAccessible);
			}			
			else if (joinColumn != null) {
				String name = joinColumn.name();
				if (name.isEmpty()) {
					name = field.getName();
				}
				ColumnAccessible columnAccessible = new ColumnAccessible();
				columnAccessible.column = field;
				columnAccessible.joinColumnId = findColumnIdAccessible(field.getType());
				result.put(name.toLowerCase(), columnAccessible);				
			}
		}
		
		for (Method method : entity.getDeclaredMethods()) {
			method.setAccessible(true);
			javax.persistence.Column column = method.getAnnotation(javax.persistence.Column.class);
			javax.persistence.JoinColumn joinColumn = method.getAnnotation(javax.persistence.JoinColumn.class);
			if (column != null) {
				String name = column.name();
				if (name.isEmpty()) {
					name = method.getName().substring(3); // remove get or set ?
				}
				ColumnAccessible columnAccessible = new ColumnAccessible();
				columnAccessible.column = method;
				result.put(name.toLowerCase(), columnAccessible);
			}
			else if (joinColumn != null) {
				String name = joinColumn.name();
				if (name.isEmpty()) {
					name = method.getName().substring(3); // remove get or set ?
				}
				ColumnAccessible columnAccessible = new ColumnAccessible();
				columnAccessible.column = method;
				columnAccessible.joinColumnId = findColumnIdAccessible(method.getReturnType());
				result.put(name.toLowerCase(), columnAccessible);
			}
		}
		return result;
	}
	
	private AccessibleObject findColumnIdAccessible(Class<?> entity) {
		for (Field field : entity.getDeclaredFields()) {
			javax.persistence.Id id = field.getAnnotation(javax.persistence.Id.class);
			if (id != null) {
				return field;
			}
		}
		for (Method method : entity.getDeclaredMethods()) {
			javax.persistence.Id id = method.getAnnotation(javax.persistence.Id.class);
			if (id != null) {
				return method;
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("deprecation")
	private void addAuditTableColumn(MetadataIntegrator integrator, Table table, StringBuilder sqlInsert) throws Exception {
		Dialect dialect = integrator.getSessionFactory().getDialect();
		Field typeField = SimpleValue.class.getDeclaredField("type");
		typeField.setAccessible(true);
		
		SimpleValue stringValue = new SimpleValue((MetadataImplementor)integrator.getMetadata(), table);
		stringValue.setTypeName(String.class.getName());
		typeField.set(stringValue, StringType.INSTANCE);
		
		Column auditorIdColumn = new Column();
		auditorIdColumn.setName("auditor_id_");
		auditorIdColumn.setLength(255);
		auditorIdColumn.setScale(2);
		auditorIdColumn.setValue(stringValue);
		auditorIdColumn.setTypeIndex(0);
		auditorIdColumn.setNullable(true);
		auditorIdColumn.setPrecision(19);
		auditorIdColumn.setUnique(false);
		table.addColumn(auditorIdColumn);
		sqlInsert.append(auditorIdColumn.getQuotedName(dialect)).append(",");
		
		Column auditorNameColumn = new Column();
		auditorNameColumn.setName("auditor_name_");
		auditorNameColumn.setLength(255);
		auditorNameColumn.setScale(2);
		auditorNameColumn.setValue(stringValue);
		auditorNameColumn.setTypeIndex(0);
		auditorNameColumn.setNullable(true);
		auditorNameColumn.setPrecision(19);
		auditorNameColumn.setUnique(false);
		table.addColumn(auditorNameColumn);
		sqlInsert.append(auditorNameColumn.getQuotedName(dialect)).append(",");
		
		Column actionColumn = new Column();
		actionColumn.setName("audit_action_");
		actionColumn.setLength(255);
		actionColumn.setScale(2);
		actionColumn.setValue(stringValue);
		actionColumn.setTypeIndex(0);
		actionColumn.setNullable(true);
		actionColumn.setPrecision(19);
		actionColumn.setUnique(false);
		table.addColumn(actionColumn);
		sqlInsert.append(actionColumn.getQuotedName(dialect)).append(",");
		
		Column infoColumn = new Column();
		infoColumn.setName("audit_info_");
		infoColumn.setLength(255);
		infoColumn.setScale(2);
		infoColumn.setValue(stringValue);
		infoColumn.setTypeIndex(0);
		infoColumn.setNullable(true);
		infoColumn.setPrecision(19);
		infoColumn.setUnique(false);
		table.addColumn(infoColumn);
		sqlInsert.append(infoColumn.getQuotedName(dialect)).append(",");
		
		SimpleValue timestampValue = new SimpleValue((MetadataImplementor)integrator.getMetadata(), table);
		timestampValue.setTypeName("timestamp");
		typeField.set(timestampValue, TimestampType.INSTANCE);		
		
		Column entryColumn = new Column();
		entryColumn.setName("audit_entry_");
		entryColumn.setLength(255);
		entryColumn.setScale(2);
		entryColumn.setValue(timestampValue);
		entryColumn.setTypeIndex(0);
		entryColumn.setNullable(false);
		entryColumn.setPrecision(19);
		entryColumn.setUnique(false);
		table.addColumn(entryColumn);
		sqlInsert.append(entryColumn.getQuotedName(dialect)).append(",");
	}
	
	private boolean isTableExist(MetadataIntegrator integrator, Table table) throws Exception {
		Connection connection = null;
		try {
			connection = integrator.getConnection();
			DatabaseMetaData dbMetaData = connection.getMetaData();
			ResultSet rs = dbMetaData.getTables(
				table.getCatalog(), 
				table.getSchema(), 
				table.getName(), 
				new String[] {"TABLE"}
			);
			boolean result = rs.next();
			rs.close();
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try { connection.close(); } catch (Exception e) {}
		}
	}
	
	private Object getValue(Object object, ColumnAccessible columnAccessible) throws Exception {
		Object value = null;
		if (columnAccessible.column instanceof Field) {
			Field field = (Field)columnAccessible.column;
			value = field.get(object);
		} 
		else if (columnAccessible.column instanceof Method) {
			Method method = (Method)columnAccessible.column;
			value = method.invoke(object);
		}
		if (columnAccessible.joinColumnId != null && value != null) {
			if (columnAccessible.joinColumnId instanceof Field) {
				Field field = (Field)columnAccessible.joinColumnId;
				value = field.get(value);
			} 
			else if (columnAccessible.joinColumnId instanceof Method) {
				Method method = (Method)columnAccessible.joinColumnId;
				value = method.invoke(value);
			}
		}
		return value;
	}
	
	private class EntityTableInfo {
		private Map<Class<?>, String> entityTable = new HashMap<Class<?>, String>();
		private Map<String, Auditable> tableAuditable = new HashMap<String, Auditable>();
		private Map<String, Class<?>> tableEntity = new HashMap<String, Class<?>>();
		private PlatformTransactionManager transactionManager;
		private MetadataIntegrator integrator;
	}
	
	private class TableAccessible {
		private String sqlInsert;
		private List<ColumnAccessible> parameters;
		private MetadataIntegrator integrator;
	}
	
	private class ColumnAccessible {
		private AccessibleObject column;
		private AccessibleObject joinColumnId;		
	}

}
