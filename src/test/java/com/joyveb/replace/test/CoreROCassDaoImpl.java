package com.joyveb.replace.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.joyveb.cassandra.bean.SpDataRpRo;
import com.joyveb.dbpimpl.cass.prepare.convert.CassandraConverter;

/**
 * Sample repository managing {@link CoreRO} entities.
 * 
 */
public class CoreROCassDaoImpl extends AbstractCassDaoImpl<SpDataRpRo, String>{

	private static Logger logger = LoggerFactory
			.getLogger(CoreROCassDaoImpl.class);

	public CoreROCassDaoImpl(Session session,
			CassandraConverter converter
//			ConsistencyLevel consistencyLevel,
//			RetryPolicy retryPolicy
			) {
//		super(session,SpDataRpRo.class, consistencyLevel, retryPolicy);
		super(session, converter, SpDataRpRo.class);
		logger.debug("CoreROCassDaoImpl is started!");
	}

}
