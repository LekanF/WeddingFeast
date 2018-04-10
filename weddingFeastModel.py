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
model.I = Set()
model.J = Set()
model.R = Set()
model.Omega = Set()
model.Omega2 = Set()

'''
Initialize parameters used in constructing the model by specifying 
what are their subscripts and, optionally how to initialize them 
Names should be self-explanatory
'''
model.ingredientCost = Param(model.R, within=PositiveReals)
model.manhourNumber = Param(model.I, within=PositiveReals)
model.oneManhourCost = Param(model.I, within=PositiveReals)

## even though it is an abstract model, we try 
## without value() applied to parameters
def manhInit(model, i):
	return model.manhourNumber[i] * model.oneManhourCost[i]

model.manhourCost = Param(model.I, initialize=manhInit)
model.ingredientAmount = Param(model.I, model.R, within=PositiveReals)

def dishInit(model, i):
	return sum(model.ingredientCost[r] * model.ingredientAmount[i,r] for r in model.R)

model.dishCost = Param(model.I, initialize=dishInit)
model.tableCost = Param(model.J, within=PositiveReals)
model.tableSize = Param(model.J, within=PositiveIntegers)

def surplusInit(model, i):
	return model.dishCost[i] + model.manhourCost[i]

model.penaltyPlus = Param(model.I, initialize=surplusInit)

def defInit(model, i):
	return 1.2*(model.dishCost[i] + model.manhourCost[i])

model.penaltyMinus = Param(model.I, initialize=defInit)
model.penaltyTable = Param(within=PositiveReals)

## a little bit different parameter, i.e. the demand for dishes
## and general demand (# people)
model.demand = Param(model.I, model.Omega, within=NonNegativeIntegers)
model.generalDemand = Param(model.Omega2, within=PositiveIntegers)
## corresponding probabilities
model.prob = Param(model.Omega, within=PositiveReals)
model.generalProb = Param(model.Omega2, within=PositiveReals)

'''
State model's variables x - number ofparticular dishes, y - number of 
particular tables (w and v are for unmet demand/surplus)
'''
model.x = Var(model.I, domain=PositiveIntegers)
model.y = Var(model.J, domain=NonNegativeIntegers)
model.wP = Var(model.I, model.Omega, domain=NonNegativeIntegers)
model.wN = Var(model.I, model.Omega, domain=NonNegativeIntegers)
model.vP = Var(model.Omega2, domain=NonNegativeIntegers)
model.vN = Var(model.Omega2, domain=NonNegativeIntegers)

'''
Specify the constraints (due to giving domains for variables there
are just two for a relationship between surplus and deficiency in 
dishes and tables, respectively)
'''
def generationRule(model, i, omega):
	return model.wP[i, omega] - model.wN[i, omega] == model.x[i] - model.demand[i, omega]

model.wPwN = Constraint(model.I, model.Omega, rule=generationRule)

def generationRule2(model, omega):
	return model.vP[omega] - model.vN[omega] == sum(tableSize[j] * model.y[j] for j in model.j) - model.generalDemand[omega]

model.vPvN = Constraint(model.Omega2, rule=generationRule2)

'''
Define the objective function
'''
def objectiveExpr(model):
	return summation(model.manhourCost, model.x) + summation(model.dishCost, model.x) \
		+ sum(model.prob[omega] * sum(model.penaltyPlus[i] * model.wP[i, omega] \
		+ model.penaltyMinus[i] * model.wN[i, omega] for i in model.I) for omega in model.Omega) \
		+ summation(model.tableCost, model.y) \
		+ model.penaltyTable * sum(model.generalProb[omega] * (model.vP[omega] + model.vN[omega]) for omega in model.Omega2)

model.objective = Objective(sense=minimize, rule=objectiveExpr)

## data specification is in another file modelData.dat (which does not exist yet)
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