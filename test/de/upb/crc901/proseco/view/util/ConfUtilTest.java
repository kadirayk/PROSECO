package de.upb.crc901.proseco.view.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConfUtilTest {

	@Test
	public void booleanValueTest() {
		ConfUtil.withConf("test/conf/app.properties");
		assertTrue(ConfUtil.getValue(ConfUtil.DEBUG));
		assertFalse(ConfUtil.getValue("non existent param"));
	}

}
