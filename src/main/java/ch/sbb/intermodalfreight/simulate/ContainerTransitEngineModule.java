
/* *********************************************************************** *
 * project: org.matsim.*
 * TransitEngineModule.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

 package ch.sbb.intermodalfreight.simulate;

import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.pt.SimpleTransitStopHandlerFactory;
import org.matsim.core.mobsim.qsim.pt.TransitQSimEngine;
import org.matsim.core.mobsim.qsim.pt.TransitStopHandlerFactory;
import org.matsim.pt.ReconstructingUmlaufBuilder;
import org.matsim.pt.UmlaufBuilder;

/**
 * @author ikaddoura
 * this class configures the transit engine component
 * It sets up the necessary bindings for TransitQSimEngine, TransitStopHandlerFactory, and UmlaufBuilder
 * Depending on the simulation configuration, it selects appropriate handler factories for transit stops 
 * and ensures the components are properly instantiated for the simulation to run effectively.
 * 
 * TransitStopHandlerFactory is an interface or abstract class used by QSim to create handlers for transit stops.
 */
public class ContainerTransitEngineModule extends AbstractQSimModule {
	public final static String TRANSIT_ENGINE_NAME = "ContainerTransitEngine";

	@Override
	protected void configureQSim() {
		bind(TransitQSimEngine.class).asEagerSingleton();  
		// a single instance of TransitQSimEngine is created immediately upon application start and used throughout the application whenever TransitQSimEngine is required.
		addNamedComponent(TransitQSimEngine.class, TRANSIT_ENGINE_NAME);
		// checks the configuration to determine if transit is used and if it is being used in the simulation
		// conditional bindings: configures the dependency injection based on runtime configuration
		// If transit is enabled and used in the simulation, TransitStopHandlerFactory is bound to ContainerTransitStopHandlerFactory
		if ( this.getConfig().transit().isUseTransit() && this.getConfig().transit().isUsingTransitInMobsim() ) {
			bind( TransitStopHandlerFactory.class ).to( ContainerTransitStopHandlerFactory.class ) ;
		// if both conditions are true, ContainerTransitStopHandlerFactory is used wherever TransitStopHandlerFactory is required
		} else { // otherwise binds TransitStopHandlerFactory to SimpleTransitStopHandlerFactory
			// Explicit bindings are required, so although it may not be used, we need provide something.
			bind( TransitStopHandlerFactory.class ).to( SimpleTransitStopHandlerFactory.class );
		}

		bind( UmlaufBuilder.class ).to( ReconstructingUmlaufBuilder.class );
	}

}
