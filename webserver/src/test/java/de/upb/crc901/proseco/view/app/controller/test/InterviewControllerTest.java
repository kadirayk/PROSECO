package de.upb.crc901.proseco.view.app.controller.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.SerializationUtil;
import de.upb.crc901.proseco.view.app.controller.InterviewController;
import de.upb.crc901.proseco.view.app.model.InterviewDTO;

@Ignore
public class InterviewControllerTest {

	private static final String INTERVIEW_STATE_PATH = "processes/test-default/interview/interview_state.json";

	@Test
	public void testFileUpload() throws IOException, InvalidStateTransitionException {

		// delete file if exists
		File uploadedFile = new File("processes/test-default/interview/res/file");
		if (uploadedFile.exists()) {
			Files.delete(uploadedFile.toPath());
		}

		// update interview state to file question
		File src = new File("testdata/interview_state_file.json");
		File dst = new File(INTERVIEW_STATE_PATH);
		Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);

		InterviewController controller = new InterviewController();
		InterviewDTO interviewDTO = new InterviewDTO();
		interviewDTO.setContent("test");
		controller.initSubmit(interviewDTO);
		File testFile = new File("testdata/test.txt");
		DiskFileItem fileItem = (DiskFileItem) new DiskFileItemFactory().createItem("fileData", "text/plain", true,
				testFile.getName());
		try (InputStream input = new FileInputStream(testFile)) {
			OutputStream os = fileItem.getOutputStream();
			int ret = input.read();
			while (ret != -1) {
				os.write(ret);
				ret = input.read();
			}
			os.flush();
		}

		MultipartFile file = new CommonsMultipartFile(fileItem);
		controller.nextPost("test-default", interviewDTO, null, file);

		uploadedFile = new File("processes/test-default/interview/res/file");
		assertTrue(uploadedFile.exists());

	}

	@Test
	public void testSimpleInput() throws IOException, InvalidStateTransitionException {
		// update interview state to simple question
		File src = new File("testdata/interview_state_simple.json");
		File dst = new File(INTERVIEW_STATE_PATH);
		Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);

		InterviewController controller = new InterviewController();
		InterviewDTO interviewDTO = new InterviewDTO();
		interviewDTO.setContent("test");
		controller.initSubmit(interviewDTO);
		controller.nextPost("test-default", interviewDTO, "test", null);
		File interviewState = new File(INTERVIEW_STATE_PATH);
		InterviewFillout interview = SerializationUtil.readAsJSON(interviewState);
		interview.getAnswers();
		assertEquals("step1", interview.getCurrentState().getName());

	}

}