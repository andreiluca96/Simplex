package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class DualSimplexMain {
    public static void main(String[] args) {
        System.out.println("Seminar example");
        new DualSimplexMain().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/dual/seminar_example.txt");
        System.out.println("--------------");
        System.out.println("Seminar example");
        new DualSimplexMain().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/dual/ex1.txt");
        System.out.println("--------------");
        System.out.println("Seminar example");
        new DualSimplexMain().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/dual/ex2.txt");
        System.out.println("--------------");
    }

    void solveLinearProgram(String filePath) {
        /* Read the table as as text. */
        LinearProgramInputTable linearProgramInputTable = SimplexTableReader.readTable(filePath);

        /* Convert the problem to the standard form. */
        LinearProgramStandardFormTable linearProgramStandardFormTable =
                SimplexStandardFormTransformer.transformToStandardForm(linearProgramInputTable);

        /* Multiply with -1 where slack variables are negative */
        linearProgramStandardFormTable.setRestrictions(linearProgramStandardFormTable.getRestrictions().stream().map(
                doubles -> {
                    List<Double> slackVariablesSubList = doubles.subList(
                            linearProgramStandardFormTable.getNumberOfVariables(),
                            doubles.size()
                    );

                    if (slackVariablesSubList.stream().anyMatch(aDouble -> aDouble < 0)) {
                        return doubles.stream().map(aDouble -> aDouble * -1).collect(Collectors.toList());
                    }
                    return  doubles;
                }
        ).collect(Collectors.toList()));

        List<Double> z = Lists.newArrayList(linearProgramStandardFormTable.getObjectiveFunction());
        for (int i = 0; i < linearProgramStandardFormTable.getRestrictions().get(0).size() - linearProgramStandardFormTable.getNumberOfVariables() - 1; i++) {
            z.add(linearProgramStandardFormTable.getNumberOfVariables(), -0.0);
        }

        List<List<Double>> simplexTable = Lists.newArrayList(linearProgramStandardFormTable.getRestrictions());
        simplexTable.add(z);

        new DualSimplexSolver().applySimplex(simplexTable);
    }
}
