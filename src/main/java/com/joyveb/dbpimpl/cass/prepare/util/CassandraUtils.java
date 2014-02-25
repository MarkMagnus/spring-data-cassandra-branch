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
package com.joyveb.dbpimpl.cass.prepare.util;

import com.joyveb.dbpimpl.cass.prepare.support.CassandraExceptionTranslator;

public class CassandraUtils {
	
	public static CassandraExceptionTranslator EXCEPTION_TRANSLATOR = new CassandraExceptionTranslator();

	public static RuntimeException translateIfPossible(RuntimeException ex) {
		RuntimeException resolved = EXCEPTION_TRANSLATOR.translateExceptionIfPossible(ex);
		return resolved == null ? ex : resolved;
	}

}
