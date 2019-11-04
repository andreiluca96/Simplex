package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.OptimizationFunction;
import com.fii.or.simplex.model.Restriction;
import com.google.common.collect.Lists;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexTableReader {
    public static LinearProgramInputTable readTable(String filePath) {
        List<String> allLines = Lists.newArrayList();

        try {
            allLines = Files.readAllLines(Paths.get(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        /* First element from the first line is the optimization type (max/min) and the last element is free. */
        int numberOfVariables = allLines.get(0).split(" ").length - 2;
        /* First line is the optimization program one. */
        int numberOfRestrictions = allLines.size() - 1;

        /* Get the actual table. */
        List<List<String>> table = allLines.stream()
                .map(line -> Lists.newArrayList(line.split(" ")))
                .collect(Collectors.toList());

        OptimizationFunction optimizationFunction = new OptimizationFunction(
                table.get(0).get(0),
                table.get(0).subList(1, table.get(0).size()).stream().map(Double::parseDouble).collect(Collectors.toList())
        );

        List<Restriction> restrictions = table.subList(1, table.size()).stream()
                .map(restriction -> {
                    String operator = restriction.get(restriction.size() - 2);
                    String restrictionResult = restriction.get(restriction.size() - 1);
                    List<Double> expression = restriction.subList(0, restriction.size() - 2).stream()
                            .map(Double::parseDouble)
                            .collect(Collectors.toList());

                    return new Restriction(expression, operator, Double.parseDouble(restrictionResult));
                })
                .collect(Collectors.toList());

        return new LinearProgramInputTable(
                numberOfVariables,
                numberOfRestrictions,
                optimizationFunction,
                restrictions
        );
    }

    public static List<List<Double>> readSimplexTable(String filePath) {
        List<String> allLines = Lists.newArrayList();

        try {
            allLines = Files.readAllLines(Paths.get(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        int variables = allLines.get(0).split(" ").length - 1;

        List<List<Double>> restrictions = Lists.newArrayList();
        List<Double> targetFunction = null;
        for (int i = 0; i < allLines.size(); i++) {
            if (i == 0) {
                targetFunction = Arrays.stream(allLines.get(i).split(" "))
                        .map(Double::parseDouble)
                        .collect(Collectors.toList());
            } else {
                restrictions.add(
                        Arrays.stream(allLines.get(i).split(" "))
                                .map(Double::parseDouble)
                                .collect(Collectors.toList())
                );
            }
        }

        List<List<Double>> simplexTable = Lists.newArrayList();
        for (int i = 0; i < restrictions.size(); i++) {
            simplexTable.add(Lists
                    .newArrayList(restrictions.get(i).subList(0, restrictions.get(i).size() - 1)));
            for (int j = 0; j < restrictions.size(); j++) {
                simplexTable.get(i).add(i == j ? 1.0 : 0.0);
            }
        }

        for (int i = 0; i < restrictions.size(); i++) {
            simplexTable.get(i).add(
                    restrictions.get(i).get(restrictions.get(i).size() - 1)
            );
        }

        List<Double> z = Lists.newArrayList(targetFunction.subList(0, targetFunction.size() - 1));
        for (int i = 0; i < restrictions.size(); i++) {
            z.add(0.0);
        }
        z.add(targetFunction.get((int) variables - 1));
        simplexTable.add(z);
        return simplexTable;
    }
}
