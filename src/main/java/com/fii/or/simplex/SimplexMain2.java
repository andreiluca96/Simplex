package com.fii.or.simplex;

import com.fii.or.simplex.model.LinearProgramInputTable;
import com.fii.or.simplex.model.LinearProgramStandardFormTable;

import java.util.List;

public class SimplexMain2 {
    public static void main(String[] args) {
        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/twophase/seminar_example.txt");

//
//        System.out.println("First example");
//        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/twophase/ex1.txt");
//
//        System.out.println("--------------");
//
//        System.out.println("Second example");
//        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/twophase/ex2.txt");
//
//        System.out.println("--------------");
//
//        System.out.println("Third example");
//        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/twophase/ex3.txt");
//
//        System.out.println("--------------");
//
//        System.out.println("Fourth example");
//        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/twophase/ex4.txt");
//
//        System.out.println("--------------");
//
//        System.out.println("Old first example");
//        new SimplexMain2().solveLinearProgram("/Users/andrluc/Documents/Facultate/Master/OR/Simplex/src/main/java/com/fii/or/simplex/data/homework/homework1.txt");
    }

    void solveLinearProgram(String filePath) {
        /* Read the table as as text. */
        LinearProgramInputTable linearProgramInputTable = SimplexTableReader.readTable(filePath);

        /* Convert the problem to the standard form. */
        LinearProgramStandardFormTable linearProgramStandardFormTable =
                SimplexStandardFormTransformer.transformToStandardForm(linearProgramInputTable);

        /* Check if it has an initial solution based on slack variables. */
        List<List<Double>> simplexTable;
        if (SimplexTwoRuleTransformer.needsTransformation(linearProgramStandardFormTable)) {
            simplexTable = SimplexTwoRuleTransformer.applyFirstRuleTransformation(linearProgramStandardFormTable);
        } else {
//            simplexTable = linearProgramStandardFormTable.toSimplexTable();
        }

        // solve


    }
}
