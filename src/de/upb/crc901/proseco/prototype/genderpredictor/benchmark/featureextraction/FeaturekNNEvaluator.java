package de.upb.crc901.proseco.prototype.genderpredictor.benchmark.featureextraction;

import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class FeaturekNNEvaluator implements FeatureExtractionEvaluator {

	@Override
	public double evaluate(final Instances instancesToEvaluate) {
		IBk classifier = new IBk();
		classifier.setKNN(5);
		try {
			int correctCounter = 0;
			for (int i = 0; i < instancesToEvaluate.size(); i++) {
				Instances copyOfInstances = new Instances(instancesToEvaluate);
				copyOfInstances.remove(i);

				classifier.buildClassifier(copyOfInstances);
				if (instancesToEvaluate.get(i).classValue() == classifier
						.classifyInstance(instancesToEvaluate.get(i))) {
					correctCounter++;
				}
			}

			return (double) correctCounter / instancesToEvaluate.size();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

}
