package com.github.conciens.gameenactor;

import clojure.lang.RT;
import clojure.lang.Var;

public class GameBridgeLauncher
{
	public static void main(String args[]) throws Exception
	{        			
		// Load the Clojure script -- as a side effect this initializes the runtime.
        RT.loadResourceScript("com/github/conciens/gameenactor/GameBridge.clj");
 
        // Get a reference to the foo function.
        Var foo = RT.var("com.github.conciens.gameenactor.GameBridge", "main");
 
        // Call it!
        Object result = foo.invoke();
        System.out.println(result);
	}
}
