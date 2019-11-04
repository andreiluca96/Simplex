package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexTwoRuleTransformer {
    public static boolean needsTransformation(LinearProgramStandardFormTable linearProgramStandardFormTable) {
        for (int i = 0; i < linearProgramStandardFormTable.getRestrictions().size(); i++) {
            Double slackVariableValueForRestriction = linearProgramStandardFormTable.getSlackVariableValueForRestriction(i);

            if (slackVariableValueForRestriction == null || slackVariableValueForRestriction < 0) {
                return true;
            }
        }

        return false;
    }

    public static List<List<Double>> applyFirstRuleTransformation(LinearProgramStandardFormTable linearProgramStandardFormTable) {
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

        System.out.println(firstRuleSimplexTable);

        new SimplexSolver().applySimplex(firstRuleSimplexTable);

        System.out.println(firstRuleSimplexTable);

        return null;
    }
}
