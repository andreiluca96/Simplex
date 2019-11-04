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
public class OptimizationFunction {
    private final String optimizationType;
    private final List<Double> expression;
}
