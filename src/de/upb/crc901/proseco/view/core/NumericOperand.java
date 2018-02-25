package de.upb.crc901.proseco.view.core;

public class NumericOperand extends Operand {

	private double numericValue;

	public NumericOperand(String value) {
		super(value);
		numericValue = Double.valueOf(value);
	}

	public double getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(double numericValue) {
		this.numericValue = numericValue;
	}

}
