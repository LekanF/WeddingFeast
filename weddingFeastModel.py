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
model.ingredientAmount = Param(model.I, model.R)

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

## a little bit different parameter, i.e. the demand for the dishes
## and general demand for the tables (# people)
model.demand = Param(model.I, model.Omega, within=NonNegativeIntegers)
model.demandV = Param(model.Omega, within=PositiveIntegers)
model.demandNV = Param(model.Omega, within=PositiveIntegers)
## corresponding probabilities
model.prob = Param(model.Omega, within=PositiveReals)

model.fridgeIngs = Set()

'''
State model's variables x - number ofparticular dishes, y - number of 
particular tables for veg/non-veg (w and v are for unmet demand/surplus)
'''
model.x = Var(model.I, domain=PositiveIntegers)
model.y1 = Var(model.J, domain=NonNegativeIntegers)
model.y2 = Var(model.J, domain=NonNegativeIntegers)
model.wP = Var(model.I, model.Omega, domain=NonNegativeIntegers)
model.wN = Var(model.I, model.Omega, domain=NonNegativeIntegers)
model.vP1 = Var(model.Omega, domain=NonNegativeIntegers)
model.vN1 = Var(model.Omega, domain=NonNegativeIntegers)
model.vP2 = Var(model.Omega, domain=NonNegativeIntegers)
model.vN2 = Var(model.Omega, domain=NonNegativeIntegers)

'''
Specify the constraints. Due to giving domains for variables there
are just three for a relationship between surplus and shortage in 
dishes and tables, respectively, the fridge capacity constraint, 
and at least one table constraint.
'''
def generationRule(model, i, omega):
	return model.wP[i, omega] - model.wN[i, omega] == model.x[i] - model.demand[i, omega]

model.wPwN = Constraint(model.I, model.Omega, rule=generationRule)

def generationRuleV(model, omega):
	return model.vP1[omega] - model.vN1[omega] == sum(model.tableSize[j] * model.y1[j] for j in model.J) - model.demandV[omega]

model.vPvN1 = Constraint(model.Omega, rule=generationRuleV)

def generationRuleNV(model, omega):
	return model.vP2[omega] - model.vN2[omega] == sum(model.tableSize[j] * model.y2[j] for j in model.J) - model.demandNV[omega]

model.vPvN2 = Constraint(model.Omega, rule=generationRuleNV)

def generationRule3(model):
	return sum(model.x[i]*sum(model.ingredientAmount[i,s] for s in model.fridgeIngs) for i in model.I) <= 5000

model.con = Constraint(rule=generationRule3)

def generationRuleT1(model):
	return sum(model.y1[j] for j in model.J) >= 1

model.tab1 = Constraint(rule=generationRuleT1)

def generationRuleT2(model):
	return sum(model.y2[j] for j in model.J) >= 1

model.tab2 = Constraint(rule=generationRuleT2)

'''
Define the objective function
'''
def objectiveExpr(model):
	return summation(model.manhourCost, model.x) + summation(model.dishCost, model.x) \
		+ sum(model.prob[omega] * sum(model.penaltyPlus[i] * model.wP[i, omega] \
		+ model.penaltyMinus[i] * model.wN[i, omega] for i in model.I) for omega in model.Omega) 
		+ summation(model.tableCost, model.y1) + summation(model.tableCost, model.y2) \
		+ model.penaltyTable * sum(model.prob[omega] * (model.vP1[omega] + model.vN1[omega]) for omega in model.Omega) \
		+ model.penaltyTable * sum(model.prob[omega] * (model.vP2[omega] + model.vN2[omega]) for omega in model.Omega)

model.objective = Objective(sense=minimize, rule=objectiveExpr)

## data specification is in another file modelData.dat
