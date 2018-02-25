
package de.upb.crc901.proseco.view.app;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "logging.level.org.springframework.web=DEBUG")
@AutoConfigureMockMvc
public class ApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testMachineLearningContext() throws Exception {
		mockMvc.perform(get("/init")).andExpect(content().string(containsString("Input")));

		mockMvc.perform(post("/init").param("content", "I want a Machine Learning system."))
				.andExpect(content().string(containsString("Machine Learning")));

		mockMvc.perform(post("/init").param("content", "I want a ml app."))
				.andExpect(content().string(containsString("Machine Learning")));

//		mockMvc.perform(post("/interview").param("response", "name")).andExpect(content().string(containsString("What type of learning")));

//		mockMvc.perform(get("/prev")).andExpect(content().string(containsString("What is the name of")));

	}

	@Test
	public void testGenericGameContext() throws Exception {
		mockMvc.perform(get("/init")).andExpect(content().string(containsString("Input")));

		mockMvc.perform(post("/init").param("content", "I want to play a game."))
				.andExpect(content().string(containsString("Game")));

//		mockMvc.perform(post("/interview").param("response", "warcraft")).andExpect(content().string(containsString("Loading")));

	}

	@Test
	public void testSpecificGameContext() throws Exception {
		mockMvc.perform(get("/init")).andExpect(content().string(containsString("Input")));

		mockMvc.perform(post("/init").param("content", "I want to play Star Craft."))
				.andExpect(content().string(containsString("Loading game")));

	}

}
