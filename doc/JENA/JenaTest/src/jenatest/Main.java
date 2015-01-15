/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jenatest;

import java.io.FileNotFoundException;



/**
 *
 * @author Ignasi Gómez-Sebastià
 */
public class Main {



    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws FileNotFoundException
    {
        String JENA = "/Users/igomez/deapt/dea-repo/conciens/doc/JENA/JenaTest/";
        String File = "ConciensEventOntology.owl";
        String NamingContext = "http://www.semanticweb.org/igomez/ontologies/2014/11/ConciensEventOntology.owl";
        
        System.out.println("----------------Starting program -------------");

        JenaTester tester = new JenaTester(JENA,File,NamingContext);

        tester.loadOntology();        

        tester.getClasses();

        //tester.getIndividuals();

        //tester.getIndividualsByClass();

        //tester.getPropertiesByClass();       

        System.out.println("Run a test Object property");
        //tester.runSparqlQueryDataProperty();

        System.out.println("Run a test Data property");
        //tester.runSparqlQueryObjectProperty();

        System.out.println("Run and modify");
        //tester.runSparqlQueryModify();

        System.out.println("Re-Run to check modification");
        //tester.runSparqlQueryModify();

        
        
        //tester.releaseOntology();
        
        System.out.println("--------- Program terminated --------------------");
     
    }

}
