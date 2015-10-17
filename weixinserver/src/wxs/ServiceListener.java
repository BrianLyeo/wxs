package wxs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;

public class ServiceListener implements ServletContextListener {
	

	public void contextInitialized(ServletContextEvent event) {
		PropertyConfigurator.configure("/opt/conf/log4j.properties");
	}

	
	public void contextDestroyed(ServletContextEvent event) {
		
	}
}
