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
import java.text.DecimalFormat;

public class Wedding {

	static int nDishes;
	static int nIgred;
	static int scenarios, scenarios2;
	
	static double[][] amount;
	static double[] pGuestDist;
	static double[] guests;
	static double[] pVegDist;
	static double[] veg;
	
	static double[] price;
	static double[] HoursPerDish;
	static double wage;
	
	static double[] tableCost;
	static int[] tableSize;
	static double penaltyTable;
	
	static int typesTable;
	static DecimalFormat df = new DecimalFormat("#0.0");
	
	static void readData(String fileName) throws java.io.IOException, InputDataReader.InputDataReaderException {
		
		InputDataReader reader = new InputDataReader(fileName);
		
		amount		=	reader.readDoubleArrayArray(); // amount of ingredients per dish
		pGuestDist 	=	reader.readDoubleArray();
		guests		=	reader.readDoubleArray();
		pVegDist		=	reader.readDoubleArray();
		veg			=	reader.readDoubleArray();
		
		price		=	reader.readDoubleArray();
		HoursPerDish	=	reader.readDoubleArray();
		wage			=	reader.readDouble();	
		
		tableCost	=	reader.readDoubleArray();
		tableSize	=	reader.readIntArray();
		penaltyTable	=	reader.readDouble();
		
		nDishes = HoursPerDish.length;
		nIgred = price.length;
		scenarios  = guests.length;
		scenarios2 = veg.length;
		typesTable = tableSize.length; // 
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
	        	 	Prepare[i] = cplex.intVar(1, Integer.MAX_VALUE);
	         }
	         
	         IloNumVar[] numTables = new IloNumVar[typesTable];	// Number of tables to be bought for each group size
	         for (int i = 0; i < typesTable; i++) {
	        	 	numTables[i] = cplex.intVar(0, Integer.MAX_VALUE);

	         }
	         
//	         // Surplus plates prepared per dish 
//	         IloNumVar[] Surplus = new IloNumVar[scenarios];
//	        
//	         for (int i = 0; i < scenarios; i++) {
//					Surplus[i] = cplex.numVar(0, Double.MAX_VALUE);
//	         }
	         
	         // Surplus: you have a table that is partially filled
	         
	         IloNumVar[] tableSurplus = new IloNumVar[scenarios];
	         for (int i = 0; i < scenarios; i++) {
					tableSurplus[i] = cplex.numVar(0, Double.MAX_VALUE);
	         }
	         
	         // Shortage: people not assigned a seat, left standing 
	         IloNumVar[] tableShortage = new IloNumVar[scenarios];
	         for (int i = 0; i < scenarios; i++) {
					tableShortage[i] = cplex.numVar(0, Double.MAX_VALUE);
	         }
	         
	         // Surplus amount of plates prepared per dish
	         IloNumVar[][][] dSurplus = new IloNumVar[nDishes][scenarios][scenarios2];
	         
	         for (int i = 0; i < nDishes; i++) {
	        	 	for (int j = 0; j< scenarios; j++) {
	        	 		for (int k = 0; k < scenarios2; k++) {

	        	 		dSurplus[i][j][k] = cplex.numVar(0, Double.MAX_VALUE);
	        	 		}
	         	}
			}
	         
//	         // Shortage of plates prepared 
//	         IloNumVar[] Shortage = new IloNumVar[scenarios];
//	         for (int i = 0; i < scenarios; i++) {
//					Shortage[i] = cplex.numVar(0, Double.MAX_VALUE);
//	         }
	         
	         // Shortage amount of food prepared per dish
	         IloNumVar[][][] dShortage = new IloNumVar[nDishes][scenarios][scenarios2];
	         
	         for (int i = 0; i < nDishes; i++) {
	         for (int j = 0; j < scenarios; j++) {
	        	 	for (int k = 0; k < scenarios2; k++) {
	        	 		dShortage[i][j][k] = cplex.numVar(0, Double.MAX_VALUE);
	        	 	}
	         }
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
	 		
	 		
	 		// Tables Objective 
	 		IloLinearNumExpr nTables = cplex.linearNumExpr();
	 		IloLinearNumExpr objTableSurplus = cplex.linearNumExpr();
	 		IloLinearNumExpr objTableShortage = cplex.linearNumExpr();
	 		
	 		for (int i =0; i < typesTable; i++) {
	 			nTables.addTerm(numTables[i], tableCost[i]);
	 		}
	 		
	 		// for each guests scenario
	 		for (int j = 0; j < scenarios; j++) {
	 			double coefficient = pGuestDist[j] * penaltyTable;
	 			
	 			objTableSurplus.addTerm(tableSurplus[j], coefficient);
	 			objTableShortage.addTerm(tableShortage[j], coefficient);
	 		}
	 		
	       	
	 		//  Dish Objective 
	        IloLinearNumExpr objNumDishes = cplex.linearNumExpr();
	        IloLinearNumExpr objSurplus = cplex.linearNumExpr();
	        IloLinearNumExpr objShortage = cplex.linearNumExpr();
	 		
	        for (int i = 0 ; i < nDishes; i++) {
	        		objNumDishes.addTerm(manHourCost[i], Prepare[i]);
	        		objNumDishes.addTerm(cost[i], Prepare[i]);	        		
	        }
	        
	        double coefficientSurplus = 0; 
			double coefficientShortage = 0;
		     for (int i= 0 ; i < nDishes; i++) {   
		        for (int j = 0; j < guests.length; j++) {
		        		for (int k = 0; k < pVegDist.length; k++) {	    
		        			
		        			if (i == 0 || i == 1) {
		        				 coefficientSurplus = pGuestDist[j] * pVegDist[k] * costSurplus[i];
		        				 coefficientShortage = pGuestDist[j] * pVegDist[k] * costSurplus[i];
		        			}
		        			else {
			        			 coefficientSurplus = pGuestDist[j] * (1-pVegDist[k]) * costSurplus[i];
			        			 coefficientShortage = pGuestDist[j] * (1-pVegDist[k]) * costSurplus[i];
		        			}
		        			objSurplus.addTerm(dSurplus[i][j][k], coefficientSurplus);
		        			objShortage.addTerm(dShortage[i][j][k], coefficientShortage);
		        		}
		        }
		     } 
		     

			cplex.addMinimize(cplex.sum(objNumDishes, nTables, objSurplus, objShortage, objTableSurplus, objTableShortage));

		     
		 	//  Vegetarian and Non-vegetarian Constraints 
		 	
		 	for (int i = 0; i < nDishes; i++) {
		 		for(int j = 0; j < scenarios; j++) {
		 			for(int k = 0; k < scenarios2; k++) {
		 				
		 				double numVegetarians = guests[j] * veg[k];
		 				double numNonVeg = guests[j] - numVegetarians;
		 				
		 				
		 				if ( i == 2 || i == 3 ) { // it is a non-vegetarian food
		 					cplex.addEq(cplex.diff(dSurplus[i][j][k], dShortage[i][j][k]), cplex.diff(Prepare[i], numVegetarians));
		 				}
		 				else{ // It is vegetarian food
		 					cplex.addEq(cplex.diff(dSurplus[i][j][k], dShortage[i][j][k]), cplex.diff(Prepare[i], numNonVeg));
		 				}
		 			}
		 		}
		 	}
		 	
		 	// Tables Constraint
		 	
		 	 IloLinearNumExpr expr = cplex.linearNumExpr();
		        for (int i = 0; i < typesTable; i++) {
		        		expr.addTerm(numTables[i], tableSize[i]); 
		        }
		        for (int i = 0; i < typesTable; i++) {
		        		for (int j = 0; j < scenarios; j++) {
		        			cplex.addEq(cplex.diff(tableSurplus[j], tableShortage[j]), cplex.diff(expr, guests[j]));
		        		}		        		
		        }
	 	
		 	
		 	// Fridge Capacity constraint
		 	
		 	IloLinearNumExpr mayo = cplex.linearNumExpr();
		 	IloLinearNumExpr ketchup = cplex.linearNumExpr();
		 	IloLinearNumExpr beef = cplex.linearNumExpr();
		 	for(int i = 0; i < nDishes; i++) {
		 		mayo.addTerm(amount[i][22], Prepare[i]);
		 		ketchup.addTerm(amount[i][23], Prepare[i]);
		 		beef.addTerm(amount[i][21], Prepare[i]);
		 	}
		 	
		 	
		 	cplex.addLe(cplex.sum(beef, cplex.sum(mayo,ketchup)), 1);
		 	
	        // Solve
			
	        if (cplex.solve()) {
	        	FileWriter results = new FileWriter("weddingResults.txt", false);
	        		results.write("Solution Status: " + cplex.getStatus() + "\n");
	        		results.write("Total Cost = " + cplex.getObjValue() + "\n");
	        		results.write("\tDType  \tGScenario  \tVScenario \tTableType \t  NumTables \t TSurplus \tTShortage \t NumPlates \tSurplus \t\tShortage\n");
	        		
	        		System.out.println("Solution Status: " + cplex.getStatus());
	        		System.out.println();
	        		System.out.println("Total Cost = " + cplex.getObjValue());
	        		
	        		System.out.println();
//	        		System.out.println("\ti \ts \tc \t Prepare \tSurplus \t\tShortage");
	        		System.out.println("\ti  \ts \tc \tj \t  NumTables \t Table Surplus \t Table Shortage \t Prepare \tSurplus \t\tShortage" );
	        		for (int i= 0; i < nDishes; i++) {
	        			for (int j = 0; j < typesTable; j++) {
	        			for (int s = 0; s < scenarios; s++) {
	        				for (int c = 0; c < scenarios2; c++) {
	        				System.out.println("\t" + (i+1) + 
	        					"\t"	 + (s+1) + 
	        					"\t"	 + (c+1) +
	        					"\t" + (j+1) + 
	        					"\t\t" + cplex.getValue(numTables[j]) +
	        					"\t" + cplex.getValue(tableSurplus[s]) +
	        					"\t" + cplex.getValue(tableShortage[s]) +
	        					
	        					"\t " + cplex.getValue(Prepare[i]) +	        					
	        					"\t\t" + cplex.getValue(dSurplus[i][s][c]) + 
	        					"\t\t" + cplex.getValue(dShortage[i][s][c]));
	        				
	        				results.write("\t" + (i+1) + 
	        					"\t\t\t"	 + (s+1) + 
	        					"\t\t\t"	 + (c+1) +
	        					"\t\t\t" + (j+1) + 
	        					
	        					"\t\t\t" + cplex.getValue(numTables[j]) +
	        					"\t\t\t" + cplex.getValue(tableSurplus[s]) +
	        					"\t\t\t" + cplex.getValue(tableShortage[s]) +
	        					
	        					"\t\t\t" + cplex.getValue(Prepare[i]) +        					
	        					"\t\t\t" + df.format(cplex.getValue(dSurplus[i][s][c])) + 
	        					"\t\t\t" + df.format(cplex.getValue(dShortage[i][s][c])) + "\n");
	        				}
	        			}
	        		}
	        }
	        	
	        		System.out.println("Total Cost = " + cplex.getObjValue());
	        		for (int i = 0; i < nDishes; i++) {
	        			System.out.println( "x[" + (i+1) + "]: " + cplex.getValue(Prepare[i]));
	        		}
	        		
	        		for (int j = 0; j < typesTable; j++) {
	        			System.out.println((j+1) +": " + cplex.getValue(numTables[j]) );
	        		}
	        		results.close();
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
