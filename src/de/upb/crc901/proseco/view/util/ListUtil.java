package de.upb.crc901.proseco.view.util;

import java.util.List;

public class ListUtil {

	private ListUtil() {

	}

	public static <T> boolean isNotEmpty(List<T> list) {
		return list != null && !list.isEmpty();
	}

}
