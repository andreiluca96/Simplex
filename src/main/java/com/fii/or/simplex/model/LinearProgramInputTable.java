package com.fii.or.simplex.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LinearProgramInputTable {
    private int numberOfVariables;
    private int numberOfRestrictions;

    private OptimizationFunction objectiveFunction;
    private List<Restriction> restrictions;
}
