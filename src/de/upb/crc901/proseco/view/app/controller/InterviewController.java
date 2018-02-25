package de.upb.crc901.proseco.view.app.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import de.upb.crc901.proseco.PrototypeBasedComposer;
import de.upb.crc901.proseco.view.app.model.Initiator;
import de.upb.crc901.proseco.view.core.NextStateNotFoundException;
import de.upb.crc901.proseco.view.core.Parser;
import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
import de.upb.crc901.proseco.view.util.ListUtil;
import de.upb.crc901.proseco.view.util.SerializationUtil;

@Controller
public class InterviewController {
	Interview interview;
	private static final String INIT_TEMPLATE = "initiator";
	private static final String RESULT_TEMPLATE = "result";
	private static final String FILE_UPLOAD_DIR = "data/stored/fileupload/";

	@GetMapping("/init")
	public String init(Model model) {
		model.addAttribute(INIT_TEMPLATE, new Initiator());
		interview = null;
		return INIT_TEMPLATE;
	}

	@PostMapping("/init")
	public String initSubmit(@ModelAttribute Initiator init) throws NextStateNotFoundException {
		if (StringUtils.containsIgnoreCase(init.getContent(), "machine learning", Locale.ENGLISH)
				|| StringUtils.containsIgnoreCase(init.getContent(), " ml", Locale.ENGLISH)) {
			String filePath = "data/ml_interview.yaml";
			Parser parser = new Parser();
			interview = parser.parseInterview(filePath);
			init.setInterview(interview);
		} else if (StringUtils.containsIgnoreCase(init.getContent(), "play a game", Locale.ENGLISH)) {
			String filePath = "data/game_interview.yaml";
			Parser parser = new Parser();
			interview = parser.parseInterview(filePath);
			init.setInterview(interview);

		} else if (StringUtils.containsIgnoreCase(init.getContent(), "play", Locale.ENGLISH)) {
			String filePath = "data/game_interview.yaml";
			Parser parser = new Parser();
			interview = parser.parseInterview(filePath);
			interview.getCurrentState().getQuestions().get(0).setAnswer(init.getContent());
			interview.nextState();
			init.setInterview(interview);

		}
		SerializationUtil.write(interview);
		return RESULT_TEMPLATE;
	}

	@GetMapping("/interview/{id}")
	public String next(@ModelAttribute Initiator init) throws NextStateNotFoundException {
		interview = SerializationUtil.read(init.getId());
		if (interview != null) {
			init.setInterview(interview);
		}
		return RESULT_TEMPLATE;
	}

	@PostMapping("/interview/{id}")
	public String nextPost(@ModelAttribute Initiator init,
			@RequestParam(required = false, name = "response") String response,
			@RequestParam(required = false, name = "file") MultipartFile file) throws NextStateNotFoundException {

		// final step
		if (interview.getCurrentState().getTransition() == null
				|| interview.getCurrentState().getTransition().isEmpty()) {

			Runnable task = () -> {
				PrototypeBasedComposer.run(interview);
			};
			new Thread(task).start();
			init.setInterview(interview);

			return RESULT_TEMPLATE;
		}

		if (interview != null) {
			if (file != null && !file.isEmpty()) {
				try {

					// Get the file and save it somewhere
					byte[] bytes = file.getBytes();
					Path path = Paths.get(FILE_UPLOAD_DIR + init.getId() + file.getOriginalFilename());
					Files.write(path, bytes);

					List<Question> questions = interview.getCurrentState().getQuestions();
					if (ListUtil.isNotEmpty(questions)) {
						int i = 0;
						for (Question q : questions) {
							if ("file".equals(q.getUiElement().getAttributes().get("type"))) {
								q.setAnswer(path.toString());
							}
							i++;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

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
			interview.nextState();
			init.setInterview(interview);

		}
		SerializationUtil.write(interview);

		return RESULT_TEMPLATE;
	}

	@GetMapping("/prev")
	public String prev(@ModelAttribute Initiator init) {
		if (interview != null) {
			interview.prevState();
			init.setInterview(interview);
		}
		return RESULT_TEMPLATE;
	}

}
