package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import de.upb.crc901.proseco.PrototypeBasedComposer;
import de.upb.crc901.proseco.util.Config;
import de.upb.crc901.proseco.view.app.model.InterviewDTO;
import de.upb.crc901.proseco.view.core.NextStateNotFoundException;
import de.upb.crc901.proseco.view.core.Parser;
import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
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
	Interview interview;
	private static final String INIT_TEMPLATE = "initiator";
	private static final String RESULT_TEMPLATE = "result";

	private File executionDirectory;
	private File prototypeDirectory;

	/**
	 * Displays Interview initiator
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/init")
	public String init(Model model) {
		model.addAttribute("interviewDTO", new InterviewDTO());
		interview = null;
		return INIT_TEMPLATE;
	}

	/**
	 * Initiates interview process and decides prototype according to given
	 * information
	 * 
	 * @param init
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@PostMapping("/init")
	public String initSubmit(@ModelAttribute InterviewDTO init) throws NextStateNotFoundException {
		String prototypeName = null;
		if (StringUtils.containsIgnoreCase(init.getContent(), "image classification", Locale.ENGLISH)
				|| StringUtils.containsIgnoreCase(init.getContent(), "ic", Locale.ENGLISH)) {
			prototypeName = "imageclassification";
		} else if (StringUtils.containsIgnoreCase(init.getContent(), "play a game", Locale.ENGLISH)) {
			prototypeName = "game";

		}
		findInterviewOfPrototype(prototypeName, init);
		copyPrototypeSkeleton(prototypeName);
		saveInterviewState(init);
		return RESULT_TEMPLATE;
	}

	/**
	 * Http Get method for /interview/{id} to display the current state of the
	 * interview with the given {id}
	 * 
	 * @param init
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@GetMapping("/interview/{id}")
	public String next(@ModelAttribute InterviewDTO init) throws NextStateNotFoundException {
		interview = findInterview(init.getId());
		if (interview != null) {
			init.setInterview(interview);
		}
		return RESULT_TEMPLATE;
	}

	/**
	 * Finds interview of the prototype with the given ID
	 * 
	 * @param id
	 * @return
	 */
	private Interview findInterview(String id) {
		String folder = null;
		File root = Config.EXECUTIONS;
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains(id)) {
					folder = file.getAbsolutePath();
				}
			}
		}

		return SerializationUtil.readAsJSON(folder + File.separator + Config.INTERVIEW_PATH);
	}

	/**
	 * Http Post method for /interview/{id} to post form values and continue to
	 * the next step
	 * 
	 * @param init
	 * @param response
	 *            is any string value that is filled in the form
	 * @param file
	 *            is any file that is uploaded via the form
	 * @return
	 * @throws NextStateNotFoundException
	 */
	@PostMapping("/interview/{id}")
	public String nextPost(@ModelAttribute InterviewDTO init,
			@RequestParam(required = false, name = "response") String response,
			@RequestParam(required = false, name = "file") MultipartFile file) throws NextStateNotFoundException {

		// if it is final state run PrototypeBasedComposer with interview inputs
		if (interview.getCurrentState().getTransition() == null
				|| interview.getCurrentState().getTransition().isEmpty()) {

			Runnable task = () -> {
				PrototypeBasedComposer.run(interview.getPrototypeName() + "-" + init.getId());
			};
			new Thread(task).start();
			init.setInterview(interview);
			init.setShowConsole(true);
			return RESULT_TEMPLATE;
		}

		if (interview != null) {
			// if a file is uploaded save the file to prototype's interview
			// directory
			// set the reference of file (file path) as answer to the respected
			// question in the interview
			if (file != null && !file.isEmpty()) {
				try {
					byte[] bytes = file.getBytes();
					Path path = Paths.get(Config.EXECUTIONS_PATH + interview.getPrototypeName() + "-" + init.getId()
							+ File.separator + Config.INTERVIEW_PATH + Config.INTERVIEW_RESOUCES_PATH
							+ file.getOriginalFilename());
					Files.write(path, bytes);

					List<Question> questions = interview.getCurrentState().getQuestions();
					if (ListUtil.isNotEmpty(questions)) {
						for (Question q : questions) {
							if ("file".equals(q.getUiElement().getAttributes().get("type"))) {
								q.setAnswer(path.toString());
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// if any string response is given set the responses as answer to
			// the respected interview question
			if (response != null && !StringUtils.isEmpty(response)) {
				List<String> answers = Arrays.asList(response.split(","));
				List<Question> questions = interview.getCurrentState().getQuestions();
				if (ListUtil.isNotEmpty(questions)) {
					int i = 0;
					for (Question q : questions) {
						if (!"file".equals(q.getUiElement().getAttributes().get("type"))) {
							if (i < answers.size()) {
								q.setAnswer(answers.get(i));
							}
						}
						i++;
					}
				}
			}

			// continue to next interview state
			interview.nextState();
			init.setInterview(interview);

		}
		// save current interview state
		saveInterviewState(init);

		return RESULT_TEMPLATE;
	}

	@GetMapping("/prev")
	public String prev(@ModelAttribute InterviewDTO init) {
		if (interview != null) {
			interview.prevState();
			init.setInterview(interview);
		}
		return RESULT_TEMPLATE;
	}

	/**
	 * Copies prototype skeleton for the current prototype instance
	 */
	private void copyPrototypeSkeleton(String prototypeName) {
		prototypeDirectory = new File(Config.PROTOTYPES_PATH + File.separator + prototypeName);
		this.executionDirectory = new File(
				Config.EXECUTIONS.getAbsolutePath() + File.separator + prototypeName + "-" + interview.getId());
		try {
			FileUtils.copyDirectory(this.prototypeDirectory, this.executionDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * finds current prototype's interview to be showed in the web interface,
	 * from the prototype's initial directory
	 * 
	 * @param init
	 */
	private void findInterviewOfPrototype(String prototypeName, InterviewDTO init) {
		String filePath = Config.PROTOTYPES_PATH + prototypeName + File.separator + Config.INTERVIEW_PATH
				+ "interview.yaml";
		Parser parser = new Parser();
		interview = parser.parseInterview(filePath);
		init.setInterview(interview);
	}

	/**
	 * saves interview state on current prototype instance's directory
	 * 
	 * @param init
	 */
	private void saveInterviewState(InterviewDTO init) {
		SerializationUtil.writeAsJSON(Config.EXECUTIONS_PATH + interview.getPrototypeName() + "-" + init.getId()
				+ File.separator + Config.INTERVIEW_PATH, interview);
	}

}
