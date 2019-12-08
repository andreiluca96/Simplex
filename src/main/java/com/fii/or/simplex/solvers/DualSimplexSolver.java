package com.fii.or.simplex.solvers;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DualSimplexSolver {
    public void applySimplex(List<List<Double>> simplexTable) {
        while (!isDone(simplexTable)) {
            int pivotLineIndex = getFirstNegativeIndex(simplexTable);
            List<Double> ratios = computeRatios(simplexTable, pivotLineIndex);
            int pivotColumnIndex;
            try {
                pivotColumnIndex = getSmallestPositiveRatio(ratios);
            } catch (ArithmeticException e) {
                System.out.println("Problem is unfeasible!!");
                return;
            }
            applyPivoting(simplexTable, pivotLineIndex, pivotColumnIndex);
        }
        if (hasMultipleSolutions(simplexTable)) {
            System.out.println("Problem has more than one optimal solutions");
        }
        getSolution(simplexTable);
    }

    private void getSolution(List<List<Double>> simplexTable) {
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
                solution.set(columnIndex, simplexTable.get(onePosition).get(simplexTable.get(onePosition).size()-1));
            }
        }
        System.out.println("Solution is " + solution.stream().map(Object::toString).reduce((a, b) -> a + " " + b).get() +
                " with optimal value "
                + -simplexTable.get(simplexTable.size() - 1)
                .get(simplexTable.get(simplexTable.size() - 1).size() - 1));
    }

    private int getSmallestPositiveRatio(List<Double> ratios) throws ArithmeticException {
        double minValue = ratios.subList(0, ratios.size())
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(x -> x)
                .min()
                .orElseThrow(ArithmeticException::new);
        return ratios.indexOf(minValue);
    }

    private boolean hasMultipleSolutions(List<List<Double>> simplexTable) {
        int numberOfBasicVariables = simplexTable.size();
        long numberOfZeros = simplexTable.get(simplexTable.size()-1).stream().filter(x -> x == 0.0).count();
        return numberOfBasicVariables == numberOfZeros;
    }

    private void applyPivoting(
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

    private List<Double> computeRatios(List<List<Double>> simplexTable, int pivotIndex) {
        List<Double> ratios = Lists.newArrayList();
        for (int i = 0; i < simplexTable.get(0).size() - 1; i++) {
            if (simplexTable.get(pivotIndex).get(i) >= 0) {
                ratios.add(null);
            } else {
                ratios.add(Math.abs(simplexTable.get(simplexTable.size() - 1).get(i) / simplexTable.get(pivotIndex).get(i)));
            }
        }

        return ratios;
    }

    private int getFirstNegativeIndex(List<List<Double>> simplexTable) {
        Optional<Double> first = simplexTable.subList(0, simplexTable.size() - 1).stream()
                .map(doubles -> doubles.get(doubles.size() - 1))
                .filter(aDouble -> aDouble < 0)
                .findFirst();

        return simplexTable.subList(0, simplexTable.size() - 1).stream()
                .map(doubles -> doubles.get(doubles.size() - 1))
                .collect(Collectors.toList())
                .indexOf(first.get());
    }

    private boolean isDone(List<List<Double>> simplexTable) {
        return simplexTable.subList(0, simplexTable.size() - 1).stream()
                .map(doubles -> doubles.get(doubles.size() - 1))
                .allMatch(x -> x >= 0);
    }
}
