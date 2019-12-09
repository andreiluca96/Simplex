package com.fii.or.simplex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder(toBuilder=true)
public class LinearProgramStandardFormTable {
    private int numberOfVariables;
    private int numberOfRestrictions;
    private int numberOfSlackVariables;

    private List<Double> objectiveFunction;
    private List<List<Double>> restrictions;

    public List<List<Double>> toSimplexTable() {
        return null;
    }

    public Double getSlackVariableValueForRestriction(int restrictionIndex) {
        if (numberOfSlackVariables <= 0) {
            return null;
        }

        Optional<Double> slackValue = restrictions.get(restrictionIndex)
                .subList(numberOfVariables, restrictions.get(restrictionIndex).size() - 1)
                .stream()
                .filter(aDouble -> aDouble != 0)
                .findFirst();

        return slackValue.orElse(null);
    }
}
