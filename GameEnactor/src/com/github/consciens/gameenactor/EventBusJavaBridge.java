package com.github.consciens.gameenactor;


import java.io.IOException;
import clojure.lang.RT;
import clojure.lang.Var;

import net.sf.ictalive.eventbus.EventBus;
import net.sf.ictalive.eventbus.exception.EventBusConnectionException;
import net.sf.ictalive.operetta.OM.Atom;
import net.sf.ictalive.operetta.OM.Constant;
import net.sf.ictalive.operetta.OM.Norm;
import net.sf.ictalive.operetta.OM.OMFactory;
import net.sf.ictalive.operetta.OM.OMPackage;
import net.sf.ictalive.runtime.event.Actor;
import net.sf.ictalive.runtime.event.Event;
import net.sf.ictalive.runtime.event.EventFactory;
import net.sf.ictalive.runtime.event.Key;
import net.sf.ictalive.runtime.event.ObserverView;
import net.sf.ictalive.runtime.fact.Content;
import net.sf.ictalive.runtime.fact.Message;
import net.sf.ictalive.runtime.fact.SendAct;

import net.sf.ictalive.runtime.action.ActionFactory;
import net.sf.ictalive.runtime.fact.FactFactory;
import net.sf.ictalive.runtime.action.MatchmakerQuery;
import net.sf.ictalive.operetta.OM.OperAModel;
import java.net.URL;
import net.sf.ictalive.metamodel.utils.Serialiser;
import net.sf.ictalive.runtime.NormInstances.NormInstance;
import net.sf.ictalive.runtime.NormInstances.NormInstancesFactory;
import net.sf.ictalive.runtime.NormInstances.PartialStateDescriptionInstance;

public class EventBusJavaBridge 
{
	EventBus	eb;
	String busHost = "localhost";
	
	FactFactory FFactory;
	ActionFactory AFactory;

	public EventBusJavaBridge()
	{
		try {
			this.eb = new EventBus(busHost);
			
			FFactory = FactFactory.eINSTANCE;
			AFactory = ActionFactory.eINSTANCE;
			
		} catch (EventBusConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SendMessageEvent(String text)
	{			
		Event		ev;
		Content		c;
		SendAct		sa;
		Message		ms;
		Atom		a;
		Constant	ct;
		
		String[] splitted_text = text.split(":");
		
		
		ev = EventFactory.eINSTANCE.createEvent();
		c = FactFactory.eINSTANCE.createContent();
		sa = FactFactory.eINSTANCE.createSendAct();
		ms = FactFactory.eINSTANCE.createMessage();
		a = OMFactory.eINSTANCE.createAtom();
		
		Key myKey = EventFactory.eINSTANCE.createKey();
		myKey.setId(String.valueOf(System.currentTimeMillis()));
		
		Actor myActor = EventFactory.eINSTANCE.createActor();
		myActor.setName("WoWGameEnactor");
		myActor.setUrl("alive.lsi.upc.edu");
		
		ObserverView myView = EventFactory.eINSTANCE.createObserverView();
		
		ev.setLocalKey(myKey);
		ev.setAsserter(myActor);
		ev.setPointOfView(myView);
				
		c.setFact(sa);
		ev.setContent(c);
		ms.getObject().add(a);
		a.setPredicate(splitted_text[0]);
		/*
		ct = OMFactory.eINSTANCE.createConstant();
		ct.setName(System.currentTimeMillis() + "");
		a.getArguments().add(ct);
		*/
		for (int i =1; i< splitted_text.length; i++)
		{
			ct = OMFactory.eINSTANCE.createConstant();
			ct.setName(splitted_text[i]);
			a.getArguments().add(ct);
		}						
		sa.setSendMessage(ms);
		
		try
		{
			eb.publish(ev);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SubmitMatchmakerQueryEvent(String text)
	{																				
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
				
}
