/*
 * Copyright 2014 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joyveb.dbpimpl.cass.prepare.schema;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.util.Assert;

import com.datastax.driver.core.Query;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.joyveb.dbpimpl.cass.prepare.util.CassandraUtils;

/**
 * 
 * @author Alex Shvid
 * 
 * @param <T>
 * @param <O>
 */

public abstract class AbstractQueryOperation<T, O extends QueryOperation<T, O>> implements QueryOperation<T, O> {

	protected final Session session;

	private ConsistencyLevel consistencyLevel;
	private RetryPolicy retryPolicy;
	private Boolean queryTracing;

	private Executor executor;

	protected AbstractQueryOperation(Session session) {
		Assert.notNull(session);
		this.session = session;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O withConsistencyLevel(ConsistencyLevel consistencyLevel) {
		Assert.notNull(consistencyLevel);
		this.consistencyLevel = consistencyLevel;
		return (O) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O withRetryPolicy(RetryPolicy retryPolicy) {
		Assert.notNull(retryPolicy);
		this.retryPolicy = retryPolicy;
		return (O) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O withQueryTracing(Boolean queryTracing) {
		Assert.notNull(queryTracing);
		return (O) this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public O withExecutor(Executor executor) {
		Assert.notNull(executor);
		this.executor = executor;
		return (O) this;
	}

	protected void addQueryOptions(Query query) {
		/*
		 * Add Query Options
		 */
		if (consistencyLevel != null) {
			query.setConsistencyLevel(ConsistencyLevelResolver.resolve(consistencyLevel));
		}
		if (retryPolicy != null) {
			query.setRetryPolicy(RetryPolicyResolver.resolve(retryPolicy));
		}
		if (queryTracing != null) {
			if (queryTracing.booleanValue()) {
				query.enableTracing();
			} else {
				query.disableTracing();
			}
		}
	}

	protected Query doCreateQuery(QueryCreator qc) {
		return qc.createQuery();
	}

	protected ResultSet doExecute(Query query) {
		addQueryOptions(query);
		return session.execute(query);
	}
	
	protected CassandraFuture<ResultSet> doExecuteAsync(Query query) {
		addQueryOptions(query);
		ResultSetFuture resultSetFuture = session.executeAsync(query);
		CassandraFuture<ResultSet> wrappedFuture = new CassandraFuture<ResultSet>(resultSetFuture,
				CassandraUtils.EXCEPTION_TRANSLATOR);
		return wrappedFuture;
	}

	protected Executor getExecutor() {
		return executor != null ? executor : MoreExecutors.sameThreadExecutor();
	}

	/*
	 * Parallel execution
	 */

	protected List<ResultSet> doExecute(Iterator<Query> queryIterator) {
		return doExecuteAsync(queryIterator).getUninterruptibly();
	}

	protected CassandraFuture<List<ResultSet>> doExecuteAsync(Iterator<Query> queryIterator) {

		if (!queryIterator.hasNext()) {
			ListenableFuture<List<ResultSet>> emptyResultFuture = Futures
					.immediateFuture(Collections.<ResultSet> emptyList());
			CassandraFuture<List<ResultSet>> wrappedFuture = new CassandraFuture<List<ResultSet>>(emptyResultFuture,
					CassandraUtils.EXCEPTION_TRANSLATOR);
			return wrappedFuture;
		}
		final Iterator<ListenableFuture<ResultSet>> resultSetFutures = Iterators.transform(queryIterator,
				new Function<Query, ListenableFuture<ResultSet>>() {

					@Override
					public ListenableFuture<ResultSet> apply(Query query) {
						return doExecuteAsync(query);
					}

				});
		ListenableFuture<List<ResultSet>> allResultSetFuture = Futures
				.successfulAsList(new Iterable<ListenableFuture<ResultSet>>() {

					@Override
					public Iterator<ListenableFuture<ResultSet>> iterator() {
						return resultSetFutures;
					}

				});

		CassandraFuture<List<ResultSet>> wrappedFuture = new CassandraFuture<List<ResultSet>>(allResultSetFuture,
				CassandraUtils.EXCEPTION_TRANSLATOR);

		return wrappedFuture;
	}

}
