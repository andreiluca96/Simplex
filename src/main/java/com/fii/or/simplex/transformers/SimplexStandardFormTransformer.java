package com.fii.or.simplex.transformers;

import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.model.OptimizationFunction;
import com.fii.or.simplex.model.Restriction;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class SimplexStandardFormTransformer {
    public static LinearProgramStandardFormTable transformToStandardForm(LinearProgramInputTable linearProgramInputTable) {
        List<Double> objectiveFunctionInStandardForm = transformObjectiveFunction(linearProgramInputTable.getObjectiveFunction());

        int numberOfVariables = linearProgramInputTable.getNumberOfVariables();

        /* Populate with the original expressions */
        List<List<Double>> restrictions = linearProgramInputTable.getRestrictions()
                .stream()
                .map(restriction -> Lists.newArrayList(restriction.getExpression()))
                .collect(Collectors.toList());

        /* Add slack variables where needed. */
        addSlackVariables(linearProgramInputTable, restrictions);

        /* Add the result to the restrictions' expressions. */
        addRestrictionResult(linearProgramInputTable, restrictions);

        int numberOfRestrictions = restrictions.size();
        int numberOfSlackVariables = restrictions.get(0).size() - numberOfVariables + 1;

        return new LinearProgramStandardFormTable(
                numberOfVariables,
                numberOfRestrictions,
                numberOfSlackVariables,
                objectiveFunctionInStandardForm,
                restrictions
        );
    }

    private static void addRestrictionResult(LinearProgramInputTable linearProgramInputTable, List<List<Double>> restrictions) {
        for (int i = 0; i < linearProgramInputTable.getRestrictions().size(); i++) {
            Restriction currentRestriction = linearProgramInputTable.getRestrictions().get(i);

            String operator = currentRestriction.getOperator();
            Double result = currentRestriction.getResult();
            switch(operator) {
                case "lt":
                case "eq": {
                    if (result >= 0) {
                        /* If the restriction result in positive, simply add it to the expression. */
                        restrictions.get(i).add(result);
                    } else {
                        /* Otherwise add it and multiply with -1 the whole expression. */
                        restrictions.get(i).add(result);
                        restrictions.set(i, restrictions.get(i).stream()
                                .map(aDouble -> aDouble * -1)
                                .collect(Collectors.toList()));
                    }

                    break;
                }
                case "gt": {
                    if (result <= 0) {
                        /* If the restriction result in negative, simply add it to the expression. */
                        restrictions.get(i).add(-result);
                    } else {
                        /* Otherwise add it and multiply with -1 the whole expression. */
                        restrictions.get(i).add(-result);
                        restrictions.set(i, restrictions.get(i).stream()
                                .map(aDouble -> aDouble * -1)
                                .collect(Collectors.toList()));
                    }

                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + operator);
            }
        }
    }

    private static void addSlackVariables(LinearProgramInputTable linearProgramInputTable, List<List<Double>> restrictions) {
        for (int i = 0; i < linearProgramInputTable.getRestrictions().size(); i++) {
            Restriction currentRestriction = linearProgramInputTable.getRestrictions().get(i);

            String operator = currentRestriction.getOperator();
            switch(operator) {
                case "lt": {
                    /* We need to simply add a slack variable. */
                    for (int j = 0; j < linearProgramInputTable.getRestrictions().size(); j++) {
                        if (i == j) {
                            restrictions.get(i).add(1.0);
                        } else {
                            restrictions.get(j).add(0.0);
                        }
                    }

                    break;
                }
                case "gt": {
                    /* We need to multiply with -1 the whole currentRestriction and add a slack variable. */
                    restrictions.set(i, restrictions.get(i).stream()
                            .map(aDouble -> aDouble * -1)
                            .collect(Collectors.toList()));
                    for (int j = 0; j < linearProgramInputTable.getRestrictions().size(); j++) {
                        if (i == j) {
                            restrictions.get(j).add(1.0);
                        } else {
                            restrictions.get(j).add(0.0);
                        }
                    }

                    break;
                }
                case "eq": {
                    /* Leave as it is */
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + operator);
            }
        }
    }

    private static List<Double> transformObjectiveFunction(OptimizationFunction objectiveFunction) {
        if (objectiveFunction.getOptimizationType().toLowerCase().equals("max")) {
            /* If it is maximization problem we need to multiply with -1 all the values. */
            return objectiveFunction.getExpression().stream()
                    .map(aDouble -> aDouble * (-1))
                    .collect(Collectors.toList());
        } else {
            /* If it is a minimization problem, we leave the values as they are. */
            return Lists.newArrayList(objectiveFunction.getExpression());
        }
    }
}
