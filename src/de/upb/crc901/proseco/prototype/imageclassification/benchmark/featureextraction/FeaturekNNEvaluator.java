package de.upb.crc901.proseco.prototype.imageclassification.benchmark.featureextraction;

import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class FeaturekNNEvaluator implements FeatureExtractionEvaluator {

	@Override
	public double evaluate(final Instances instancesToEvaluate) {
		IBk classifier = new IBk();
		classifier.setKNN(instancesToEvaluate.size() / 2);
		try {
			Evaluation eval = new Evaluation(instancesToEvaluate);
			eval.crossValidateModel(classifier, instancesToEvaluate, 5, new Random(123), new Object[] {});
			System.out.println("Error-rate: " + eval.errorRate());
			return 1 - eval.errorRate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

}
