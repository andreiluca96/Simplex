package com.fii.or.simplex;

import com.google.common.collect.Lists;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimplexMain {
    private static int variables;
    private static int restrictions;

    public static void main(String[] args) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get("D:\\Simplex\\src\\main\\java\\com\\fii\\or\\simplex\\test.txt"));
            restrictions = allLines.size() - 1;
            variables = allLines.get(0).split(" ").length;

            List<List<Integer>> restrictions = Lists.newArrayList();
            List<Integer> optimizationFunction = null;
            for (int i = 0; i < allLines.size(); i++) {
                if (i == 0) {

                    optimizationFunction = Arrays.stream(allLines.get(i).split(" "))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                } else {
                    restrictions.add(
                            Arrays.stream(allLines.get(i).split(" "))
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList())
                    );
                }
            }

            List<List<Integer>> simplexTable = Lists.newArrayList();
            for (int i = 0; i < restrictions.size() ; i++) {
                simplexTable.add(Lists.newArrayList(restrictions.get(i).subList(0, restrictions.get(i).size() - 1)));
                for (int j = 0; j < restrictions.size(); j++) {
                    simplexTable.get(i).add(i == j ? 1 : 0);
                }
            }

            for (int i = 0; i < restrictions.size(); i++) {
                simplexTable.get(i).add(
                        restrictions.get(i).get(restrictions.get(i).size() - 1)
                );
            }

            List<Integer> z = Lists.newArrayList(optimizationFunction.subList(0, optimizationFunction.size() - 1));
            for (int i = 0; i < restrictions.size(); i++) {
                z.add(0);
            }
            z.add(optimizationFunction.get(variables - 1));
            simplexTable.add(z);



            System.out.println(simplexTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
