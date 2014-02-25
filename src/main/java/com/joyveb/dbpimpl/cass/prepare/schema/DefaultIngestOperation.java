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

import java.util.Iterator;
import java.util.List;

import com.datastax.driver.core.Query;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * Default Ingest operation implementation
 * 
 * @author Alex Shvid
 * 
 */

public class DefaultIngestOperation extends AbstractQueryOperation<List<ResultSet>, IngestOperation> implements
		IngestOperation {

	private final Iterator<Query> queryIterator;

	public DefaultIngestOperation(Session session, Iterator<Query> iterator) {
		super(session);
		this.queryIterator = iterator;
	}

	@Override
	public List<ResultSet> execute() {
		return doExecute(queryIterator);
	}

}
