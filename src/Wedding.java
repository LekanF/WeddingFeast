/** -------------------------------------------------------------------------
 * File: Wedding.java
 * Version 2
 * --------------------------------------------------------------------------
 * A project for the COMP 567 Class at McGill
 * Describes a stochastic wedding feast problem
 * Author: Olamilekan Fadahunsi
 * Co-Author: Julian Skirzynski
 * --------------------------------------------------------------------------
 * 
 * */

import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;

public class Wedding {

	static int nDishes;
	static int nIgred;
	static int scenarios;
	
	static double[][] amount;
	static double[] pGuestDist;
	static double[] guests;
	static double[] pVegDist;
	static double[] veg;
	
	static double[] price;
	static double[] HoursPerDish;
	static double wage;
	
	static void readData(String fileName) throws java.io.IOException, InputDataReader.InputDataReaderException {
		
		InputDataReader reader = new InputDataReader(fileName);
		
		amount		=	reader.readDoubleArrayArray();
		pGuestDist 	=	reader.readDoubleArray();
		guests		=	reader.readDoubleArray();
		pVegDist		=	reader.readDoubleArray();
		veg			=	reader.readDoubleArray();
		
		price		=	reader.readDoubleArray();
		HoursPerDish	=	reader.readDoubleArray();
		wage			=	reader.readDouble();	
		
		nDishes = HoursPerDish.length;
		nIgred = price.length;
		scenarios  = guests.length;
	}
	
	public static void main(String[] args) {
		try {
			String fileName = "wedding.dat";
			if ( args.length > 0 )
	            fileName = args[0];
	         readData(fileName);
	         
	         // Model solver
	         IloCplex cplex = new IloCplex();
	         
	         // VARIABLES

	         IloNumVar[] Prepare = new IloNumVar[nDishes]; // Number of plates to prepare per dish
	         
	         for (int i = 0; i < nDishes; i++) {
					Prepare[i] = cplex.numVar(1, Double.MAX_VALUE);
	         }
	         
//	         IloNumVar[] Surplus = new IloNumVar[nDishes]; // Surplus plates prepared per dish 
	         IloNumVar[] Surplus = new IloNumVar[scenarios];
	         for (int i = 0; i < scenarios; i++) {
					Surplus[i] = cplex.numVar(0, Double.MAX_VALUE);
	         }
	         
//	         IloNumVar[] Shortage = new IloNumVar[nDishes]; // Shortage of plates prepared 
	         IloNumVar[] Shortage = new IloNumVar[scenarios];
	         for (int i = 0; i < scenarios; i++) {
					Shortage[i] = cplex.numVar(0, Double.MAX_VALUE);
	         }
	         
	         	         
	         
	         // Cost of each dish
	         double[] cost = new double[nDishes];
	         
	         for (int i = 0; i < nDishes; i++) {
	 			for (int j = 0; j < nIgred; j++) {
	 				cost[i] += amount[i][j] * price[j];
	 			}
	        }	
	 			
	 		// Man hours cost i.e, cost per hour of cooking a dish
	 		double[] manHourCost = new double[nDishes]; 

	 		for (int k = 0; k < HoursPerDish.length; k++) {
	 			manHourCost[k] = HoursPerDish[k] * wage;
	 		}
	 		
	 		double costSurplus[] = new double[nDishes];
	 		
	 		for (int i = 0; i < nDishes; i++) {
	 			costSurplus[i] = cost[i] + manHourCost[i];
	 		}
	 		
	 		double costShortage[] = new double[nDishes];
	 		
	 		for (int i = 0; i < nDishes; i++) {
	 			costShortage[i] = 1.2 * (cost[i] + manHourCost[i]);
	 		}
	 		
	 		
	         
	         // Objective Function
	        IloLinearNumExpr ObjNumDishes = cplex.linearNumExpr();
	        IloLinearNumExpr ObjSurplus = cplex.linearNumExpr();
	        IloLinearNumExpr ObjShortage = cplex.linearNumExpr();
	 		
	        for (int i = 0 ; i < nDishes; i++) {
	        		ObjNumDishes.addTerm(manHourCost[i], Prepare[i]);
	        		ObjNumDishes.addTerm(cost[i], Prepare[i]);
//	        		ObjSurplus.addTerm(costSurplus[i], Surplus[i]);
//	        		ObjShortage.addTerm(costShortage[i], Shortage[i]);	        		
	        }
	        
	        IloLinearNumExpr expr = cplex.linearNumExpr();
	        
	        // For each scenario, i.e. number of guests
	        
	        for (int i = 0; i < nDishes; i++) {
	        		
	        		for (int j = 0; j < scenarios; j++) {
	        			double coefficientSurplus = pGuestDist[j] * costSurplus[i];
	        			double coefficientShortage = pGuestDist[j] * costShortage[i];
	        			
	        			ObjSurplus.addTerm(Surplus[j], coefficientSurplus);
	        			ObjShortage.addTerm(Shortage[j], coefficientShortage);
	        		}
	        }
	        
	        
//	        		cplex.addMinimize(cplex.sum(ObjNumDishes, cplex.prod(pGuestDist, ObjSurplus), cplex.prod(pGuestDist, ObjShortage)));
	        		
	        		cplex.addMinimize(cplex.sum(ObjNumDishes, ObjSurplus, ObjShortage));
	        		
	        
	        
	        
	        // Constraints
	        // For each guests, surplus - shortage = number of plates - guests

	        for (int i = 0; i < guests.length; i++) {
	        		for(int j = 0; j < nDishes; j++) {
	        			
	        			// Surplus[j] - Shortage[j] = Prepare[j] - guests[i]

	        			cplex.addEq(cplex.diff(Surplus[i], Shortage[i]), cplex.diff(Prepare[j], guests[i]));


	        			// Note that here, we can specify veggies and normal guests dishes. o.e., diesh 1 and 2 for Veggies and 3 and 4 for Normal
	        		}
	        }
	        
	        // Solve
	        
	        if (cplex.solve()) {
	        		System.out.println("Solution Status: " + cplex.getStatus());
	        		System.out.println();
	        		System.out.println("Total Cost = " + cplex.getObjValue());
	        		
	        		System.out.println();
	        		System.out.println("\ti \ts \t Prepare \tSurplus \t\tShortage");
	        		for (int i= 0; i < nDishes; i++) {
	        			for (int s = 0; s < scenarios; s++) {
	        				System.out.println("\t" + (i+1) + 
	        					"\t"	 + (s+1) +
	        					"\t " + cplex.getValue(Prepare[i]) +
	        					"\t\t" + cplex.getValue(Surplus[s]) + 
	        					"\t\t" + cplex.getValue(Shortage[s]));
	        			}
	        		}
	        }
	        else {
	        	System.out.println("No solution found!");
	        }
	        cplex.end();
	         
		}
		catch (IloException exc) {
	         System.err.println("Concert exception '" + exc + "' caught");
	      }
	      catch (java.io.IOException exc) {
	    	  System.out.println("File not found");
	         System.err.println("Error reading file " + args[0] + ": " + exc);
	         
	      }
	      catch (InputDataReader.InputDataReaderException exc) {
	         System.err.println(exc);
	      }
	}
}
