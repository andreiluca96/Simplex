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
public class Restriction {
    private final List<Double> expression;
    private final String operator;
    private final Double result;
}
