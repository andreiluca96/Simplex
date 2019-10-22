package com.fii.or.simplex;

import com.google.common.collect.Lists;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexMain {
    private static double variables;
    private static double restrictions;

    public static void main(String[] args) {
        List<String> allLines = Lists.newArrayList();
        try {
            allLines = Files.readAllLines(
                    Paths.get("D:\\Simplex\\src\\main\\java\\com\\fii\\or\\simplex\\test3.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        restrictions = allLines.size() - 1;
        variables = allLines.get(0).split(" ").length;

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
        System.out.println(simplexTable);

        while (!isDone(simplexTable)) {
            int pivotColumnIndex = getFirstNegativeIndex(simplexTable);
            System.out.println(pivotColumnIndex);
            List<Double> ratios = computeRatios(simplexTable, pivotColumnIndex);
            int pivotRowIndex;
            try {
                pivotRowIndex = getSmallestPositiveRatio(ratios);
            } catch (ArithmeticException e) {
                System.out.println("Problem is unbounded");
                return;
            }
            applyPivoting(simplexTable, pivotRowIndex, pivotColumnIndex);
        }
        if (hasMultipleSolutions(simplexTable)) {
            System.out.println("Problem has more than one optimal solutions");
        }
        getSolution(simplexTable);
    }

    private static void getSolution(List<List<Double>> simplexTable) {
        List<Double> solution = new ArrayList<>();
        while (solution.size() < simplexTable.get(0).size()-1) solution.add(0.0);
        for (int columnIndex = 0; columnIndex < simplexTable.get(0).size() - 1; columnIndex++) {
            boolean hasOnlyOneOne = false;
            int onePosition = 0;
            boolean isBasic = true;
            for (int rowIndex = 0; rowIndex < simplexTable.size(); rowIndex++) {
                if (simplexTable.get(rowIndex).get(columnIndex) == 1) {
                    if (!hasOnlyOneOne) {
                        hasOnlyOneOne = true;
                        onePosition = rowIndex;
                    } else {
                        isBasic = false;
                        break;
                    }
                } else if (simplexTable.get(rowIndex).get(columnIndex) != 0) {
                    isBasic = false;
                    break;
                }
            }
            if (isBasic && hasOnlyOneOne) {
                System.out.println(columnIndex);
                System.out.println(solution.size());
                solution.set(columnIndex, simplexTable.get(onePosition).get(simplexTable.get(onePosition).size()-1));
            }
        }
        System.out.println("Solution is " + solution.stream().map(Object::toString).reduce((a, b) -> a + " " + b).get() +
                " with optimal value "
                + -simplexTable.get(simplexTable.size() - 1)
                .get(simplexTable.get(simplexTable.size() - 1).size() - 1));
    }

    private static int getSmallestPositiveRatio(List<Double> ratios) throws ArithmeticException {
        return ratios.indexOf(ratios.subList(0, ratios.size()-1).stream()
                .mapToDouble(x -> x < 0 ? Double.MAX_VALUE : x)
                .min()
                .orElseThrow(ArithmeticException::new));
    }

    private static boolean hasMultipleSolutions(List<List<Double>> simplexTable) {
        int numberOfBasicVariables = simplexTable.size();
        long numberOfZeros = simplexTable.get(simplexTable.size()-1).stream().filter(x -> x == 0.0).count();
        return numberOfBasicVariables == numberOfZeros;
    }

    private static void applyPivoting(
            List<List<Double>> simplexTable,
            int pivotRowIndex,
            int pivotColumnIndex) {
        System.out.println("Pivoting " + pivotRowIndex + " " + pivotColumnIndex);
        double pivotValue = simplexTable.get(pivotRowIndex).get(pivotColumnIndex);
        simplexTable.set(pivotRowIndex, simplexTable.get(pivotRowIndex)
                .stream()
                .map(x -> x / pivotValue)
                .collect(Collectors.toList()));
        for (int rowIndex = 0; rowIndex < simplexTable.size(); rowIndex++) {
            if (rowIndex != pivotRowIndex) {
                double normalizationFactor =
                        -simplexTable.get(rowIndex).get(pivotColumnIndex);
                List<Double> updatedList = new ArrayList<>();
                for (int columnIndex = 0; columnIndex < simplexTable.get(rowIndex).size(); columnIndex++) {
                    updatedList.add(simplexTable.get(rowIndex).get(columnIndex) +
                            simplexTable.get(pivotRowIndex).get(columnIndex) * normalizationFactor);
                }
                simplexTable.set(rowIndex, updatedList);
            }
        }
    }

    private static List<Double> computeRatios(List<List<Double>> simplexTable, int pivotIndex) {
        return simplexTable
                .stream()
                .map(row -> row.get(pivotIndex) > 0 ? row.get(row.size() - 1) / row.get(pivotIndex) : Double.MAX_VALUE)
                .collect(Collectors.toList());
    }

    private static int getFirstNegativeIndex(List<List<Double>> simplexTable) {
        double firstNegativeValue = simplexTable.get(simplexTable.size() - 1)
                .stream()
                .mapToDouble(x -> x)
                .filter(x -> x < 0)
                .findFirst()
                .getAsDouble();
        return simplexTable.get(simplexTable.size() - 1).indexOf(firstNegativeValue);
    }

    private static boolean isDone(List<List<Double>> simplexTable) {
        return simplexTable.get(simplexTable.size() - 1)
                .stream()
                .allMatch(x -> x >= 0);
    }
}
