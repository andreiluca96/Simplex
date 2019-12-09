package com.fii.or.simplex;

import com.fii.or.simplex.exceptions.UnfeasibleSolutionException;
import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.solvers.SimplexTwoRuleSolver;
import com.fii.or.simplex.transformers.SimplexStandardFormTransformer;
import com.fii.or.simplex.utils.SimplexTableReader;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class BranchAndBoundMain {
    public static void main(String[] args) {
        System.out.println("Seminar example");
        new BranchAndBoundMain().solveLinearProgram("D:\\Simplex\\src\\main\\java\\com\\fii\\or\\simplex\\data\\integer\\seminar_example.txt");
        System.out.println("--------------");
//
//        System.out.println("First example");
//        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex1.txt");
//        System.out.println("--------------");
//
//        System.out.println("Second example");
//        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex2.txt");
//        System.out.println("--------------");
//
//        System.out.println("Third example");
//        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex3.txt");
//        System.out.println("--------------");
//
//        System.out.println("--------------");
    }

    void solveLinearProgram(String filePath) {
        /* Read the table as as text. */
        LinearProgramInputTable linearProgramInputTable = SimplexTableReader.readTable(filePath);

        /* Convert the problem to the standard form. */
        LinearProgramStandardFormTable linearProgramStandardFormTable =
                SimplexStandardFormTransformer.transformToStandardForm(linearProgramInputTable);

        /* Solution of the integer programming problem */
        List<Double> solution = null;
        double globalOptimalValue = -999999;

        /* Stack for the branch and bound method */
        Stack<LinearProgramStandardFormTable> problemsStack = new Stack<>();
        problemsStack.push(linearProgramStandardFormTable);

        while (!problemsStack.empty()) {
            LinearProgramStandardFormTable currentProblem = problemsStack.pop();

            List<Double> currentSolution;
            try {
                currentSolution = SimplexTwoRuleSolver.solve(currentProblem);
            } catch (UnfeasibleSolutionException e) {
                System.out.println("Unfeasible problem.");
                continue;
            }

            double currentOptimalValue = -currentSolution.get(currentSolution.size() - 1);
            if (currentOptimalValue > globalOptimalValue) {
                /* Check if the solutions are integer or not. */
                boolean ok = true;
                for (int i = 0; i < currentSolution.size(); i++) {
                    double x = currentSolution.get(i);
                    if (abs(abs(x) - round(abs(x))) <= 0.0001) {
                        if (i == currentSolution.size() - 1 && ok) {
                            globalOptimalValue = currentOptimalValue;
                            solution = Lists.newArrayList(currentSolution);
                        }
                    } else {
                        ok = false;
                        List<Double> restriction1 = Lists.newArrayList(Collections.nCopies(currentSolution.size(), 0.0));
                        List<Double> restriction2 = Lists.newArrayList(Collections.nCopies(currentSolution.size(), 0.0));

                        restriction1.set(i, 1.0);
                        restriction2.set(i, -1.0);

                        restriction1.set(restriction1.size() - 1, Math.floor(currentSolution.get(i)));
                        restriction2.set(restriction1.size() - 1, -Math.ceil(currentSolution.get(i)));

                        List<List<Double>> newRestrictions1 = Lists.newArrayList(currentProblem.getRestrictions());
                        List<List<Double>> newRestrictions2 = Lists.newArrayList(currentProblem.getRestrictions());

                        newRestrictions1.add(restriction1);
                        newRestrictions2.add(restriction2);

                        LinearProgramStandardFormTable newProblem1 = currentProblem.toBuilder()
                                .restrictions(newRestrictions1)
                                .numberOfRestrictions(currentProblem.getNumberOfRestrictions() + 1)
                                .build();

                        LinearProgramStandardFormTable newProblem2 = currentProblem.toBuilder()
                                .restrictions(newRestrictions2)
                                .numberOfRestrictions(currentProblem.getNumberOfRestrictions() + 1)
                                .build();

                        problemsStack.add(newProblem1);
                        problemsStack.add(newProblem2);
                    }
                }
            }
        }

        System.out.println(solution);
    }

}
