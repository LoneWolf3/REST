package com.rest.main;


import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rest.auth.PhonebookAuthenticator;
import com.rest.auth.User;
import com.rest.config.PhonebookConfiguration;
import com.rest.healthcheck.HealthCheckController;
import com.rest.resources.ContactResource;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class App extends Application<PhonebookConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Exception {
		new App().run(args);
	}

	@Override
	public void initialize(Bootstrap<PhonebookConfiguration> b) {
	}

	@Override
	public void run(PhonebookConfiguration c, Environment e) throws Exception {
		LOGGER.info("Method App#run() called");
		for (int i = 0; i < c.getMessageRepetitions(); i++) {
			System.out.println(c.getMessage());
		}
		System.out.println(c.getAdditionalMessage());

		// Create a DBI factory and build a JDBI instance
		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(e, c.getDataSourceFactory(), "mysql");
		// Add the resource to the environment
		e.jersey().register(new ContactResource(jdbi, e.getValidator()));

		// ****** Dropwizard security - custom classes ***********/
		e.jersey().register(new AuthDynamicFeature(
				new BasicCredentialAuthFilter .Builder<User>()
				.setAuthenticator(new PhonebookAuthenticator())
				.setRealm("BASIC-AUTH-REALM").buildAuthFilter()));
		e.jersey().register(RolesAllowedDynamicFeature.class);
		e.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
		
	
         
        //Run multiple health checks
        e.jersey().register(new HealthCheckController(e.healthChecks()));

	}
}
