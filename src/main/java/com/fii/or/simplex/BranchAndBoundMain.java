package com.fii.or.simplex;

import com.fii.or.simplex.exceptions.UnfeasibleSolutionException;
import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.solvers.SimplexTwoRuleSolver;
import com.fii.or.simplex.transformers.SimplexStandardFormTransformer;
import com.fii.or.simplex.utils.SimplexTableReader;
import com.google.common.collect.Lists;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public class BranchAndBoundMain {
    public static void main(String[] args) {
        System.out.println("Seminar example");
        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/seminar_example.txt");
        System.out.println("--------------");

        System.out.println("First example");
        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex1.txt");
        System.out.println("--------------");
//
        System.out.println("Second example");
        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex2.txt");
        System.out.println("--------------");

        System.out.println("Third example");
        new BranchAndBoundMain().solveLinearProgram("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/ex3.txt");
        System.out.println("--------------");
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
        problemsStack.add(linearProgramStandardFormTable);

        while (!problemsStack.isEmpty()) {
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
                for (int i = 0; i < currentProblem.getNumberOfVariables(); i++) {
                    double x = currentSolution.get(i);
                    if (abs(abs(x) - round(abs(x))) <= 0.001) {
                        if (i == currentProblem.getNumberOfVariables() - 1 && ok) {
                            globalOptimalValue = currentOptimalValue;
                            solution = Lists.newArrayList(currentSolution);
                        }
                    } else {
                        ok = false;
                        /* First restriction */
                        List<Double> restriction1 = Lists.newArrayList(Collections.nCopies(currentSolution.size(), 0.0));
                        restriction1.set(i, 1.0);
                        restriction1.set(restriction1.size() - 1, Math.floor(currentSolution.get(i)));

                        /* Second restriction */
                        List<Double> restriction2 = Lists.newArrayList(Collections.nCopies(currentSolution.size(), 0.0));
                        restriction2.set(i, 1.0);
                        restriction2.set(restriction1.size() - 1, Math.ceil(currentSolution.get(i)));

                        restriction1.add(restriction1.size() - 2, 1.0);
                        restriction2.add(restriction2.size() - 2, -1.0);
                        // Check if current restriction already exists
                        if (!problemContainsRestriction(currentProblem, restriction1)) {
                            List<List<Double>> newRestrictions1 = currentProblem.getRestrictions().stream()
                                    .map(doubles -> {
                                        List<Double> newDoubles = Lists.newArrayList(doubles);
                                        newDoubles.add(doubles.size() - 1, 0.0);
                                        return newDoubles;
                                    })
                                    .collect(Collectors.toList());
                            newRestrictions1.add(restriction1);
                            LinearProgramStandardFormTable newProblem1 = currentProblem.toBuilder()
                                    .restrictions(newRestrictions1)
                                    .numberOfRestrictions(currentProblem.getNumberOfRestrictions() + 1)
                                    .build();
                            if (!problemsStack.contains(newProblem1)) {
                                problemsStack.add(newProblem1);
                            }
                        }

                        if (!problemContainsRestriction(currentProblem, restriction2)) {
                            List<List<Double>> newRestrictions2 = currentProblem.getRestrictions().stream()
                                    .map(doubles -> {
                                        List<Double> newDoubles = Lists.newArrayList(doubles);
                                        newDoubles.add(doubles.size() - 1, 0.0);
                                        return newDoubles;
                                    })
                                    .collect(Collectors.toList());

                            newRestrictions2.add(restriction2);

                            LinearProgramStandardFormTable newProblem2 = currentProblem.toBuilder()
                                    .restrictions(newRestrictions2)
                                    .numberOfRestrictions(currentProblem.getNumberOfRestrictions() + 1)
                                    .build();

                            if (!problemsStack.contains(newProblem2)) {
                                problemsStack.add(newProblem2);
                            }
                        }
                        System.out.println("Restriction 1 (" + i + ") variable:" + restriction1);
                        System.out.println("Restriction 2 (" + i + ") variable:" + restriction2);
                    }
                }
            }
        }

        System.out.println(solution);
    }

    private boolean problemContainsRestriction(LinearProgramStandardFormTable currentProblem, List<Double> restriction) {
        List<Double> restrictionCopy = Lists.newArrayList(restriction);
        List<List<Double>> truncatedRestrictions = currentProblem.getRestrictions()
                .stream()
                .map(rest -> {
                    List<Double> res = Lists.newArrayList(rest);
                    List<Double> r = res.subList(0, currentProblem.getNumberOfVariables());
                    r.addAll(res.subList(currentProblem.getNumberOfVariables(), res.size()).stream().filter(x -> x != 0).collect(Collectors.toList()));
                    return r;
                })
                .collect(Collectors.toList());
        List<Double> truncatedRestriction = restrictionCopy.subList(0, currentProblem.getNumberOfVariables());
        truncatedRestriction.addAll(restrictionCopy.subList(currentProblem.getNumberOfVariables(), restrictionCopy.size()).stream().filter(x -> x != 0).collect(Collectors.toList()));
        return truncatedRestrictions.contains(truncatedRestriction);
    }

}
