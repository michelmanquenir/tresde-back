package com.tresde_creation.tienda_tresde.back;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegrationTests {

	// PNG de 1x1 píxel
	private static final byte[] PNG = Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

	@Autowired
	MockMvc mvc;

	private String token() throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);
		if (!m.find()) throw new AssertionError("Sin token: " + body);
		return "Bearer " + m.group(1);
	}

	private static String productJson(String name, boolean hidden, String imagesJson) {
		return """
				{"name":"%s","category":"figuras","price":25990,"description":"Figura de prueba",
				 "material":"Resina","heightCm":15,"colors":["#FF7A00","#2b2b2b"],"images":%s,
				 "isNew":true,"bestSeller":false,"hidden":%s}
				""".formatted(name, imagesJson, hidden);
	}

	@Test
	void endpointsPublicos() throws Exception {
		mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("ok")));
		mvc.perform(get("/api/categories")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(6)))
				.andExpect(jsonPath("$[0].slug", is("decoracion")));
		mvc.perform(get("/api/products")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(15)))
				.andExpect(jsonPath("$[0].id", is("maceta-geometrica")))
				.andExpect(jsonPath("$[0].isNew", is(true)))
				.andExpect(jsonPath("$[0].colors", hasSize(4)));
		mvc.perform(get("/api/products").param("category", "figuras"))
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[*].category", everyItem(is("figuras"))));
		mvc.perform(get("/api/products").param("q", "dragón"))
				.andExpect(jsonPath("$[*].id", hasItem("dragon-articulado")));
		mvc.perform(get("/api/products/dragon-articulado")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Dragón articulado")))
				.andExpect(jsonPath("$.material", is("PLA")));
		mvc.perform(get("/api/products/no-existe")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is("Producto no encontrado.")));
	}

	@Test
	void loginYPermisos() throws Exception {
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"mala\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message", is("Usuario o contraseña incorrectos.")));

		// Sin token / token inválido -> 401
		mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/admin/products").header("Authorization", "Bearer basura")).andExpect(status().isUnauthorized());

		String auth = token();
		mvc.perform(get("/api/admin/products").header("Authorization", auth)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(15)));
		mvc.perform(get("/api/auth/me").header("Authorization", auth)).andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("admin")))
				.andExpect(jsonPath("$.name", is("Administrador")));
	}

	@Test
	void crudDeProductosConFotos() throws Exception {
		String auth = token();

		// Subir foto
		String upload = mvc.perform(multipart("/api/admin/images")
				.file(new MockMultipartFile("file", "goku.png", "image/png", PNG)).header("Authorization", auth))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.url", startsWith("http://localhost/api/images/")))
				.andReturn().getResponse().getContentAsString();
		String imageUrl = upload.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1");
		mvc.perform(get(imageUrl.replace("http://localhost", ""))).andExpect(status().isOk())
				.andExpect(content().contentType("image/png"))
				.andExpect(header().string("Cache-Control", containsString("max-age")));

		// Crear (otra foto llega como data URL y se guarda en la base)
		String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(PNG);
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Figura Goku", false, "[\"" + imageUrl + "\",\"" + dataUrl + "\"]")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is("figura-goku")))
				.andExpect(jsonPath("$.colors[0]", is("#ff7a00")))
				.andExpect(jsonPath("$.images", hasSize(2)))
				.andExpect(jsonPath("$.images[1]", startsWith("http://localhost/api/images/")));

		// Mismo nombre -> id único
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Figura Goku", false, "[]")))
				.andExpect(jsonPath("$.id", is("figura-goku-2")));

		// Visible en la tienda
		mvc.perform(get("/api/products/figura-goku")).andExpect(status().isOk());

		// Editar: quitar la primera foto -> se borra de la base
		mvc.perform(put("/api/admin/products/figura-goku").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Figura Goku SSJ", false, "[]")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Figura Goku SSJ")))
				.andExpect(jsonPath("$.images", hasSize(0)));
		mvc.perform(get(imageUrl.replace("http://localhost", ""))).andExpect(status().isNotFound());

		// Ocultar -> desaparece de la tienda pero sigue en el panel
		mvc.perform(patch("/api/admin/products/figura-goku/visibility").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"hidden\":true}"))
				.andExpect(jsonPath("$.hidden", is(true)));
		mvc.perform(get("/api/products/figura-goku")).andExpect(status().isNotFound());
		mvc.perform(get("/api/products")).andExpect(jsonPath("$[*].id", not(hasItem("figura-goku"))));
		mvc.perform(get("/api/admin/products/figura-goku").header("Authorization", auth)).andExpect(status().isOk());

		// Eliminar
		mvc.perform(delete("/api/admin/products/figura-goku").header("Authorization", auth))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/products/figura-goku").header("Authorization", auth))
				.andExpect(status().isNotFound());
	}

	@Test
	void validaciones() throws Exception {
		String auth = token();
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"\",\"category\":\"figuras\",\"price\":0,\"description\":\"x\",\"material\":\"PLA\",\"colors\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.name", is("Escribe un nombre.")))
				.andExpect(jsonPath("$.errors.price", is("El precio debe ser mayor a 0.")))
				.andExpect(jsonPath("$.errors.colors", is("Agrega al menos un color.")));

		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("X", false, "[]").replace("\"figuras\"", "\"no-existe\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("no existe")));

		mvc.perform(multipart("/api/admin/images")
				.file(new MockMultipartFile("file", "a.txt", "text/plain", "hola".getBytes()))
				.header("Authorization", auth))
				.andExpect(status().isBadRequest());
	}

	@Test
	void importarReemplazaElCatalogo() throws Exception {
		String auth = token();
		String json = "[" + productJson("Uno", false, "[]").replaceFirst("\\{", "{\"id\":\"uno\",\"rating\":4.5,") + ","
				+ productJson("Dos", true, "[]") + "]";
		mvc.perform(post("/api/admin/products/import").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/admin/products").header("Authorization", auth)).andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/products")).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is("uno")))
				.andExpect(jsonPath("$[0].rating", is(4.5)));
	}

	@Test
	void corsPermiteElFrontend() throws Exception {
		mvc.perform(options("/api/admin/products")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
		mvc.perform(options("/api/products")
				.header("Origin", "https://sitio-malicioso.com")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden());
	}
}
