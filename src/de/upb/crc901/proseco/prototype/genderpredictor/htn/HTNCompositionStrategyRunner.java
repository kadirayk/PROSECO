package de.upb.crc901.proseco.prototype.genderpredictor.htn;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.upb.crc901.proseco.PrototypeProperties;
import de.upb.crc901.taskconfigurator.core.MLUtil;
import de.upb.crc901.taskconfigurator.core.SolutionEvaluator;
import de.upb.crc901.taskconfigurator.search.algorithms.BestFirstPipelineOptimizer;
import de.upb.crc901.taskconfigurator.search.evaluators.RandomCompletionEvaluator;
import jaicore.basic.PerformanceLogger;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.search.algorithms.standard.core.NodeEvaluator;
import weka.core.Instances;

/**
 * This program searches the "inputs/classifierdef" folder of its execution for a file called "instances.serialized". This file is supposed to store a serialized object of the WEKA
 * class Instances.
 *
 * It then invokes the AutoML machine to construct a good chain of WEKA tools.
 *
 * In a final step, this chain is serialized into Java code that is stored to "classifierdef" of the "outputs" folder
 *
 */
public class HTNCompositionStrategyRunner implements SolutionEvaluator {
	
	private static final PrototypeProperties PROPS = new PrototypeProperties("conf/htncompositionstrategyrunner.conf");

	private static final boolean SHOW_GRAPH = Boolean.parseBoolean(PROPS.getProperty("show_graph"));
	private static final int NUMBER_OF_CONSIDERED_SOLUTIONS = Integer.parseInt(PROPS.getProperty("number_of_considered_solutions"));
	private static final int EVALUATION_SAMPLE_SIZE = Integer.parseInt(PROPS.getProperty("evaluation_sample_size"));

	private final static String NAME_PLACEHOLDER = PROPS.getProperty("name_placeholder");
	private final static String NAME_PARAM = PROPS.getProperty("name_param");
	private static final String NAME_FVALUE = PROPS.getProperty("name_fvalue");

	private final File benchmarkFile;
	private final File outputFolder;
	private final Map<String, Integer> fValueMap = new HashMap<>();
	
	public HTNCompositionStrategyRunner(final File outputFolder, final File benchmarkFile) {
		super();
		this.outputFolder = outputFolder;
		this.benchmarkFile = benchmarkFile;
	}

	public static void main(final String[] args) throws ClassNotFoundException, FileNotFoundException, IOException {
		Thread.currentThread().setName("HTNCompositionStrategy Main");

		/* read in input and output folder specifications */
		if (args.length != 3) {
			System.err.println("Invalid usage of composition Strategy. Provide three params: \"input folder\", \"output folder\", and \"benchmark executable\"");
			return;
		}
		final File paramFile = new File(args[0] + File.separator + NAME_PLACEHOLDER + File.separator + NAME_PARAM);
		if (!paramFile.exists()) {
			if (!paramFile.getParentFile().exists()) {
				System.err.println("Invalid usage of composition Strategy. Please make sure that the first param (input folder) exists");
			} else {
				System.err.println("Invalid usage of composition Strategy. Please make sure that the input folder contains the file " + NAME_PLACEHOLDER);
			}
			return;
		}
		PerformanceLogger.logStart("StrategyTotalRun");

		/* get data */
		@SuppressWarnings("resource")
		final Instances data = (Instances) new ObjectInputStream(new BufferedInputStream(new FileInputStream(paramFile))).readObject();

		/* compute java code */
		final HTNCompositionStrategyRunner strategy = new HTNCompositionStrategyRunner(new File(args[1]), new File(args[2]));
		final String javaCode = strategy.getPlaceholderValue(data);
		strategy.writeSolution(javaCode);
		PerformanceLogger.logEnd("StrategyTotalRun");
		PerformanceLogger.saveGlobalLogToFile(new File("HTNCompositionStrategy.log"));
	}

	public String getPlaceholderValue(final Instances data) {
		/* solve composition problem */
		final Random random = new Random(0);

		final NodeEvaluator<TFDNode, Integer> nodeEval = new RandomCompletionEvaluator(random, EVALUATION_SAMPLE_SIZE, this);
		final BestFirstPipelineOptimizer optimizer = new BestFirstPipelineOptimizer(new File("htn.searchspace"), nodeEval, random, NUMBER_OF_CONSIDERED_SOLUTIONS, SHOW_GRAPH);
		final List<CEOCAction> pipelineDescription = optimizer.getPipelineDescription(data);

		/* derive Java code from the plan (this is the recipe) */
		return MLUtil.getJavaCodeFromPlan(pipelineDescription);
	}

	@Override
	public int getSolutionScore(final List<CEOCAction> plan) throws Exception {
		PerformanceLogger.logStart("getF");
		/* write down solution */
		final String javaCode = MLUtil.getJavaCodeFromPlan(plan);
		final String key = String.valueOf(System.currentTimeMillis());
		this.writeSolution(javaCode, key);

		/* call benchmark and await termination */
		System.out.println("Compute f value for current testbed");

		final File candidateFolder = new File(this.outputFolder.getAbsolutePath() + File.separator + key);
		final ProcessBuilder pb = new ProcessBuilder(this.benchmarkFile.getAbsolutePath(), candidateFolder.getAbsolutePath());
		Process fValueProcess;
		try {
			fValueProcess = pb.start();
			fValueProcess.waitFor();

			final File fValueFile = new File(candidateFolder.getAbsolutePath() + File.separator + NAME_FVALUE);
			boolean resultAvailable = false;

			while (!resultAvailable) {
				if (fValueFile.exists()) {
					resultAvailable = true;
				} else {
					Thread.sleep(50);
				}
			}

			if (fValueFile.exists() && fValueFile.isFile()) {
				try (BufferedReader br = new BufferedReader(new FileReader(fValueFile))) {
					PerformanceLogger.logEnd("getF");
					final int fValue = (int) ((1 - Double.parseDouble(br.readLine())) * FVALUE_ACCURACY);
					this.fValueMap.put(javaCode, fValue);
					return fValue;
				}
			} else {
				System.out.println("Could not compute f Value");
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("getF");
		/* read in result of the benchmark process */
		this.fValueMap.put(javaCode, 10000);
		return 10000;
	}

	private static final int FVALUE_ACCURACY = 10000;

	public void writeSolution(final String code) {
		this.writeSolution(code, "");

		try (FileWriter fw = new FileWriter(new File(this.outputFolder + File.separator + NAME_FVALUE))) {
			fw.write((1 - (this.fValueMap.get(code) / FVALUE_ACCURACY)) + "\n");
		} catch (final IOException e) {
			System.out.println("Failed to write fvalue");
			e.printStackTrace();
		}
	}

	public void writeSolution(final String code, final String subfolder) {

		/* write code to output */
		final File targetFile = new File(this.outputFolder + (!(subfolder.equals("")) ? File.separator + subfolder : "") + File.separator + NAME_PLACEHOLDER);
		if (!targetFile.getParentFile().exists()) {
			targetFile.getParentFile().mkdirs();
		}
		try (FileWriter fw = new FileWriter(targetFile)) {
			fw.write(this.rewriteJavaCode(code));
		} catch (final IOException e) {
			System.out.println("Failed to write solution " + targetFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public String rewriteJavaCode(String code) {
		final Pattern p = Pattern.compile("weka\\.classifiers\\.[^ ]*([^=]*)");
		final Matcher m = p.matcher(code);
		if (m.find()) {
			final String classifierVar = m.group(1).trim();
			code += "c = " + classifierVar + ";\n";
		} else {
			System.err.println("No classifier definition found. Cannot assign variable to c.");
		}
		return code;
	}

	@Override
	public void setTrainingData(Instances train) {
		
		/* we ignore this here, because the training and test data is already contained in the benchmark anyway */
	}

	@Override
	public void setControlData(Instances validation) {
		
		/* we ignore this here, because the training and test data is already contained in the benchmark anyway */
	}
}
