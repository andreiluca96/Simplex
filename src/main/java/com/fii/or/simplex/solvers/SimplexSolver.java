package com.fii.or.simplex.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimplexSolver {
    public void applySimplex(List<List<Double>> simplexTable) {
        while (!isDone(simplexTable)) {
            int pivotColumnIndex = getFirstNegativeIndex(simplexTable);
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
        double minValue = ratios.subList(0, ratios.size()-1)
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
//        System.out.println("Pivoting " + pivotRowIndex + " " + pivotColumnIndex);
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
        return simplexTable
                .stream()
                .map(row -> row.get(pivotIndex) > 0 ? row.get(row.size() - 1) / row.get(pivotIndex) : null)
                .collect(Collectors.toList());
    }

    private int getFirstNegativeIndex(List<List<Double>> simplexTable) {
        double firstNegativeValue = simplexTable.get(simplexTable.size() - 1)
                .stream()
                .mapToDouble(x -> x)
                .filter(x -> x < 0)
                .findFirst()
                .getAsDouble();
        return simplexTable.get(simplexTable.size() - 1).indexOf(firstNegativeValue);
    }

    private boolean isDone(List<List<Double>> simplexTable) {
        return simplexTable.get(simplexTable.size() - 1)
                .subList(0, simplexTable.get(simplexTable.size() - 1).size() - 1)
                .stream()
                .allMatch(x -> x >= 0);
    }
}
