package cbc.boot.myboot.controller.db.util;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 
 * @author Administrator
 * 在Spring 2.0.1中引入了AbstractRoutingDataSource,
 * 该类充当了DataSource的路由中介, 
 * 能有在运行时, 根据某种key值来动态切换到真正的DataSource上。
 */
public class DynamicDataSource extends AbstractRoutingDataSource{
	@Override
	protected Object determineCurrentLookupKey() {
		// TODO Auto-generated method stub
		return DynamicDataSourceHolder.getDataSourceType();
	}

}
