package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.CompositionAlgorithm;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.core.interview.Question;
import de.upb.crc901.proseco.view.app.model.InterviewDTO;
import de.upb.crc901.proseco.view.core.NextStateNotFoundException;
import de.upb.crc901.proseco.view.core.Parser;
import de.upb.crc901.proseco.view.util.ListUtil;
import de.upb.crc901.proseco.view.util.SerializationUtil;

/**
 * Interview Controller for web application
 * 
 * 
 * @author kadirayk
 *
 */
@Controller
public class InterviewController {

	private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);
	private static final String INIT_TEMPLATE = "initiator";
	private static final String RESULT_TEMPLATE = "result";
	private static final String ERROR_TEMPLATE = "error";
	private ProcessController processController = new DefaultProcessController(new File("proseco.conf"));

	/**
	 * Displays Interview initiator. Interview initiator is the step where the user inputs the required keywords for corresponding prototype to be found.
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/init")
	public String init(Model model) {
		model.addAttribute("interviewDTO", new InterviewDTO());
		return INIT_TEMPLATE;
	}

	/**
	 * Initiates interview process and decides prototype according to given information
	 * 
	 * @param init
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@PostMapping("/init")
	public String initSubmit(@ModelAttribute InterviewDTO interviewDTO) throws Exception {

		/* determine domain name */
		String domainName = interviewDTO.getContent();
		
		/* create a new PROSECO service construction process and retrieve the interview */
		try {
			logger.info("Initializing new process folder for domain {}.", domainName);
			PROSECOProcessEnvironment env = processController.createConstructionProcessEnvironment(domainName);
			File interviewFile = new File(env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
			logger.info("Reading interview file {}", interviewFile);
			Parser parser = new Parser();
			interviewDTO.setInterviewFillout(new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile)));
			interviewDTO.setProcessId(env.getProcessId());
		} catch (Exception e) {
			logger.error("Error in creating a construction process for domain " + domainName + ". The exception is as follows:");
			e.printStackTrace();
		}
		saveInterviewState(interviewDTO);
		return RESULT_TEMPLATE;
	}

	/**
	 * Http Get method for /interview/{id} to display the current state of the interview with the given {id}
	 * 
	 * @param init
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@GetMapping("/interview/{id}")
	public String next(@PathVariable("id") String id, @ModelAttribute InterviewDTO interviewDTO) throws Exception {
		populateInterviewDTO(interviewDTO, id);
		return RESULT_TEMPLATE;
	}

	@GetMapping("/prev")
	public String prev(@ModelAttribute InterviewDTO init) {
		// if (memorizedInterview != null) {
		// memorizedInterview.prevState();
		// init.setInterview(memorizedInterview);
		// }
		return RESULT_TEMPLATE;
	}

	/**
	 * Http Post method for /interview/{id} to post form values and continue to the next step
	 * 
	 * @param interviewDTO
	 * @param response
	 *            is any string value that is filled in the form
	 * @param file
	 *            is any file that is uploaded via the form
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@PostMapping("/interview/{id}")
	public String nextPost(@PathVariable("id") String id, @ModelAttribute InterviewDTO interviewDTO, @RequestParam(required = false, name = "response") String response,
			@RequestParam(required = false, name = "file") MultipartFile file) throws Exception {
		
		/* retrieve the interview state */
		logger.info("Receiving response {} and file {} for process id {}. Interview: {}", response, file, id, interviewDTO);
		populateInterviewDTO(interviewDTO, id);
		logger.info("Receiving response {} and file {} for process id {}. Interview: {}", response, file, id, interviewDTO);
		InterviewFillout memorizedInterviewFillout = interviewDTO.getInterviewFillout();
		PROSECOProcessEnvironment env = processController.getConstructionProcessEnvironment(id);
		
		Map<String,String> updatedAnswers = new HashMap<>(memorizedInterviewFillout.getAnswers());

		// if it is final state run PrototypeBasedComposer with interview inputs
		if (memorizedInterviewFillout.getCurrentState().getTransition() == null || memorizedInterviewFillout.getCurrentState().getTransition().isEmpty()) {
			Runnable task = () -> {
				try {
					CompositionAlgorithm pc = new CompositionAlgorithm(env, 100);
					pc.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			new Thread(task).start();
			interviewDTO.setShowConfigurationPane(true);
			interviewDTO.setShowConsole(true);
			return RESULT_TEMPLATE;
		}

		// if a file is uploaded save the file to prototype's interview directory set the reference of file (file path) as answer to the respected question in the interview
		if (file != null && !file.isEmpty()) {
			try {
				List<Question> questions = memorizedInterviewFillout.getCurrentState().getQuestions();
				if (ListUtil.isNotEmpty(questions)) {
					for (Question q : questions) {
						if ("file".equals(q.getUiElement().getAttributes().get("type"))) {
							byte[] bytes = file.getBytes();
							if (!env.getInterviewResourcesDirectory().exists())
								FileUtils.forceMkdir(env.getInterviewResourcesDirectory());
							Path path = Paths.get(env.getInterviewResourcesDirectory() + File.separator + q.getId());
							Files.write(path, bytes);
							updatedAnswers.put(q.getId(), path.toFile().getName());
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// if any string response is given set the responses as answer to the respected interview question
		if (response != null && !StringUtils.isEmpty(response)) {
			List<String> answers = Arrays.asList(response.split(","));
			List<Question> questions = memorizedInterviewFillout.getCurrentState().getQuestions();
			if (ListUtil.isNotEmpty(questions)) {
				int i = 0;
				for (Question q : questions) {
					String answerToThisQuestion = answers.get(i);
					logger.info("Processing answer {} to question {}", answerToThisQuestion, q);
//					if (!StringUtils.isEmpty(answerToThisQuestion)) {
//						logger.warn("Question \"{}\"has already been answered.", q);
//						continue;
//					}
					if ("file".equals(q.getUiElement().getAttributes().get("type"))) {
						logger.warn("Cannot process file fields in standard process");
						continue;
					}
					if (i < answers.size()) {
						updatedAnswers.put(q.getId(), answerToThisQuestion);
						logger.info("Updating answer of question {} to: {}", q.getId(), answerToThisQuestion);
						i++;
					}
				}
			}
		}
//		logger.info("Interview state after having processed the answers is {}. Questions: {}", interviewDTO.getInterviewFillout().getCurrentState(), interviewDTO.getInterviewFillout().getCurrentState().getQuestions().stream()
//				.map(q -> "\n\t" + q.getContent() + "(" + q + "): " + q.getAnswer()).collect(Collectors.joining()));
		// update current interview state (to the first state with an unanswered question) and save it
		interviewDTO.setInterviewFillout(new InterviewFillout(memorizedInterviewFillout.getInterview(), updatedAnswers));
		saveInterviewState(interviewDTO);
//		logger.info("Interview state after having it saved is {}. Questions: {}", interviewDTO.getInterviewFillout().getCurrentState(), interviewDTO.getInterviewFillout().getCurrentState().getQuestions().stream()
//				.map(q -> "\n\t" + q.getContent() + "(" + q + "): " + q.getAnswer()).collect(Collectors.joining()));
//		interviewDTO.getInterviewFillout().getStates().forEach(
//				s -> logger.info("Saving interview state {} with questions:{}", s, s.getQuestions().stream().map(q -> "\n\t" + q.getContent() + "(" + q + "): " + q.getAnswer()).collect(Collectors.joining())));
		logger.info("Interview is now in state {}", interviewDTO.getInterviewFillout().getCurrentState());
		return RESULT_TEMPLATE;
	}



	/**
	 * Finds interview of the prototype with the given ID
	 * 
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	private void populateInterviewDTO(InterviewDTO interviewDTO, String id) throws Exception {
		PROSECOProcessEnvironment env = processController.getConstructionProcessEnvironment(id);
		InterviewFillout interview = SerializationUtil.readAsJSON(env.getInterviewStateFile());
		interviewDTO.setInterviewFillout(interview);
		interviewDTO.setProcessId(id);
	}

	/**
	 * saves interview state on current prototype instance's directory
	 * 
	 * @param interviewDTO
	 * @throws Exception 
	 */
	private void saveInterviewState(InterviewDTO interviewDTO) throws Exception {
		PROSECOProcessEnvironment env = processController.getConstructionProcessEnvironment(interviewDTO.getProcessId());
		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), interviewDTO.getInterviewFillout());
	}
}
