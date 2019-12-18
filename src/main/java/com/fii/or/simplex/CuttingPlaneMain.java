package com.fii.or.simplex;

import com.fii.or.simplex.exceptions.UnfeasibleSolutionException;
import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.model.Restriction;
import com.fii.or.simplex.solvers.SimplexTwoRuleSolver;
import com.fii.or.simplex.transformers.SimplexStandardFormTransformer;
import com.fii.or.simplex.utils.SimplexTableReader;
import com.google.common.primitives.Doubles;
import org.ejml.simple.SimpleMatrix;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class CuttingPlaneMain {
    public static void main(String[] args) {
//        System.out.println("Seminar example");
//        new CuttingPlaneMain().solveLinearProgram(
//                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/seminar_example.txt");
//        System.out.println("--------------");
//
//        System.out.println("First example");
//        new CuttingPlaneMain().solveLinearProgram(
//                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex1.txt");
//        System.out.println("--------------");
//
//        System.out.println("Second example");
//        new CuttingPlaneMain().solveLinearProgram(
//                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex2.txt");
//        System.out.println("--------------");

        System.out.println("Third example");
        new CuttingPlaneMain().solveLinearProgram(
                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex3.txt");
        System.out.println("--------------");
    }

    void solveLinearProgram(String filePath) {
        /* Read the table as as text. */
        LinearProgramInputTable linearProgramInputTable = SimplexTableReader.readTable(filePath);

        /* Solution of the integer programming problem */
        List<Double> solution = null;
        double optimalValue = -999999;
        boolean hasObtainedIntegerSolution = false;
        LinearProgramStandardFormTable linearProgramStandardFormTable;
        while (true) {
            List<Double> currentSolution;
            /* Convert the problem to the standard form. */
            linearProgramStandardFormTable = SimplexStandardFormTransformer
                            .transformToStandardForm(linearProgramInputTable);
            try {
                currentSolution = SimplexTwoRuleSolver.solve(linearProgramStandardFormTable);
                solution = currentSolution;
            } catch (UnfeasibleSolutionException e) {
                System.out.println("Unfeasible problem.");
                continue;
            }
            Double currentOptimalValue = -currentSolution.get(currentSolution.size() - 1);
            hasObtainedIntegerSolution = currentSolution.subList(0, linearProgramStandardFormTable.getNumberOfVariables())
                    .stream()
                    .allMatch(x -> abs(abs(x) - round(abs(x))) <= 0.0001);
            if (hasObtainedIntegerSolution)
                break;
            /* Compute the new constraint */
            List<List<Double>> simplexTable = SimplexTwoRuleSolver.getComputedSimplexTable();
            List<Double> chosenRow =
                    simplexTable.subList(0, simplexTable.size() - 1)
                            .stream()
                            .filter(row -> abs(abs(row.get(row.size()-1)) - round(abs(row.get(row.size()-1)))) >= 0.0001)
                            .findFirst()
                            .get()
                            .stream()
                            .map(Math::floor)
                            .collect(Collectors.toList());
            List<Double> slackVariableQuotients = chosenRow.subList(linearProgramInputTable.getNumberOfVariables(), chosenRow.size()-1);
            for (int i = 0; i < slackVariableQuotients.size(); i++) {
                int finalI = i;
                List<Double> multipliedQuotients = linearProgramStandardFormTable
                        .getRestrictions()
                        .get(i)
                        .stream()
                        .map(x -> -x * slackVariableQuotients.get(finalI))
                        .collect(Collectors.toList());
                for (int j = 0; j < chosenRow.size(); j++) {
                    chosenRow.set(j, chosenRow.get(j) + multipliedQuotients.get(j));
                }
            }
            System.out.println(chosenRow);
            /* Add the new restriction to the table */
            Restriction restriction = new Restriction(chosenRow.subList(0, chosenRow.size()-1-linearProgramInputTable.getNumberOfRestrictions()), "lt", chosenRow.get(chosenRow.size()-1));
            linearProgramInputTable.setNumberOfRestrictions(linearProgramInputTable.getNumberOfRestrictions()+1);
            linearProgramInputTable.getRestrictions().add(restriction);
        }
        System.out.println(solution);
    }
}
