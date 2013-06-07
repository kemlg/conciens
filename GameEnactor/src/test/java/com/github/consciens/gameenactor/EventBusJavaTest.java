package com.github.consciens.gameenactor;


import java.io.IOException;
import java.net.URL;

import net.sf.ictalive.operetta.OM.Norm;
import net.sf.ictalive.operetta.OM.OMPackage;
import net.sf.ictalive.operetta.OM.OperAModel;
import net.sf.ictalive.runtime.NormInstances.NormInstance;
import net.sf.ictalive.runtime.NormInstances.NormInstancesFactory;
import net.sf.ictalive.runtime.NormInstances.PartialStateDescriptionInstance;
import net.sf.ictalive.runtime.action.ActionFactory;
import net.sf.ictalive.runtime.action.MatchmakerQuery;
import net.sf.ictalive.runtime.fact.FactFactory;
import eu.superhub.wp4.monitor.eventbus.EventBus;
import eu.superhub.wp4.monitor.eventbus.exception.EventBusConnectionException;
import eu.superhub.wp4.monitor.metamodel.utils.Serialiser;

public class EventBusJavaTest 
{
	EventBus	eb;
	String busHost = "localhost";

	public EventBusJavaTest()
	{
		try {
			this.eb = new EventBus(busHost, "7676");
		} catch (EventBusConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SubmitTestEvent(String text)
	{				
		FactFactory FFactory = FactFactory.eINSTANCE;
		ActionFactory AFactory = ActionFactory.eINSTANCE;																	
		
		net.sf.ictalive.runtime.fact.LandmarkFulfilment LandMarkF = FFactory.createLandmarkFulfilment();
		
		MatchmakerQuery MQuery = AFactory.createMatchmakerQuery();					 
	 	MQuery.setQuery(text);					 	
		LandMarkF.setDueTo(MQuery);
		
		eb.setActor("#:"+ text + "MatchMakerQuery", "http://EventInjector.com");					
		
		try
		{		
			eb.publish(LandMarkF);		
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
//	private void testDrools() throws Exception
//	{
//		// Load the Clojure script -- as a side effect this initializes the runtime.
//        RT.loadResourceScript("com/github/consciens/gameenactor/Test.clj");
// 
//        // Get a reference to the foo function.
//        Var foo = RT.var("com.github.consciens.gameenactor.Test", "main");
// 
//        // Call it!
//        Object result = foo.invoke();
//        System.out.println(result);
//	}
	
	private void testEventBusNorms() throws Exception
	{
		FactFactory FFactory = FactFactory.eINSTANCE;	
        NormInstancesFactory NIFactory = NormInstancesFactory.eINSTANCE;
        Serialiser<OperAModel>	sOpModel = new Serialiser<OperAModel>(OMPackage.class);
        OperAModel OpModel = sOpModel.deserialise(new URL("file:" + "./TMT-OperettA2.0.opera"));
        
        NormInstance NI = NIFactory.createNormInstance();	
        PartialStateDescriptionInstance psdInstance = NIFactory.createPartialStateDescriptionInstance();
        psdInstance.setName("A robar carteras!!");
        
        Norm dummy = OpModel.getOm().getNs().getNorms().get(0);					
		NI.setNorm(dummy);
		NI.getPartialStateDescriptionInstance().add(psdInstance);
		NI.setName("#"  + dummy.getNormID());
		
		
        
      //Norm Instance Activated Event					
		net.sf.ictalive.runtime.fact.NormInstanceActivated NormIA = FFactory.createNormInstanceActivated();
		NormIA.setNormInstance(NI);				
		eb.setActor("Activator", "http://EventInjector.com");			
		eb.publish(NormIA);				 
	//EO Norm Instance Activated Event
	
		 
	//Norm Instance Violated Event
		net.sf.ictalive.runtime.fact.NormInstanceViolated NormIV = FFactory.createNormInstanceViolated();
		NormIV.setNormInstance(NI);		
					
		eb.setActor("Violator", "http://EventInjector.com");			
		eb.publish(NormIV);		
	//EO Norm Instance Violated Event
	
	//Norm Instance Expired Event
		net.sf.ictalive.runtime.fact.NormInstanceExpired NormIE = FFactory.createNormInstanceExpired();
		NormIE.setNormInstance(NI);									
		eb.setActor("#:"  + "Expirator", "http://EventInjector.com");			
		eb.publish(NormIE);		
	//EO Norm Instance Expired Event	
	}
	
	public static void main(String args[]) throws Exception
	{        			
		
		EventBusJavaTest dummy = new EventBusJavaTest();
		
		
		//dummy.testDrools();
		
		dummy.testEventBusNorms();
        
        
        
        
	}
	
}
