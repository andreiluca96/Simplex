package com.fii.or.simplex.solvers;

import com.fii.or.simplex.exceptions.UnfeasibleSolutionException;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexTwoRuleSolver {
    @Getter
    private static List<List<Double>> computedSimplexTable;
    public static boolean needsTransformation(LinearProgramStandardFormTable linearProgramStandardFormTable) {
        for (int i = 0; i < linearProgramStandardFormTable.getRestrictions().size(); i++) {
            Double slackVariableValueForRestriction = linearProgramStandardFormTable.getSlackVariableValueForRestriction(i);

            if (slackVariableValueForRestriction == null || slackVariableValueForRestriction < 0) {
                return true;
            }
        }

        return false;
    }

    public static List<Double> solve(LinearProgramStandardFormTable linearProgramStandardFormTable) throws UnfeasibleSolutionException {
        /* Copy the current restrictions. */
        List<List<Double>> firstRuleSimplexTable = Lists.newArrayList(
                linearProgramStandardFormTable.getRestrictions()
                        .stream()
                        .map(Lists::newArrayList)
                        .collect(Collectors.toList()
                        )
        );

        List<Integer> linesWithFirstRuleSlackVariables = Lists.newArrayList();
        /* Add the 'y's for the First rule.  */
        for (int i = 0; i < linearProgramStandardFormTable.getRestrictions().size(); i++) {
            Double slackVariableValueForRestriction = linearProgramStandardFormTable.getSlackVariableValueForRestriction(i);

            if (slackVariableValueForRestriction == null || slackVariableValueForRestriction < 0) {
                linesWithFirstRuleSlackVariables.add(i);

                for (int j = 0; j < linearProgramStandardFormTable.getRestrictions().size(); j++) {
                    if (i == j) {
                        firstRuleSimplexTable.get(j).add(firstRuleSimplexTable.get(j).size() - 1, 1.0);
                    } else {
                        firstRuleSimplexTable.get(j).add(firstRuleSimplexTable.get(j).size() - 1, 0.0);
                    }
                }
            }
        }

        List<Double> lastLine = new ArrayList<>(Collections.nCopies(firstRuleSimplexTable.get(0).size(), 0.0));
        linesWithFirstRuleSlackVariables
                .forEach(index -> {
                    List<Double> copyOfRestriction = Lists.newArrayList(firstRuleSimplexTable.get(index));
                    copyOfRestriction.set(copyOfRestriction.subList(0, copyOfRestriction.size() - 1).lastIndexOf(1.0), 0.0);

                    for (int i = 0; i < firstRuleSimplexTable.get(0).size(); i++) {
                        lastLine.set(i, lastLine.get(i) - copyOfRestriction.get(i));
                    }
                });
        firstRuleSimplexTable.add(lastLine);

        System.out.println("Before Phase 1:");
        System.out.println(firstRuleSimplexTable);

        new SimplexSolver().applySimplex(firstRuleSimplexTable);

        System.out.println("After Phase 1:");
        System.out.println(firstRuleSimplexTable);

        if (firstRuleSimplexTable.get(firstRuleSimplexTable.size() - 1).get(firstRuleSimplexTable.get(firstRuleSimplexTable.size() - 1).size() - 1) != 0){
            throw new UnfeasibleSolutionException();
        }

        for (int i = 0; i < firstRuleSimplexTable.size(); i++) {
            for (int j = 0; j < linesWithFirstRuleSlackVariables.size(); j++) {
                firstRuleSimplexTable.get(i).remove(firstRuleSimplexTable.get(i).size() - 2);
            }
        }

        List<Double> lastLineSecondPhase = new ArrayList<>(Collections.nCopies(firstRuleSimplexTable.get(0).size(), 0.0));
        for (int columnIndex = 0; columnIndex < firstRuleSimplexTable.get(0).size() - 1; columnIndex++) {
            boolean hasOnlyOneOne = false;
            int onePosition = 0;
            boolean isBasic = true;
            for (int rowIndex = 0; rowIndex < firstRuleSimplexTable.size(); rowIndex++) {
                if (firstRuleSimplexTable.get(rowIndex).get(columnIndex) == 1) {
                    if (!hasOnlyOneOne) {
                        hasOnlyOneOne = true;
                        onePosition = rowIndex;
                    } else {
                        isBasic = false;
                        break;
                    }
                } else if (firstRuleSimplexTable.get(rowIndex).get(columnIndex) != 0) {
                    isBasic = false;
                    break;
                }
            }
            if (isBasic && hasOnlyOneOne) {
                if (columnIndex < linearProgramStandardFormTable.getNumberOfVariables()) {
                    List<Double> copyOfCurrentLine = Lists.newArrayList(firstRuleSimplexTable.get(onePosition));

                    copyOfCurrentLine.set(columnIndex, 0.0);

                    for (int i = 0; i < copyOfCurrentLine.size() - 1; i++) {
                        copyOfCurrentLine.set(
                                i,
                                - linearProgramStandardFormTable.getObjectiveFunction().get(columnIndex) * copyOfCurrentLine.get(i)
                        );
                    }
                    copyOfCurrentLine.set(
                            copyOfCurrentLine.size() - 1,
                            linearProgramStandardFormTable.getObjectiveFunction().get(columnIndex) * copyOfCurrentLine.get(copyOfCurrentLine.size() - 1)
                    );

                    for (int i = 0; i < copyOfCurrentLine.size(); i++) {
                        lastLineSecondPhase.set(i, lastLineSecondPhase.get(i) + copyOfCurrentLine.get(i));
                    }
                }
            } else {
                if (columnIndex < linearProgramStandardFormTable.getNumberOfVariables()) {
                    lastLineSecondPhase.set(columnIndex, lastLineSecondPhase.get(columnIndex) + linearProgramStandardFormTable.getObjectiveFunction().get(columnIndex));
                }
            }
        }

        lastLineSecondPhase.set(
                lastLineSecondPhase.size() - 1,
                -lastLineSecondPhase.get(lastLineSecondPhase.size() - 1)
        );

        firstRuleSimplexTable.set(firstRuleSimplexTable.size() - 1, lastLineSecondPhase);

        System.out.println("Before phase 2:");
        System.out.println(firstRuleSimplexTable);

        List<Double> solution = new SimplexSolver().applySimplex(firstRuleSimplexTable);

        System.out.println("After phase 2:");
        System.out.println(firstRuleSimplexTable);

        computedSimplexTable = firstRuleSimplexTable;

        return solution;
    }
}
