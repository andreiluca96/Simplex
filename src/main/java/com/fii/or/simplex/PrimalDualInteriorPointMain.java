package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramStandardFormTable;
import com.fii.or.simplex.transformers.SimplexStandardFormTransformer;
import com.fii.or.simplex.utils.SimplexTableReader;
import com.google.common.primitives.Doubles;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.equation.Equation;
import org.ejml.simple.SimpleMatrix;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PrimalDualInteriorPointMain {
    public static void main(String[] args) {
        System.out.println("Course example\n--------------");
        new PrimalDualInteriorPointMain().solvePrimalDualInteriorPointProblem(
                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/interior_point/course.txt");
        System.out.println("\n\nFirst exercise\n--------------");
        new PrimalDualInteriorPointMain().solvePrimalDualInteriorPointProblem(
                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/interior_point/ex1.txt");
        System.out.println("\n\nSecond exercise\n--------------");
        new PrimalDualInteriorPointMain().solvePrimalDualInteriorPointProblem(
                "/Users/andrluc/Documents/facultate/or/Simplex/src/main/java/com/fii/or/simplex/data/interior_point/ex2.txt");
    }

    private void solvePrimalDualInteriorPointProblem(String filePath) {
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

        Equation equation = new Equation();
        int n = inputTable.getNumberOfVariables() + inputTable.getNumberOfSlackVariables();
        SimpleMatrix problemMatrix = new SimpleMatrix(table);
        SimpleMatrix A = problemMatrix.cols(0, n);
        SimpleMatrix b = problemMatrix.cols(n, n + 1);
        List<Double> objectiveFunctionCopy = inputTable.getObjectiveFunction();
        objectiveFunctionCopy.addAll(inputTable.getNumberOfVariables(),
                Collections.nCopies(inputTable.getNumberOfSlackVariables(), 0.0d));
        SimpleMatrix c = new SimpleMatrix(
                (new double[][] { Doubles.toArray(objectiveFunctionCopy) })).cols(0, n).transpose();
        int precision = 6;
        double epsilon = Math.pow(10, -precision);
        int kmax = 1000;
        int k = 0;
        int q = 6;
        double theta = n > 13 ? 1 - 3.5 / Math.sqrt(n) : 0.1;
        double mu = 10;
        double alphax = 1, alphas = 1, alphamax = 1, alpha = 1;

        // Initialize x, s, y randomly
        Random random = new Random();
        double[] xArray = random.doubles(n, 0, 1).toArray();
//        double[] xArray = new double[] {0.5, 0.5, 2.5, 6.5, 1.5}; // course init
        double[] sArray = random.doubles(n, 0, 1).toArray();
//        double[] sArray = new double[] {1, 11, 1, 1, 5}; // course init
        double[] yArray = random.doubles(inputTable.getNumberOfRestrictions(), 0, 1).toArray();
//        double[] yArray = new double[] {-1,-1,-5}; // course init
        double[] dArray = new double[n];
        for (int i = 0; i < xArray.length; i++) {
            dArray[i] = xArray[i] / sArray[i];
        }
        SimpleMatrix x = new SimpleMatrix(new double[][] { xArray }).transpose();
        SimpleMatrix s = new SimpleMatrix(new double[][] { sArray }).transpose();
        SimpleMatrix y = new SimpleMatrix(new double[][] { yArray }).transpose();
        SimpleMatrix S = SimpleMatrix.diag(sArray);
        SimpleMatrix D = SimpleMatrix.diag(dArray);

        // Initialize equation variables
        equation.alias
                (
                        A, "A",
                        b, "b",
                        c, "c",
                        S, "S",
                        D, "D",
                        x, "x",
                        s, "s",
                        y, "y",
                        mu, "mu",
                        alpha, "alpha"
                );
        // Main program loop
        do {
            // Compute S and D
            S = SimpleMatrix.diag(equation.lookupDDRM("s").data);
            dArray = new double[n];
            for (int i = 0; i < xArray.length; i++) {
                dArray[i] = equation.lookupDDRM("x").data[i] / equation.lookupDDRM("s").data[i];
            }
            D = SimpleMatrix.diag(dArray);
            equation.alias(S, "S");
            equation.alias(D, "D");

            // Matrix operations
            equation.process("rokp = b - A*x");
            equation.process("rokd = c - A'*y - s");
            equation.process("v = mu - x.*s");
            equation.process("deltay = -(inv(A*D*A')) * (A*inv(S)*v - A*D*rokd - rokp)");
            equation.process("deltas = -A'*deltay + rokd");
            equation.process("deltax = inv(S)*v - D*deltas");

            // Alpha computation
            alphax = IntStream.range(0, equation.lookupDDRM("x").data.length)
                    .mapToObj(i -> equation.lookupDDRM("x").data[i] + ":" +
                                   equation.lookupDDRM("deltax").data[i])
                    .filter(pair -> Double.parseDouble(pair.split(":")[1]) < 0)
                    .mapToDouble(pair -> -Double.parseDouble(pair.split(":")[0]) / Double
                            .parseDouble(pair.split(":")[1]))
                    .min()
                    .orElse(1.0d);
            alphas = IntStream.range(0, equation.lookupDDRM("s").data.length)
                    .mapToObj(i -> equation.lookupDDRM("s").data[i] + ":" +
                                   equation.lookupDDRM("deltas").data[i])
                    .filter(pair -> Double.parseDouble(pair.split(":")[1]) < 0)
                    .mapToDouble(pair -> -Double.parseDouble(pair.split(":")[0]) / Double
                            .parseDouble(pair.split(":")[1]))
                    .min()
                    .orElse(1.0d);
            alphamax = Math.min(alphax, alphas);
            alpha = 0.999999 * alphamax;

            equation.alias(alpha, "alpha");
            // Update x, y, s and iteration variables
            equation.process("x = x + alpha * deltax");
            equation.process("y = y + alpha * deltay");
            equation.process("s = s + alpha * deltas");
            x = SimpleMatrix.wrap(equation.lookupDDRM("x"));
            y = SimpleMatrix.wrap(equation.lookupDDRM("y"));
            s = SimpleMatrix.wrap(equation.lookupDDRM("s"));

            // Stop criteria
            equation.process("gap = x' * s");
            equation.process("gap_norm_matrix = [alpha * deltax;alpha*deltay;alpha*deltas]");

            k += 1;
            System.out.println("Iteration " + k + "\nGap: " + (equation.lookupDouble("gap") - n * mu) + "; mu: " + mu);

            mu = theta * mu;
            equation.alias(mu, "mu");
        } while ((Math.abs(equation.lookupDouble("gap")) > epsilon) &&
                (k < kmax) &&
                (NormOps_DDRM.normP2(equation.lookupDDRM("gap_norm_matrix")) < Math.pow(10, q)));
        System.out.println("Stopping reason: " +
                (Math.abs(equation.lookupDouble("gap")) > epsilon ? "" : "gap " + Math.abs(equation.lookupDouble("gap")) + " < " + epsilon) +
                (k < kmax ? "" : "iteration end " + kmax) +
                (NormOps_DDRM.normP2(equation.lookupDDRM("gap_norm_matrix")) < Math.pow(10, q) ? "" : " norm " + NormOps_DDRM.normP2(equation.lookupDDRM("gap_norm_matrix")) + " > " + Math.pow(10, q)));
        System.out.println("Primal solution: " +x + "\nDual solution: " + y);
        SimpleMatrix objectiveVector = new SimpleMatrix(new double[][]{Doubles.toArray(inputTable.getObjectiveFunction().subList(0, inputTable.getObjectiveFunction().size() - 1))});
        System.out.println("Objective function value: " + objectiveVector.mult(x).get(0,0));
    }
}
