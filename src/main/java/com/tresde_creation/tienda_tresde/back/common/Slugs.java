package com.tresde_creation.tienda_tresde.back.common;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {

	private Slugs() {}

	/** "Dragón Articulado!" -> "dragon-articulado" */
	public static String slugify(String text) {
		String slug = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");
		if (slug.length() > 120) slug = slug.substring(0, 120).replaceAll("-+$", "");
		return slug.isEmpty() ? "producto" : slug;
	}
}
