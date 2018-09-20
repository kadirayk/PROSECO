package de.upb.crc901.proseco.view.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ListUtilTest {

	@Test
	public void isNotEmpty() {
		// null
		List<String> list = null;
		assertFalse(ListUtil.isNotEmpty(list));

		// not null but empty
		list = new ArrayList<>();
		assertFalse(ListUtil.isNotEmpty(list));

		// not empty
		list.add("asd");
		assertTrue(ListUtil.isNotEmpty(list));

	}

}
