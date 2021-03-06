/*
 * Copyright 2013 the original author or authors.
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
package com.joyveb.dbpimpl.cass.prepare.generator;

import static com.joyveb.dbpimpl.cass.prepare.util.CqlStringUtils.noNull;

import java.util.Map;

import com.joyveb.dbpimpl.cass.prepare.config.KeyspaceAttributes;
import com.joyveb.dbpimpl.cass.prepare.option.KeyspaceOption;
import com.joyveb.dbpimpl.cass.prepare.spec.CreateKeyspaceSpecification;

/**
 * CQL generator for generating a <code>CREATE KEYSPACE</code> statement.
 * 
 * @author Alex Shvid
 */
public class CreateKeyspaceCqlGenerator extends WithOptionsCqlGenerator<KeyspaceOption, CreateKeyspaceSpecification> {

	public CreateKeyspaceCqlGenerator(CreateKeyspaceSpecification spec) {
		super(spec);
	}

	public StringBuilder toCql(StringBuilder cql) {
		Map<String, Object> map = spec().getOptions();
		String classname = KeyspaceOption.ReplicationOption.CLASS.getName();
		String strategy = (String) map.get(classname);
		String replication = KeyspaceOption.REPLICATION.name();
		String replication_factor = KeyspaceOption.ReplicationOption.REPLICATION_FACTOR.getName();
		cql = noNull(cql).append("CREATE KEYSPACE ").append(spec().getNameAsIdentifier())
				.append("	WITH ").append(replication)
				.append("= {'").append(classname).append("' : '").append(strategy).append("', ");
		if(strategy.equals(KeyspaceAttributes.DEFAULT_REPLICATION_STRATEGY)){//if SimpleStrategy
			cql.append("'").append(replication_factor).append("':");
		}
		cql.append(map.get(replication_factor)).append("}");
		String durable_writes = KeyspaceOption.DURABLE_WRITES.getName();
		if(map.containsKey(durable_writes)){
			cql.append(" AND ").append(durable_writes).append(" = ").append(map.get(durable_writes));
		}
		return cql.append(";");
	}

}
