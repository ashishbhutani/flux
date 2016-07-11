/*
 * Copyright 2012-2016, the original author or authors.
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

package com.flipkart.flux.guice.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.flipkart.flux.config.FileLocator;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.resource.FluxResource;
import com.flipkart.flux.resource.StateMachineResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URISyntaxException;

import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_VIEW;

/**
 * <code>ContainerModule</code> is a Guice {@link AbstractModule} implementation used for wiring Flux container components.
 * 
 * @author regunath.balasubramanian
 * @author kartik.bommepally
 *
 */
public class ContainerModule extends AbstractModule {

	/**
	 * Performs concrete bindings for interfaces
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {

	}

	/**
	 * Creates a Jetty {@link WebAppContext} for the Flux dashboard
	 * @return Jetty WebAppContext
	 */
	@Named("DashboardContext")
	@Provides
	@Singleton
	WebAppContext getDashboardWebAppContext() {
		String path = null;
		File[] files = FileLocator.findDirectories("WEB-INF", null);
		for (File file : files) {
			// we need only WEB-INF from runtime project 
			String fileToString = file.toString();
			if (fileToString.contains(".jar!") && fileToString.startsWith("file:/")) {
				fileToString = fileToString.replace("file:/","jar:file:/");
				if (fileToString.contains("runtime-")) {
					path = fileToString;
					break;
				}
			} else {
				if (fileToString.contains(DASHBOARD_VIEW)) {
					path = fileToString;
					break;
				}
			}
		}
		// trim off the "WEB-INF" part as the WebAppContext path should refer to the parent directory
		if (path.endsWith("WEB-INF")) {
			path = path.replace("WEB-INF", "");
		}
		WebAppContext webAppContext = new WebAppContext(path, RuntimeConstants.DASHBOARD_CONTEXT_PATH);
		return webAppContext;
	}
	
	/**
	 * Creates the Jetty server instance for the admin Dashboard and configures it with the @Named("DashboardContext").
	 * @param port where the service is available
	 * @param acceptorThreads no. of acceptors
	 * @param maxWorkerThreads max no. of worker threads
	 * @return Jetty Server instance
	 */
	@Named("DashboardJettyServer")
	@Provides
	@Singleton
	Server getDashboardJettyServer(@Named("Dashboard.service.port") int port,
			@Named("Dashboard.service.acceptors") int acceptorThreads,
			@Named("Dashboard.service.selectors") int selectorThreads,
			@Named("Dashboard.service.workers") int maxWorkerThreads,
			@Named("DashboardContext") WebAppContext webappContext) {
		QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxWorkerThreads);
		Server server = new Server(threadPool);
		ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
		http.setPort(port);
		server.addConnector(http);
		server.setHandler(webappContext);
		server.setStopAtShutdown(true);
		return server;
	}

	/**
	 * Creates the Jetty server instance for the Flux API endpoint.
	 * @param port where the service is available.
	 * @param baseURL base url where the service is located.
	 * @return Jetty Server instance
	 */
	@Named("APIJettyServer")
	@Provides
	@Singleton
	Server getAPIJettyServer(@Named("Api.service.port") int port,
							 @Named("Api.service.baseURL") String baseURL,
							 @Named("APIResourceConfig")ResourceConfig resourceConfig,
							 ObjectMapper objectMapper) throws URISyntaxException {
		//todo-ashish figure out some way of setting acceptor/worker threads
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		provider.setMapper(objectMapper);
		resourceConfig.register(provider);
		final Server server = JettyHttpContainerFactory.createServer(UriBuilder.fromUri(baseURL + RuntimeConstants.API_CONTEXT_PATH).port(port).build(), resourceConfig);
		server.setStopAtShutdown(true);
		return server;
	}

	@Named("APIResourceConfig")
	@Singleton
	@Provides
	public ResourceConfig getAPIResourceConfig(FluxResource fluxUIResource,
											   StateMachineResource stateMachineResource) {
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.register(fluxUIResource);
		resourceConfig.register(stateMachineResource);
		return resourceConfig;
	}

	//may not be the right module class for this. may need to be moved later.
	@Provides
	@Singleton
	ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

}
