import ilog.concert.*;
import ilog.cplex.*;

public class Feast {

	
	public static void solveMe() {
		
		int numberDishes = 4; // number of dishes
		int numberIngredients = 26;
/*		
		double[] dish1_ingredients = { 1, 1, 1/2, 1/2, 3/4, 2, 1/3 }; // serves 8 people
		double[] dish2_ingredients = { 1, 1, 1, 2, 1, 1, 8, 8}; // serves 8 people
		double[] dish3_ingredients = { 9.5, 1.5, 7, 1, 3/4, 1.25, 1/2, 1/4 }; // serves 6 people
		double[] dish4_ingredients = { 3, 1/2, 3, 1, 1, 4 };
		
		// Amount of ingredients per dish
		double[] amount_dish1 = {0, 0, 0, 0, 1, 0, 1/2, 1/2, 0, 0 , 3/4, 0, 2, 1/3, 0, 1, 0 ,1 ,0 ,0 ,0 ,0 ,0 ,0, 0, 0 };
		double[] amount_dish2 = {0 ,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 1, 1, 8, 8  };
		double[] amount_dish3 = {9.5, 1.5, 7, 1, 0 ,3/4, 0, 0, 1.25, 1/2, 0, 1/4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] amount_dish4 = {0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 1/2, 0, 0, 3, 0, 3, 0, 1, 1, 0, 0, 0, 0, 0, 0 }; 
*/		
		// Amount of ingredients using double array
		double[][] amount = {{0, 0, 0, 0, 1, 0, 1.0/2, 1.0/2, 0, 0 , 3.0/4, 0, 2, 1.0/3, 0, 1, 0 ,1 ,0 ,0 ,0 ,0 ,0 ,0, 0, 0 },
				{0 ,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 1, 1, 8, 8  },
				{9.5, 1.5, 7, 1, 0 ,3.0/4, 0, 0, 1.25, 1.0/2, 0, 1.0/4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 1.0/2, 0, 0, 3, 0, 3, 0, 1, 1, 0, 0, 0, 0, 0, 0 }};
		
		
		/* Real Data Below.. not in use now
		// Distribution of guests - -guests range from 87 - 113
		double[] percentGuestsDistribution = {0.01, 0.01, 0.01, 0.02, 0.02, 0,03, 0.04, 0.04, 0.05, 0.06, 0.06, 0.07, 0.07, 0.07, 0.07, 0.06, 0.06, 0.05, 0.05, 0.04, 0.03, 0.02};
		double[] guests = {87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
				111, 112, 113};
		// Distribution of vegetarians - Vegetarians range from 1% to 11%
		double[] percentVegetarianDistribution = { 0.03, 0.16, 0.21, 0.19, 0.15, 0.11, 0.07, 0.04, 0.02, 0.01, 0.01};
		double[] vegetarians = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
		*/

		// Sampole Data in Use
		double[] guests1 = {870, 880, 890, 900, 910};
		double[] percentGuestsDistribution1 = {0.1, 0.2, 0.3, 0.2, 0.2};
		
		double[] vegetarians1 = {0.1, 0.2, 0.3, 0.4, 0.5};
		double[] percentVegetarianDistribution1 = {0.1, 0.35, 0.15, 0.3, 0.1 };
		
		/* 
		 * Ingredients
		 * Butter, Mushrooms, Chicken Broth, Extra Virgin oil, Pecans
		 * Leek, Sugar, Dry mustard, arborio rice, white wine 
		 * Vegetable oil, Parmesian cheese, Poopy seeds, Roland red wine vinegar
		 * Potatoes, Spinach, Truffle Oil, Strawberries, Pepper 
		 * Salt, Ground beef, mayonnaise, ketchup, sweet pickle relish
		 * Burger buns, Plum tomatoes
		 * */
		// Price for ingredients
		/*double butterCost = 4.77; double mushroomCost = 2.99; double chicBrothCost = 2.67; double virginOliveOilCost = 8.28;
		double pecansCost = 10.37; double leekCost = 4.99; double sugarCost = 2.79; double dryMustardCost = 6.74;
		double arborioRiceCost = 3.97; double whiteWineCost = 16.65; double VegetableOilCost = 5.67; double parmesanCheeseCost = 3.88;
		double poppySeedCost = 3.97; double redWineCost = 10.67; double potatoesCost = 5.49; double spinachCost = 6.99;
		double truffleOilCost = 37.97; double strawberriesCost = 4.45; double pepperCost = 3.06; double saltCost = 1.59;
		double groundBeefCost = 1.56; double mayonnaiseCost = 1.03; double ketchupCost = 0.73; double sweetPickleCost = 1.0;
		double burgerBunsCost = 0.59; double plumTomatoesCost = 3.49;
		*/
		
		double[] price = {4.77, 2.99, 2.67, 8.28, 10.37, 4.99, 2.79, 6.74, 3.97, 16.65, 5.67, 3.88, 3.97, 10.67, 5.49, 6.99, 37.97, 4.45,
				3.06, 1.59, 1.56, 1.03, 0.73, 1.0, 0.59, 3.49};
		double[] numberHoursPerDish = {1, 1, 6.0/8, 4.0/8};
		double wagePerHour = 12.72;
		

		// Cost of Each dish
		double costOfDish[] = new double [4];

		for (int i = 0; i < numberDishes; i++) {
			for (int j = 0; j < numberIngredients; j++) {
				costOfDish[i] += amount[i][j] * price[j];
			}
			
			System.out.println("Cost of dish " + (i+1) + " = " + costOfDish[i]);
/*			if (i == 0 || i == 1) {
				costOfDish[i] /= 8;
			}
			else if(i == 2)
				costOfDish[i] /= 6;
			else
				costOfDish[i] /= 4;

			System.out.println("New Cost of dish " + (i+1) + " = " + costOfDish[i]);
*/
		}
		
		// Man hours cost i.e, cost per hour of cooking a dish
		double[] manHourCost = new double [4];
		for (int k = 0; k < numberHoursPerDish.length; k++) {
			manHourCost[k] = numberHoursPerDish[k] * wagePerHour;
		}
		
//		for (int k = 0; k < 4; k++) {
//			for (int l = 0; l < 26; l++)
//				System.out.print(amount[k][l] + " ");
//			System.out.println();
//		}
		
		try {
			// define new model
			IloCplex cplex = new IloCplex();
			
			// variables
			IloNumVar [] x = new IloNumVar[numberDishes];
			
			for (int i = 0; i < numberDishes; i++) {
				x[i] = cplex.numVar(1, Double.MAX_VALUE);
			}
			
			/* // omega variables  - wrong
			IloNumVar[][] wPositive = new IloNumVar[numberDishes][];
			IloNumVar[][] wNegative = new IloNumVar[numberDishes][];

			for (int i = 0; i < numberDishes; i++) {
				wPositive[i] = cplex.numVarArray(scenarios, 0, Double.MAX_VALUE);
				wNegative[i] = cplex.numVarArray(scenarios, 0, Double.MAX_VALUE);
			}
			*/
			
			// Expression
			
			
			// Objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			
			for (int i = 0; i < numberDishes; i++) {
				objective.addTerm(manHourCost[i], x[i]);
				objective.addTerm(costOfDish[i], x[i]);
			}
			cplex.addMinimize(objective);
			
			
			// Add constraints - number of food must meet number of guests
			IloLinearNumExpr expr = cplex.linearNumExpr();
			/* for (int i = 0; i < numberDishes; i++) {
				for (int g = 0; g < guests.length; g++) {
					expr.addTerm(percentGuestsDistribution[g] * guests[g], x[i]); 
				}
				cplex.addEq(expr, x[i]);
			}
			*/
			

			// Vegetarians 
			for (int j = 0; j < numberDishes; j++) {
				double sum = 0;
				expr.clear();
				if (j != 1 || j != 0) { // It is a non vegetarian food i.e., j = 1
					// for the  distribution of vegetarians
					for (int g = 0; g < guests1.length; g++) {
						for (int v = 0; v < vegetarians1.length; v++) {
							double numVeggies = percentGuestsDistribution1[g] * guests1[g] * percentVegetarianDistribution1[v] * vegetarians1[v];
							expr.addTerm(numVeggies, x[j]); 
							sum += numVeggies;
//							cplex.addEq(expr, percentGuestsDistribution[g] * guests[g] * percentVegetarianDistribution[v] * vegetarians[v]);
						}
					}
					cplex.addEq(expr, sum);
				}
			}
			
			// Normal guests
			for (int d = 0; d < numberDishes; d++) {
				expr.clear(); double sum = 0;
				if (d != 2 || d != 3) { // It is a vegetarian food
					for (int g = 0; g < guests1.length; g++) {
						for (int v = 0; v < vegetarians1.length; v++) {
							double numNonVeggie = (percentGuestsDistribution1[g] * guests1[g]) - (percentGuestsDistribution1[g] * guests1[g] * percentVegetarianDistribution1[v] * vegetarians1[v]);
							expr.addTerm(numNonVeggie, x[d]); 
							sum += numNonVeggie;
						}
					}
					cplex.addEq(expr, sum);
				}
			}
			
			
			// combined
/*						for (int d = 0; d < numberDishes; d++) {
							expr.clear(); double sum = 0;
							if (d != 2 || d != 3) { // It is a vegetarian food
								for (int g = 0; g < guests1.length; g++) {
									for (int v = 0; v < vegetarians1.length; v++) {
										double numNonVeggie = (guests1[g]) - ( guests1[g] * vegetarians1[v]);
										expr.addTerm(numNonVeggie, x[d]); 
										sum += numNonVeggie;
										cplex.addEq(expr, sum);
									}
								}
//								cplex.addEq(expr, sum);
							}
							else {
								for (int g = 0; g < guests1.length; g++) {
									for (int v = 0; v < vegetarians1.length; v++) {
										double numVeggies = guests1[g] * vegetarians1[v];
										expr.addTerm(numVeggies, x[d]); 
										sum += numVeggies;
										cplex.addEq(expr, sum);
//										cplex.addEq(expr, percentGuestsDistribution[g] * guests[g] * percentVegetarianDistribution[v] * vegetarians[v]);
									}
								}
//								cplex.addEq(expr, sum);
							}
						}
			*/
			
			
			// Model
			
			// Solve
			cplex.setParam(IloCplex.Param.Benders.Strategy, -1);
			/** strategy for benders
			 * -1 = OFF: Uses standard branch and cut and no benders
			 * 0 = AUTO: If no annotations present, does standard branch and cut, otherwise sets up benders using user supplied annotations
			 * 1 = USER: Decompose problem using users annotation, if users fail to supply annotation, throws an exception
			 * 2 = WORKER: Gives cplex permission to partition into sub problems as it sees fit
			 * 3 = FULL: ignores users annotation and attempts to decompose the model, if either all variables are integers or non of them are, an exception is thrown
			*/
			if (cplex.solve()) {
				System.out.println("Solution Found");
				System.out.println("Obj = " + cplex.getObjValue());
				
				for (int i = 0; i < numberDishes; i++) {
					System.out.println("x[" + (i+1) + " ] = " + cplex.getValue(x[i]));
				}
			}
			else {
				System.out.println("Solution not Found");
			}
			// End
			cplex.end();
		}
		catch(IloException e){
			e.printStackTrace();
		}
	}

}
