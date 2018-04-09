from __future__ import division
from pyomo.environ import *

model = AbstractModel()

'''
We say that I, J and Omega are some sets of integers that correspond
to our problem indices (I for dishes J for tables, R for ingredients)
Omega depends on scenario and is included to make things easier (by
doing so it becomes a subscript for variables). We assume J includes 
indices that correspond to tables' sizes. Omega2 is for number of
guests distribution only
'''
model.I = Param(within=PositiveIntegers)
model.J = Param(within=PositiveIntegers)
model.R = Param(within=PositiveIntegers)
model.Omega = Param(within=PositiveIntegers)
model.Omega2 = Param(within=PositiveIntegers)

'''
Specify ranges over which parameters change
'''
model.i = RangeSet(1, model.I)
model.j = RangeSet(1, model.J)
model.r = RangeSet(1, model.R)
model.omega = RangeSet(1, model.Omega)
model.omega2 = RangeSet(1, model.Omega2)

'''
Initialize parameters used in constructing the model by specifying 
what are their subscripts and, optionally how to initialize them 
Names should be self-explanatory
'''
model.ingredientCost = Param(model.r, within=PositiveReals)
model.manhourNumber = Param(model.i, within=PositiveReals)
model.oneManhourCost = Param(model.i, within=PositiveReals)

## even though it is an abstract model, we try 
## without value() applied to parameters
def manhInit(model, i):
	return model.manhourNumber[i] * model.oneManhourCost[i]

model.manhourCost = Param(model.i, initialize=manhInit)
model.ingredientAmount = Param(model.i, model.r, within=PositiveReals)

def dishInit(model, i):
	return sum(model.ingredientCost[r] * model.ingredientAmount[i,r] for r in model.r)

model.dishCost = Param(model.i, initialize=dishInit)
model.tableCost = Param(model.j, within=PositiveReals)
model.tableSize = Param(model.j, within=PositiveIntegers)

def surplusInit(model, i):
	return model.dishCost[i] + model.manhourCost[i]

model.penaltyPlus = Param(model.i, initialize=surplusInit)

def defInit(model, i):
	return 1.2*(model.dishCost[i] + model.manhourCost[i])

model.penaltyMinus = Param(model.i, initialize=defInit)
model.penaltyTable = Param(within=PositiveReals)

## a little bit different parameter, i.e. the demand for dishes
## and general demand (# people)
model.demand = Param(model.i, model.omega, within=NonNegativeIntegers)
model.generalDemand = Param(model.omega2, within=PositiveIntegers)
## corresponding probabilities
model.prob = Param(model.omega, within=PositiveReals)
model.generalProb = Param(model.omega2, within=PositiveReals)

'''
State model's variables x - number ofparticular dishes, y - number of 
particular tables (w and v are for unmet demand/surplus)
'''
model.x = Var(model.i, domain=PositiveIntegers)
model.y = Var(model.j, domain=NonNegativeIntegers)
model.wP = Var(model.i, model.omega, domain=NonNegativeIntegers)
model.wN = Var(model.i, model.omega, domain=NonNegativeIntegers)
model.vP = Var(model.omega2, domain=NonNegativeIntegers)
model.vN = Var(model.omega2, domain=NonNegativeIntegers)

'''
Specify the constraints (due to giving domains for variables there
are just two for a relationship between surplus and deficiency in 
dishes and tables, respectively)
'''
def generationRule(model, i, omega):
	return model.wP[i, omega] - model.wN[i, omega] == model.x[i] - model.demand[i, omega]

model.wPwN = Constraint(model.i, model.omega, rule=generationRule)

def generationRule2(model, omega):
	return model.vP[omega] - model.vN[omega] == sum(tableSize[j] * model.y[j] for j in model.j) - model.generalDemand[omega]

model.vPvN = Constraint(model.omega2, rule=generationRule2)

'''
Define the objective function
'''
def objectiveExpr(model):
	return summation(model.manhourCost, model.x) + summation(model.dishCost, model.x) \
		+ sum(model.prob[omega] * sum(model.penaltyPlus[i] * model.wP[i, omega] \
		+ model.penaltyMinus[i] * model.wN[i, omega] for i in model.i) for omega in model.omega) \
		+ summation(model.tableCost, model.y) \
		+ model.penaltyTable * sum(model.generalProb[omega] * (model.vP[omega] + model.vN[omega]) for omega in omega2)

model.objective = Objective(sense=minimize, rule=objectiveExpr)

## data specification is in another file modelData.dat
