/** ---------------------------------------------------------------------------
 * File: Wedding.java														*
 * Version 3																*
 * ----------------------------------------------------------------------------
 * A project for the COMP 567 Class at McGill								*
 * Describes a stochastic wedding feast problem								*
 * Author: Olamilekan Fadahunsi												*
 * 																			*
 * ----------------------------------------------------------------------------
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
	
	static double[][] amountIngredients;
	static double[] probGuestDist;
	static double[] guests;
	static double[] probVegDist;
	static double[] vegetarians;
	
	static double[] price;
	static double[] HoursPerDish;
	static double wage;
	
	static double[] tableCost;
	static int[] tableSize;
	static double penaltyTable;
	
	static int typesTable;
	static DecimalFormat df = new DecimalFormat("#0.0000");
	
	static void readData(String fileName) throws java.io.IOException, InputDataReader.InputDataReaderException {
		
		InputDataReader reader = new InputDataReader(fileName);
		
		amountIngredients		=	reader.readDoubleArrayArray(); // amount of ingredients per dish
		probGuestDist 	=	reader.readDoubleArray();
		guests		=	reader.readDoubleArray();
		probVegDist		=	reader.readDoubleArray();
		vegetarians			=	reader.readDoubleArray();
		
		price		=	reader.readDoubleArray();
		HoursPerDish	=	reader.readDoubleArray();
		wage			=	reader.readDouble();	
		
		tableCost	=	reader.readDoubleArray();
		tableSize	=	reader.readIntArray();
		penaltyTable	=	reader.readDouble();
		
		nDishes = HoursPerDish.length;
		nIgred = price.length;
		scenarios  = guests.length;
		scenarios2 = vegetarians.length;
		typesTable = tableSize.length;  
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
	         
	         
	         IloNumVar[] numTablesVeg = new IloNumVar[typesTable];	// Number of tables to be bought for each group size
	         for (int i = 0; i < typesTable; i++) {
	        	 	numTablesVeg[i] = cplex.intVar(0, Integer.MAX_VALUE);

	         }
	         
	         IloNumVar[] numTablesNonVeg = new IloNumVar[typesTable];	// Number of tables to be bought for each group size
	         for (int i = 0; i < typesTable; i++) {
	        	 	numTablesNonVeg[i] = cplex.intVar(0, Integer.MAX_VALUE);

	         }
	         
	         
	         // Surplus: you have a table that is partially filled
	         
	         IloNumVar[][] tableSurplus = new IloNumVar[scenarios][scenarios2];
	         IloNumVar[][] tableSurplusN = new IloNumVar[scenarios][scenarios2];
	         for (int i = 0; i < scenarios; i++) {
	        	 	for(int j = 0; j < scenarios2; j++) {
					tableSurplus[i][j] = cplex.numVar(0, Double.MAX_VALUE);
					tableSurplusN[i][j] = cplex.numVar(0, Double.MAX_VALUE);
	        	 	}
	         }
	         
	         // Shortage: people not assigned a seat, left standing 
	         IloNumVar[][] tableShortage = new IloNumVar[scenarios][scenarios2];
	         IloNumVar[][] tableShortageN = new IloNumVar[scenarios][scenarios2];
	         for (int i = 0; i < scenarios; i++) {
	        	 	for (int j = 0; j < scenarios2; j++) {
					tableShortage[i][j] = cplex.numVar(0, Double.MAX_VALUE);
					tableShortageN[i][j] = cplex.numVar(0, Double.MAX_VALUE);
	        	 	}
	         }
	         
	         // Surplus amount of plates prepared per dish
	         IloNumVar[][][] surplus = new IloNumVar[nDishes][scenarios][scenarios2];
	         
	         for (int i = 0; i < nDishes; i++) {
	        	 	for (int j = 0; j< scenarios; j++) {
	        	 		for (int k = 0; k < scenarios2; k++) {

	        	 		surplus[i][j][k] = cplex.numVar(0, Double.MAX_VALUE);
	        	 		}
	         	}
			}
	         
	         
	         // Shortage amount of food prepared per dish
	         IloNumVar[][][] shortage = new IloNumVar[nDishes][scenarios][scenarios2];
	         
	         for (int i = 0; i < nDishes; i++) {
	        	 	for (int j = 0; j < scenarios; j++) {
	        	 		for (int k = 0; k < scenarios2; k++) {
	        	 			shortage[i][j][k] = cplex.numVar(0, Double.MAX_VALUE);

	        	 		}
	        	 	}
	         }
	         
	         // Cost of each dish
	         double[] cost = new double[nDishes];
	         
	         for (int i = 0; i < nDishes; i++) {
	        	 	cost[i] = 0;
	 			for (int j = 0; j < nIgred; j++) {
	 				cost[i] += amountIngredients[i][j] * price[j];
	 			}
	 			System.out.println("Cost [" + (i+1) + "]: " + cost[i]);
	        }	
	 			
	         System.out.println();
	 		// Man hours cost i.e, cost per hour of cooking a dish
	 		double[] manHourCost = new double[nDishes]; 

	 		for (int k = 0; k < HoursPerDish.length; k++) {
	 			manHourCost[k] = HoursPerDish[k] * wage;
	 			System.out.println("wage [" + (k+1) + "]: " + manHourCost[k]);
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
	 		IloLinearNumExpr objTableSurplusN = cplex.linearNumExpr();
	 		IloLinearNumExpr objTableShortage = cplex.linearNumExpr();
	 		IloLinearNumExpr objTableShortageN = cplex.linearNumExpr();
	 		
	 		for (int i =0; i < typesTable; i++) {
	 			nTables.addTerm(numTablesVeg[i], tableCost[i]);
	 			nTables.addTerm(numTablesNonVeg[i], tableCost[i]);
	 		}
	 		

	 		for (int j = 0; j < scenarios; j++) {
	 			for (int k = 0; k < scenarios2; k++) {
	 				double coefficient = probGuestDist[j] * probVegDist[k] * penaltyTable;
	 				
	 				objTableSurplus.addTerm(tableSurplus[j][k], coefficient);
		 			objTableShortage.addTerm(tableShortage[j][k], coefficient);
		 			
		 			double coefficientN = probGuestDist[j] * (1-probVegDist[k]) * penaltyTable;
		 			objTableSurplusN.addTerm(tableSurplusN[j][k], coefficientN);
		 			objTableShortageN.addTerm(tableShortageN[j][k], coefficientN);
	 			}
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
		        		for (int k = 0; k < probVegDist.length; k++) {	    
		        			

		        				 coefficientSurplus = probGuestDist[j] * probVegDist[k] * costSurplus[i];
		        				 coefficientShortage = probGuestDist[j] * probVegDist[k] * costShortage[i];

		        				 objSurplus.addTerm(surplus[i][j][k], coefficientSurplus);
		        				 objShortage.addTerm(shortage[i][j][k], coefficientShortage);

		        		}
		        }
		     } 
		     
			cplex.addMinimize(cplex.sum(objNumDishes, nTables, objSurplus, objShortage, objTableSurplus, objTableShortage, objTableSurplusN, objTableShortageN));


		     
		 	//  Vegetarian and Non-vegetarian Constraints 
		 	
		 
		 	
		 	for (int i = 0; i < nDishes; i++) {
		 		for(int j = 0; j < scenarios; j++) {
		 			for(int k = 0; k < scenarios2; k++) {
		 				
		 				double numVegetarians = Math.ceil(guests[j] * vegetarians[k]);
		 				double numNonVeg = guests[j] - numVegetarians;
		 				
		 				if ( i == 0 || i == 1) { // it is a vegetarian food
		 					cplex.addEq(cplex.diff(surplus[i][j][k], shortage[i][j][k]), cplex.diff(Prepare[i], numVegetarians));
		 				}
		 				else { // It is non-vegetarian food
		 					cplex.addEq(cplex.diff(surplus[i][j][k], shortage[i][j][k]), cplex.diff(Prepare[i], numNonVeg));
		 				}
		 			}
		 		}
		 	}
		 	

		 	// Tables Constraint
		 	
		        
		        IloLinearNumExpr exprVeg = cplex.linearNumExpr();
		        for (int i = 0; i < typesTable; i++) {
		        		exprVeg.addTerm(numTablesVeg[i], tableSize[i]); 
		        }
		        
		        IloLinearNumExpr exprNonVeg = cplex.linearNumExpr();
		        for (int i = 0; i < typesTable; i++) {
		        		exprNonVeg.addTerm(numTablesNonVeg[i], tableSize[i]); 
		        }
		           
		        for (int i = 0; i < typesTable; i++) {
		        		for (int j = 0; j < scenarios; j++) {
		        			for (int k = 0; k < scenarios2; k++) {
		        				
		        				double numVegetarians = Math.ceil(guests[j] * vegetarians[k]);
				 			double numNonVeg = guests[j] - numVegetarians;

		        				cplex.addEq(cplex.diff(tableSurplus[j][k], tableShortage[j][k]), cplex.diff(exprVeg, numVegetarians));	
		        				cplex.addEq(cplex.diff(tableSurplusN[j][k], tableShortageN[j][k]), cplex.diff(exprNonVeg, numNonVeg));	
		        			}
		        		}		        		
		        }
	 	
		        // There must be at least one table for either veggies or non veggies

		        IloLinearNumExpr tableOneConstraintVeg = cplex.linearNumExpr();
		        IloLinearNumExpr tableOneConstraintNonVeg = cplex.linearNumExpr();

		        for (int i = 0; i < typesTable; i++) {
		        		tableOneConstraintVeg.addTerm(numTablesVeg[i], 1);
		        		tableOneConstraintNonVeg.addTerm(numTablesVeg[i], 1);
		        }
		        
		        cplex.addGe(tableOneConstraintVeg, 1);
		        cplex.addGe(tableOneConstraintNonVeg, 1); 
		 	// Fridge Capacity constraint
		 	
		 	IloLinearNumExpr mayo = cplex.linearNumExpr();
		 	IloLinearNumExpr ketchup = cplex.linearNumExpr();
		 	IloLinearNumExpr beef = cplex.linearNumExpr();
		 	for(int i = 0; i < nDishes; i++) {
		 		mayo.addTerm(amountIngredients[i][22], Prepare[i]);
		 		ketchup.addTerm(amountIngredients[i][23], Prepare[i]);
		 		beef.addTerm(amountIngredients[i][21], Prepare[i]);
		 	}
		 	
		 	cplex.addLe(cplex.sum(beef, cplex.sum(mayo,ketchup)), 5); 
		 	
		 	
	        // Solve
		 	/** Note that extensive output is written to file "results.txt" while minimal output is shown on the console
		 	 * */
		    	
	        if (cplex.solve()) {
	        	FileWriter results = new FileWriter("weddingResults.txt", false);
	        		results.write("Solution Status: " + cplex.getStatus() + "\n");
	        		results.write("Total Cost = " + df.format(cplex.getObjValue()) + "\n");
	        		results.write("\tDType  \tGScenario  \tVScenario \t  NumPlates \t\tSurplus \t\tShortage\n");

	        		System.out.println("Solution Status: " + cplex.getStatus());
	        		System.out.println();
	        		System.out.println("Total Cost = " + df.format(cplex.getObjValue()));
	        		
	        		System.out.println();
	        		for (int i= 0; i < nDishes; i++) {
	        			for (int s = 0; s < scenarios; s++) {
	        				for (int c = 0; c < scenarios2; c++) {
	        				
	        				results.write("\t" + (i+1) + 
	        					"\t\t\t"	 + (s+1) + 
	        					"\t\t\t"	 + (c+1) +
	        					
	        					"\t\t\t" + cplex.getValue(Prepare[i]) +        					
	        					"\t\t\t" + df.format(cplex.getValue(surplus[i][s][c])) + 
	        					"\t\t\t" + df.format(cplex.getValue(shortage[i][s][c])) + "\n");
	        				}
	        			}
	        		}
	        		
	        		results.write("\n\n Vegetarians \n");
	        		
	        		for (int j = 0; j < typesTable; j++)
	        			for (int s = 0; s < scenarios; s++) {
	        				for (int c = 0; c < scenarios2; c++) {
	        					results.write("\t" + (j+1) + 
	        							"\t\t\t" + df.format(cplex.getValue(numTablesVeg[j])) +
	    	        					"\t\t\t" + df.format(cplex.getValue(tableSurplus[s][c])) +
	    	        					"\t\t\t" + df.format(cplex.getValue(tableShortage[s][c])) + "\n");
	        			}
	        		}
	        		
	        		
	        		results.write("\n\n Non Vegetarians \n");
	        		
	        		for (int j = 0; j < typesTable; j++)
	        			for (int s = 0; s < scenarios; s++) {
	        				for (int c = 0; c < scenarios2; c++) {
	        					
	        					double numVegetarians = Math.ceil(guests[s] * vegetarians[c]);
					 			double numNonVeg = guests[s] - numVegetarians;
	        					results.write("\t" + (j+1) + 
	        							"\t\t\t" + df.format(cplex.getValue(numTablesNonVeg[j])) +
	    	        					"\t\t\t" + df.format(cplex.getValue(tableSurplusN[s][c])) +
	    	        					"\t\t\t" + df.format(cplex.getValue(tableShortageN[s][c]))  +
	    	        					"\t\t" + numVegetarians + "\t\t" + numNonVeg + "\n");
	        			}
	        		}
	        		
	        	
	        		System.out.println("Dishes Types");
	        		for (int i = 0; i < nDishes; i++) {	        			
	        			System.out.println( "x[" + (i+1) + "]: " + cplex.getValue(Prepare[i]));
	        		}
	        		
	        		System.out.println();
	        		System.out.println("Table Types - Veggies");
	        		for (int j = 0; j < typesTable; j++) {
	        			System.out.println("t[" + (j+1) +"]Veg: " + cplex.getValue(numTablesVeg[j]) );
	        		}
	        		
	        		System.out.println();
	        		System.out.println("Table Types - Non Veggies");
	        		for (int j = 0; j < typesTable; j++) {
	        			System.out.println("t[" + (j+1) +"]NonVeg: " + cplex.getValue(numTablesNonVeg[j]) );
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
