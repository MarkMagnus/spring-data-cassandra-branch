package com.joyveb.replace.test;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.insertInto;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Update;
import com.joyveb.cassandra.bean.SpDataRpRo;
import com.joyveb.dbpimpl.cass.prepare.convert.CassandraConverter;

@Slf4j
public class SpConfigStarter {
	private static ApplicationContext context;

	public static void main(String[] args) {
		context = new ClassPathXmlApplicationContext("SpringContext-Cassandra.xml");
		Cluster cluster = (Cluster) context.getBean("cassandra-cluster");
		Session session1 = (Session) context.getBean("cassandra-session");
		CassandraConverter converter = (CassandraConverter) context.getBean("cassandra-converter");
		Session session2 = cluster.connect("spdatarp");
		CoreROCassDaoImpl impl = (CoreROCassDaoImpl) context.getBean("coreROCassDao");
		String messageid = "123456";
		SpDataRpRo entity = new SpDataRpRo();
		entity.setOrderno(UUID.randomUUID().toString());
		entity.setMessageid(messageid);
		impl.insert(entity);
		
//		updateTable(session);
		cluster.shutdown();
	}
	
	public static void insert(Session session){
		Insert insert = (Insert) insertInto("t_spdatarpro_ro");
		String messageid = "123456";
		insert.value("orderno", UUID.randomUUID().toString());
		insert.value("messageid", messageid);
		session.execute(insert);
		log.info(insert + " is done.");
	}
	
	public static void updateTable(Session session){
		Update update = (Update) update("t_spdatarpro_ro");
		update.with(set("amount", 10)).and(set("wagerstatus", 2)).where(eq("messageid", "123456"));
		session.execute(update);
		log.info(update + " is done.");
	}

}
