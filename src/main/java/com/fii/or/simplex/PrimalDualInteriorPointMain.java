package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.transformers.SimplexStandardFormTransformer;
import com.fii.or.simplex.utils.SimplexTableReader;
import com.google.common.primitives.Doubles;
import org.ejml.simple.SimpleMatrix;

import java.util.Collections;

public class PrimalDualInteriorPointMain {
    public static void main(String[] args) {
        solvePrimalDualInteriorPointProblem("/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/integer/seminar_example.txt");
    }

    private static void solvePrimalDualInteriorPointProblem(String filePath) {
        // Read problem from file
        LinearProgramStandardFormTable inputTable = SimplexStandardFormTransformer
                .transformToStandardForm(SimplexTableReader.readTable(filePath));
//        SimpleMatrix tableMatrix = new SimpleMatrix(
//                table.getRestrictions()
//                        .stream()
//                        .map(x -> x.toArray(new Double[0]))
//                .toArray(Double[][]::new));

        double[][] table = new double[inputTable.getRestrictions().size()][];
        for (int i = 0; i < inputTable.getRestrictions().size(); i++) {
            table[i] = Doubles.toArray(inputTable.getRestrictions().get(i));
        }

        int n = inputTable.getNumberOfVariables(); //dummy
        SimpleMatrix problemMatrix = new SimpleMatrix(table);
        SimpleMatrix A = problemMatrix.cols(0, inputTable.getNumberOfVariables() + inputTable.getNumberOfSlackVariables()-2);
        SimpleMatrix b = problemMatrix.cols(inputTable.getNumberOfVariables() + inputTable.getNumberOfSlackVariables()-2, inputTable.getNumberOfVariables() + inputTable.getNumberOfSlackVariables()-1);
        SimpleMatrix c = new SimpleMatrix((new double[][]{Doubles.toArray(inputTable.getObjectiveFunction())})).cols(0, n);
        int precision = 6;
        double epsilon = Math.pow(10, -precision);
        int kmax = 1000;
        int k = 0;
        int q = 6;
        double theta = n > 13 ? 1 - 3.5 / Math.sqrt(n) : 0.5;

        // Initialize x, s, y randomly
        double[] x = Doubles.toArray(Collections.nCopies(n, 0.0d));
        double[] s = Doubles.toArray(Collections.nCopies(n, 0.0d));
        double[] y = Doubles.toArray(Collections.nCopies(inputTable.getNumberOfRestrictions(), 0.0d));
        do {
            SimpleMatrix Sk = SimpleMatrix.diag(s);
            SimpleMatrix Dk = SimpleMatrix.diag(x).elementDiv(Sk);
            SimpleMatrix rokp = b.minus(A.cols(0, n).mult(new SimpleMatrix(new double[][]{x}).transpose()));
            SimpleMatrix rokd = c.minus(A.cols(0, n).transpose().mult(new SimpleMatrix(new double[][]{y}).transpose()))
                    .minus(new SimpleMatrix(new double[][]{s}).transpose());


        } while (!isDone());
    }

    private static boolean isDone() {

        return true;
    }


}
