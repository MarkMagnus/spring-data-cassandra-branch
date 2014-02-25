package com.joyveb.replace.test;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Update.Assignments;
import com.joyveb.dbpimpl.cass.prepare.convert.CassandraConverter;
import com.joyveb.dbpimpl.cass.prepare.core.MappingCassandraEntityInformation;
import com.joyveb.dbpimpl.cass.prepare.mapping.CassandraEntityInformation;
import com.joyveb.dbpimpl.cass.prepare.mapping.CassandraPersistentEntity;
import com.joyveb.dbpimpl.cass.prepare.mapping.Id;
import com.joyveb.dbpimpl.cass.prepare.schema.ConsistencyLevel;
import com.joyveb.dbpimpl.cass.prepare.schema.RetryPolicy;

public abstract class AbstractCassDaoImpl<T, ID extends Serializable> {
	private static Logger logger = LoggerFactory
			.getLogger(AbstractCassDaoImpl.class);

	protected Session session;
	private ConsistencyLevel consistencyLevel;
	private RetryPolicy retryPolicy;
	private CassandraEntityInformation<T, ID> entityInformation;
	private String ENTITY_PACKAGE = "com.joyveb.dbpimpl.cass.entity.";

	// QueryOptions
	public AbstractCassDaoImpl(Session session, CassandraConverter converter,
			Class<T> domainClass
//			, ConsistencyLevel consistencyLevel,
//			RetryPolicy retryPolicy
			) {
		this.session = session;
		CassandraPersistentEntity<?> entity = converter.getMappingContext()
				.getPersistentEntity(domainClass);
		entityInformation = new MappingCassandraEntityInformation<T, ID>(
				(CassandraPersistentEntity<T>) entity);
		initQueryOptions(consistencyLevel, retryPolicy);
		// TODO consistencyLevel, retryPolicy no use
	}

	private void initQueryOptions(ConsistencyLevel consistencyLevel,
			RetryPolicy retryPolicy) {
		if (consistencyLevel != null) {
			this.consistencyLevel = consistencyLevel;
		}
		if (retryPolicy != null) {
			this.retryPolicy = retryPolicy;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.joyveb.dbpapi.iface.CassDaoSupport#insert(java.lang.Object)
	 */
	public <S extends T> void insert(S entity) {
		logger.info("save function, value:" + entity);
		Insert insert = null;
		try {
			insert = buildInsertQuery(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// insert.setConsistencyLevel(consistency); //TODO
		// insert.setRetryPolicy(policy); //TODO
		System.out.println(insert.getQueryString());
		ResultSet set = session.execute(insert);
		Row row = set.one();
		boolean b = row.getBool("[applied]");// or getBool(0)
	}

	private <S extends T> Insert buildInsertQuery(S entity)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Insert insert = (Insert) insertInto(entityInformation.getTableName());
		Class<?> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			String getterName = "get" + StringUtils.capitalize(name);
			Method getter = clazz.getDeclaredMethod(getterName);
			Object result = getter.invoke(entity);
			if (result != null) {
				insert.value(name, result);
			}
		}
		return insert;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.joyveb.dbpapi.iface.CassDaoSupport#insert(java.util.List)
	 */
	public <S extends T> void insert(List<S> entities) {
		// BatchOperation option =
		// cassandraDataTemplate.saveNewInBatch(entities); TODO
		// option.withConsistencyLevel(consistencyLevel);
		// option.withRetryPolicy(retryPolicy);
		// option.execute();
		logger.info("save function value:" + entities);
		Batch batch = batch();
		try {
			for (S entity : entities) {
				Insert insert;
				insert = buildInsertQuery(entity);
				batch.add(insert);
			}
			session.execute(batch);
			logger.info("batch query:" + batch.getQueryString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.joyveb.dbpapi.iface.CassDaoSupport#update(java.lang.Object)
	 */
	public <S extends T> void update(S entity) {
		// SaveOperation option = cassandraDataTemplate.save(entity);
		// option.withConsistencyLevel(consistencyLevel);
		// option.withRetryPolicy(retryPolicy);
		// option.execute();
		// update("BO").with(set("merchantid", "888888")).and(set("status",
		// 2)).where(eq("messageid", "messageid1"));
		try {
			Update update = buildUpdate(entity);
			session.execute(update);
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private <S extends T> Update buildUpdate(S entity) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Update update = QueryBuilder.update(entityInformation.getTableName());
		for (Field field : entity.getClass().getDeclaredFields()) {
			String getterName = "get" + StringUtils.capitalize(field.getName());
			Assignments a = null;
			Method getter = entity.getClass().getDeclaredMethod(getterName);
			Object result = getter.invoke(entity);
			if (result != null) {
				String name = field.getName();
				if (field.getAnnotation(Id.class) != null) {
					update.where(eq(name, result));
				} else {
					if (a == null) {
						a = update.with(set(name, result));
					} else {
						a.and(set(name, result));
					}
				}
			}
		}
		return update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.joyveb.dbpapi.iface.CassDaoSupport#update(java.util.List)
	 */
	public <S extends T> void update(List<S> entities) {
		// BatchOperation option = cassandraDataTemplate.saveInBatch(entities);
		// option.withConsistencyLevel(consistencyLevel);
		// option.withRetryPolicy(retryPolicy);
		// option.execute();
		Batch batch = batch();
		try {
			for (S entity : entities) {
				Update update = buildUpdate(entity);
				batch.add(update);
			}
			session.execute(batch);
			logger.info("batch query:" + batch.getQueryString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Override
//	public <S extends T> T findOne(S id) {
//		logger.info("findOne function value:" + id);
//		Assert.notNull(id, "The given id must not be null!");
//		return cassandraDataTemplate.findById(entityInformation.getJavaType(),
//				entityInformation.getId(id));
//		
//		
//	}
//
//	public List<T> findByExample(T entity) throws JPAException {
//		logger.info("findByBean function value:" + entity);
//		Select select = QueryBuilder.select().all()
//				.from(entityInformation.getTableName());
//		Class<?> clazz = entityInformation.getJavaType();
//		try {
//			for (Field field : clazz.getDeclaredFields()) {
//				if ("SerialVersionUID".equalsIgnoreCase(field.getName())) {
//					continue;
//				}
//				String getterName = "get"
//						+ StringUtils.capitalize(field.getName());
//				Method getter = clazz.getDeclaredMethod(getterName);
//				Object result = getter.invoke(entity);
//				if (result != null) {
//					Clause clause = QueryBuilder.eq(field.getName(), result);
//					select.where(clause);
//				}
//			}
//			select.allowFiltering();
//			return Lists.newArrayList(cassandraDataTemplate.find(
//					entityInformation.getJavaType(), select.getQueryString()));
//			// .withConsistencyLevel(consistencyLevel)
//			// .withRetryPolicy(retryPolicy).execute());
//		} catch (Exception e) {
//			logger.info("findByBean exception,", e);
//			e.printStackTrace();
//			throw new JPAException("", e);
//		}
//	}
//
//	// @Override
//	public boolean exists(ID id) {
//		logger.info("exists function value:" + id);
//		Assert.notNull(id, "The given id must not be null!");
//		session.
//		
//		
//		return cassandraDataTemplate
//				.exists(entityInformation.getJavaType(), id);
//
//	}
//
//	// @Override
//	public List<T> findAll() {
//		logger.info("findAll function");
//		return Lists.newArrayList(cassandraDataTemplate
//				.findAll(entityInformation.getJavaType()));
//		// .withConsistencyLevel(consistencyLevel)
//		// .withRetryPolicy(retryPolicy).execute());
//
//	}
//
//	private List<T> findAll(Select query) {
//		if (query == null) {
//			return Collections.emptyList();
//		}
//		return Lists.newArrayList(cassandraDataTemplate.find(
//				entityInformation.getJavaType(), query.getQueryString()));
//		// .withConsistencyLevel(consistencyLevel)
//		// .withRetryPolicy(retryPolicy).execute());
//	}
//
//	// @Override
//	public <S extends T> List<T> findAll(List<S> pojo) {
//		logger.info("findAll function, value:" + pojo);
//		List<ID> parameters = new ArrayList<ID>();
//		for (T info : pojo) {
//			parameters.add(entityInformation.getId(info));
//		}
//		return cassandraDataTemplate.findAll(entityInformation.getJavaType(),
//				parameters);
//		// .withConsistencyLevel(consistencyLevel)
//		// .withRetryPolicy(retryPolicy).execute();
//
//	}
//
//	// @Override
//	public long count() {
//		logger.info("count function");
//		return cassandraDataTemplate.countAll(entityInformation.getJavaType());
//		// .withConsistencyLevel(consistencyLevel)
//		// .withRetryPolicy(retryPolicy).execute();
//	}
//
//	public void delete(ID id) {
//		Assert.notNull(id, "The given id must not be null!");
//		cassandraDataTemplate.deleteById(entityInformation.getJavaType(), id);
//		// DeleteOperation option = cassandraDataTemplate.deleteById(
//		// entityInformation.getJavaType(), id);
//		// option.withConsistencyLevel(consistencyLevel);
//		// option.withRetryPolicy(retryPolicy);
//		// option.execute();
//	}
//
//	// @Override
//	public void delete(T entity) {
//		logger.info("delete function, value:" + entity);
//		Assert.notNull(entity, "The given entity must not be null!");
//		cassandraDataTemplate.delete(entity);
//		// DeleteOperation option = cassandraDataTemplate.delete(entity);
//		// option.withConsistencyLevel(consistencyLevel);
//		// option.withRetryPolicy(retryPolicy);
//		// option.execute();
//	}
//
//	// @Override
//	public void delete(List<? extends T> entities) {
//		logger.info("delete function, value:" + entities);
//		Assert.notNull(entities, "The given Iterable of entities not be null!");
//		cassandraDataTemplate.deleteInBatch(entities);
//		// BatchOperation option =
//		// cassandraDataTemplate.deleteInBatch(entities);
//		// option.withConsistencyLevel(consistencyLevel);
//		// option.withRetryPolicy(retryPolicy);
//		// option.execute();
//	}
//
//	// @Override
//	public void deleteAll() {
//		cassandraDataTemplate.cqlOps().truncate(
//				entityInformation.getTableName());
//	}
//
//	// @Override
//	public T findByPartitionKey(ID id) {
//		Assert.notNull(id, "The given id must not be null!");
//		return cassandraDataTemplate.findById(entityInformation.getJavaType(),
//				id);
//		// .withConsistencyLevel(consistencyLevel)
//		// .withRetryPolicy(retryPolicy).execute();
//	}
//
//	public List<Insert> getInsertStatement(String entities, String tablename) {
//		List<Insert> inserts = new ArrayList<>();
//		try {
//			Class clazz = Class.forName(ENTITY_PACKAGE
//					+ DBPCassBean.getClassName2Name(tablename));
//			// List<T> list = AvroUtil.deserializeArray(()entities, clazz);
//			List<T> list = JsonUtil.json2List(entities, clazz);
//			for (T t : list) {
//				Insert insert = QueryBuilder.insertInto(entityInformation
//						.getTableName());
//				Field[] fields = clazz.getDeclaredFields();
//				for (Field field : fields) {
//					String name = field.getName();
//					if ("SerialVersionUID".equalsIgnoreCase(name)) {
//						continue;
//					}
//					String getterName = "get" + StringUtils.capitalize(name);
//					Method getter = clazz.getDeclaredMethod(getterName);
//					Object result = getter.invoke(t);
//					if (result != null) {
//						insert.value(name, result);
//					}
//				}
//				inserts.add(insert);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return inserts;
//	}
//
//	public Delete getDeleteStatement(String pojo, String entity) {
//		try {
//			Delete delete = QueryBuilder.delete().all()
//					.from(entityInformation.getTableName());
//			if (StringUtils.isNotBlank(entity)) {
//				Class clazz = Class.forName(ENTITY_PACKAGE
//						+ DBPCassBean.getClassName2Name(pojo));
//				T tt = (T) JsonUtil.json2Bean(entity, clazz);
//				Field[] fields = clazz.getDeclaredFields();
//				for (int i = 0; i < fields.length; i++) {
//					String name = fields[i].getName();
//					String getterName = "get" + StringUtils.capitalize(name);
//					Method getter = clazz.getDeclaredMethod(getterName);
//					Object result = getter.invoke(tt);
//					if (result != null) {
//						Clause clause = QueryBuilder.eq(name, result);
//						delete.where().and(clause);
//					}
//				}
//			}
//			return delete;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
}
