package de.upb.crc901.proseco.prototype.genderpredictor.benchmark.featureextraction;

import weka.core.Instances;

public interface FeatureExtractionEvaluator {

	public double evaluate(Instances instancesToEvaluate);

}
