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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
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
				 "isNew":true,"bestSeller":false,"stock":5,"hidden":%s}
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
				.andExpect(jsonPath("$.name", is("Administrador")))
				.andExpect(jsonPath("$.role", is("ADMIN")));
	}

	private String loginAs(String username, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);
		if (!m.find()) throw new AssertionError("Sin token: " + body);
		return "Bearer " + m.group(1);
	}

	@Test
	void usuariosDelPanel() throws Exception {
		// Ya no hay registro público
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ana\",\"email\":\"ana@correo.com\",\"password\":\"secreta123\"}"))
				.andExpect(status().isUnauthorized());

		String admin = token();
		String trabajador = "{\"name\":\"Pedro\",\"username\":\" Pedro \",\"password\":\"secreta123\",\"role\":\"STAFF\"}";
		String body = mvc.perform(post("/api/admin/users").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON).content(trabajador))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username", is("pedro")))
				.andExpect(jsonPath("$.role", is("STAFF")))
				.andReturn().getResponse().getContentAsString();
		String id = body.replaceAll(".*\"id\":([0-9]+).*", "$1");

		// Usuario repetido y contraseña corta -> 400 con error en el campo
		mvc.perform(post("/api/admin/users").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON).content(trabajador))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.username").exists());
		mvc.perform(post("/api/admin/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Luis\",\"username\":\"luis\",\"password\":\"corta\",\"role\":\"STAFF\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.password").exists());

		mvc.perform(get("/api/admin/users").header("Authorization", admin)).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].username", hasItem("pedro")));

		// El trabajador usa el panel, pero no ve usuarios ni cambia la configuración
		String staff = loginAs("pedro", "secreta123");
		mvc.perform(get("/api/admin/products").header("Authorization", staff)).andExpect(status().isOk());
		mvc.perform(get("/api/admin/settings/costs").header("Authorization", staff)).andExpect(status().isOk());
		mvc.perform(put("/api/admin/settings/costs").header("Authorization", staff)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/users").header("Authorization", staff)).andExpect(status().isForbidden());

		// Cambiar la contraseña cierra sus sesiones abiertas
		Thread.sleep(1100);
		mvc.perform(put("/api/admin/users/" + id).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Pedro Soto\",\"password\":\"otraClave99\",\"role\":\"STAFF\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Pedro Soto")));
		mvc.perform(get("/api/admin/products").header("Authorization", staff)).andExpect(status().isUnauthorized());
		staff = loginAs("pedro", "otraClave99");
		mvc.perform(get("/api/auth/me").header("Authorization", staff)).andExpect(jsonPath("$.role", is("STAFF")));

		// El administrador no puede quitarse su rol ni eliminarse
		String adminId = mvc.perform(get("/api/admin/users").header("Authorization", admin))
				.andReturn().getResponse().getContentAsString().replaceAll("^\\[\\{\"id\":([0-9]+).*", "$1");
		mvc.perform(put("/api/admin/users/" + adminId).header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Administrador\",\"role\":\"STAFF\"}"))
				.andExpect(status().isConflict());
		mvc.perform(delete("/api/admin/users/" + adminId).header("Authorization", admin)).andExpect(status().isConflict());

		// Al eliminarlo, su token deja de servir de inmediato
		mvc.perform(delete("/api/admin/users/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/products").header("Authorization", staff)).andExpect(status().isUnauthorized());
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

	@Test
	void healthEsPublicoConGetYHead() throws Exception {
		mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("ok")));
		// UptimeRobot y otros monitores usan HEAD
		mvc.perform(head("/api/health")).andExpect(status().isOk());
	}

	@Test
	void configuracionDeCostos() throws Exception {
		mvc.perform(get("/api/admin/settings/costs")).andExpect(status().isUnauthorized());

		String auth = token();
		mvc.perform(get("/api/admin/settings/costs").header("Authorization", auth)).andExpect(status().isOk())
				.andExpect(jsonPath("$.machineHourCost", is(35.0)))
				.andExpect(jsonPath("$.machineReservePercent", is(10.0)))
				.andExpect(jsonPath("$.priceMultiplier", is(4.0)))
				.andExpect(jsonPath("$.filamentPricePerKg.PLA", is(15000)));

		mvc.perform(put("/api/admin/settings/costs").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"machineHourCost\":40,\"machineReservePercent\":12,\"priceMultiplier\":3.5,"
						+ "\"filamentPricePerKg\":{\"PLA\":16000,\"PETG\":18000}}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.machineHourCost", is(40.0)))
				.andExpect(jsonPath("$.filamentPricePerKg.PETG", is(18000)))
				.andExpect(jsonPath("$.filamentPricePerKg.TPU", is(15000))); // no enviado: se conserva

		mvc.perform(put("/api/admin/settings/costs").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"machineHourCost\":-1,\"machineReservePercent\":95,\"priceMultiplier\":0,\"filamentPricePerKg\":{}}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.machineHourCost").exists())
				.andExpect(jsonPath("$.errors.machineReservePercent").exists())
				.andExpect(jsonPath("$.errors.priceMultiplier").exists());
	}

	@Test
	void costosDelProductoSoloEnElPanel() throws Exception {
		String auth = token();
		long argolla = createAccessory(auth, "Argolla test", 30);
		String json = productJson("Llavero Naruto", false, "[]").replace("\"hidden\":false}",
				"\"hidden\":false,\"cost\":{\"grams\":8,\"batchHours\":11,\"batchPieces\":10,\"accessories\":[{\"accessoryId\":" + argolla + ",\"quantity\":2}]}}");
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.cost.grams", is(8.0)))
				.andExpect(jsonPath("$.cost.batchPieces", is(10)));

		mvc.perform(get("/api/admin/products/llavero-naruto").header("Authorization", auth))
				.andExpect(jsonPath("$.cost.accessoriesCost", is(60)))
				.andExpect(jsonPath("$.cost.accessories[0].quantity", is(2)));
		// La tienda no ve los costos
		mvc.perform(get("/api/products/llavero-naruto")).andExpect(status().isOk())
				.andExpect(jsonPath("$.cost").doesNotExist());

		// Validación de costos
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json.replace("\"batchPieces\":10", "\"batchPieces\":0")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors['cost.batchPieces']").exists());
	}

	@Test
	void stockDisponible() throws Exception {
		String auth = token();
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Figura con stock", false, "[]")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.stock", is(5)));

		// La tienda ve el stock
		mvc.perform(get("/api/products/figura-con-stock")).andExpect(jsonPath("$.stock", is(5)));

		// Cambio rápido desde la lista del panel
		mvc.perform(patch("/api/admin/products/figura-con-stock/stock").contentType(MediaType.APPLICATION_JSON)
				.content("{\"stock\":2}")).andExpect(status().isUnauthorized());
		mvc.perform(patch("/api/admin/products/figura-con-stock/stock").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"stock\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock", is(2)));
		mvc.perform(patch("/api/admin/products/figura-con-stock/stock").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"stock\":-1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.stock", is("El stock no puede ser negativo.")));

		// Un producto nuevo sin stock parte en 0 (las unidades llegan con una entrega)
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Sin stock", false, "[]").replace("\"stock\":5,", "")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.stock", is(0)));

		// Editar sin mandar stock conserva el actual (no pisa ventas ni entregas aceptadas)
		mvc.perform(put("/api/admin/products/figura-con-stock").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Figura con stock", false, "[]").replace("\"stock\":5,", "")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock", is(2)));
	}

	@Test
	void productoNuevoSeEntregaYSumaAlAceptar() throws Exception {
		String admin = token();
		mvc.perform(post("/api/admin/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Pedro\",\"username\":\"pedro\",\"password\":\"secreta123\",\"role\":\"STAFF\"}"))
				.andExpect(status().isCreated());
		String pedro = loginAs("pedro", "secreta123");

		// Recién impreso: se crea sin stock
		mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Llavero nuevo", false, "[]").replace("\"stock\":5,", "")))
				.andExpect(jsonPath("$.stock", is(0)));
		String body = mvc.perform(post("/api/admin/deliveries").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientUsername\":\"pedro\",\"items\":[{\"productId\":\"llavero-nuevo\",\"quantity\":10}]}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String id = body.replaceAll("^\\{\"id\":([0-9]+).*", "$1");

		// Mientras tanto se edita el producto (cambia el precio): no toca el stock
		mvc.perform(put("/api/admin/products/llavero-nuevo").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Llavero nuevo", false, "[]").replace("\"stock\":5,", "").replace("25990", "3990")))
				.andExpect(jsonPath("$.stock", is(0)));

		mvc.perform(post("/api/admin/deliveries/" + id + "/accept").header("Authorization", pedro))
				.andExpect(jsonPath("$.status", is("ACCEPTED")));
		mvc.perform(get("/api/admin/products/llavero-nuevo").header("Authorization", admin))
				.andExpect(jsonPath("$.stock", is(10)))
				.andExpect(jsonPath("$.price", is(3990)));
	}

	@Test
	void sesionDe30DiasYRenovacion() throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"admin\"}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Matcher m = Pattern.compile("\"expiresAt\":\"([^\"]+)\"").matcher(body);
		if (!m.find()) throw new AssertionError("Sin expiresAt: " + body);
		java.time.Duration left = java.time.Duration.between(java.time.Instant.now(), java.time.Instant.parse(m.group(1)));
		if (left.toDays() < 29) throw new AssertionError("La sesión dura menos de 30 días: " + left);

		mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/auth/refresh").header("Authorization", token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.user.role", is("ADMIN")));
	}

	@Test
	void ventasDescuentanYDevuelvenStock() throws Exception {
		String auth = token();
		String json = productJson("Figura vendible", false, "[]").replace("\"hidden\":false}",
				"\"hidden\":false,\"cost\":{\"grams\":10,\"batchHours\":2,\"batchPieces\":1}}");
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isCreated());

		mvc.perform(get("/api/admin/sales")).andExpect(status().isUnauthorized());

		// Vende 2 de 5: precio actual por defecto y costo congelado (materia prima: 10 g × $15 = $150; la luz no cuenta en ventas)
		String sale = mvc.perform(post("/api/admin/sales").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"figura-vendible\",\"quantity\":2,\"color\":\"#FF7A00\",\"customer\":\"Ana\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.unitPrice", is(25990)))
				.andExpect(jsonPath("$.unitCost", is(150.0)))
				.andExpect(jsonPath("$.unitFilamentCost", is(150.0)))
				.andExpect(jsonPath("$.unitAccessoriesCost", is(0.0)))
				.andExpect(jsonPath("$.reservePercent", is(10.0)))
				.andExpect(jsonPath("$.productName", is("Figura vendible")))
				.andReturn().getResponse().getContentAsString();
		mvc.perform(get("/api/admin/products/figura-vendible").header("Authorization", auth))
				.andExpect(jsonPath("$.stock", is(3)));

		// No deja vender más que el stock
		mvc.perform(post("/api/admin/sales").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"figura-vendible\",\"quantity\":4}"))
				.andExpect(status().isConflict());
		mvc.perform(post("/api/admin/sales").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"figura-vendible\",\"quantity\":3,\"unitPrice\":20000}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.unitPrice", is(20000)));
		mvc.perform(get("/api/admin/products/figura-vendible").header("Authorization", auth))
				.andExpect(jsonPath("$.stock", is(0)));

		mvc.perform(get("/api/admin/sales").header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
		mvc.perform(get("/api/admin/sales?from=2000-01-01T00:00:00Z&to=2001-01-01T00:00:00Z").header("Authorization", auth))
				.andExpect(jsonPath("$", hasSize(0)));

		// Anular devuelve el stock
		Matcher m = Pattern.compile("\"id\":([0-9]+)").matcher(sale);
		if (!m.find()) throw new AssertionError(sale);
		mvc.perform(delete("/api/admin/sales/" + m.group(1)).header("Authorization", auth))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/products/figura-vendible").header("Authorization", auth))
				.andExpect(jsonPath("$.stock", is(2)));

		// Validación
		mvc.perform(post("/api/admin/sales").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"figura-vendible\",\"quantity\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.quantity").exists());
	}

	private long createAccessory(String auth, String name, int cost) throws Exception {
		String body = mvc.perform(post("/api/admin/accessories").header("Authorization", auth)
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\",\"cost\":" + cost + "}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Matcher m = Pattern.compile("\"id\":([0-9]+)").matcher(body);
		if (!m.find()) throw new AssertionError(body);
		return Long.parseLong(m.group(1));
	}

	@Test
	void catalogoDeAccesorios() throws Exception {
		String auth = token();
		mvc.perform(get("/api/admin/accessories")).andExpect(status().isUnauthorized());
		long clicker = createAccessory(auth, "Mecanismo clicker", 120);
		long iman = createAccessory(auth, "Imán", 15);

		// Nombre repetido
		mvc.perform(post("/api/admin/accessories").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"mecanismo CLICKER\",\"cost\":10}"))
				.andExpect(status().isConflict());

		// Producto con 1 clicker + 2 imanes = 150
		String json = productJson("Clicker con imanes", false, "[]").replace("\"hidden\":false}",
				"\"hidden\":false,\"cost\":{\"grams\":5,\"accessories\":[{\"accessoryId\":" + clicker
						+ ",\"quantity\":1},{\"accessoryId\":" + iman + ",\"quantity\":2}]}}");
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.cost.accessoriesCost", is(150)));

		// Cambiar el precio del accesorio actualiza el costo del producto
		mvc.perform(put("/api/admin/accessories/" + iman).header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Imán neodimio\",\"cost\":25}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usedBy", is(1)));
		mvc.perform(get("/api/admin/products/clicker-con-imanes").header("Authorization", auth))
				.andExpect(jsonPath("$.cost.accessoriesCost", is(170)));

		// Accesorio inexistente
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json.replace("\"accessoryId\":" + clicker, "\"accessoryId\":99999")))
				.andExpect(status().isBadRequest());

		// Eliminarlo lo quita del producto
		mvc.perform(delete("/api/admin/accessories/" + clicker).header("Authorization", auth))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/products/clicker-con-imanes").header("Authorization", auth))
				.andExpect(jsonPath("$.cost.accessoriesCost", is(50)))
				.andExpect(jsonPath("$.cost.accessories", hasSize(1)));
	}

	@Test
	void categoriasMultiplesYMantenedor() throws Exception {
		String auth = token();

		// Crear categoría: el slug sale del nombre y queda al final
		mvc.perform(post("/api/admin/categories").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Navidad\",\"description\":\"Adornos\",\"icon\":\"star\",\"hue\":120}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug", is("navidad")))
				.andExpect(jsonPath("$.products", is(0)));
		mvc.perform(post("/api/admin/categories").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"navidad\",\"icon\":\"star\",\"hue\":120}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errors.name").exists());
		mvc.perform(get("/api/categories")).andExpect(jsonPath("$", hasSize(7)))
				.andExpect(jsonPath("$[6].slug", is("navidad")));

		// Producto en dos categorías: la primera es la principal
		String json = productJson("Grinch", false, "[]").replace("\"category\":\"figuras\"", "\"categories\":[\"navidad\",\"decoracion\",\"navidad\"]");
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.category", is("navidad")))
				.andExpect(jsonPath("$.categories", hasSize(2)));
		mvc.perform(get("/api/products").param("category", "decoracion"))
				.andExpect(jsonPath("$[*].id", hasItem("grinch")));
		mvc.perform(get("/api/products").param("category", "navidad"))
				.andExpect(jsonPath("$[*].id", hasItem("grinch")));

		// Sin categorías -> 400
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Sin cat", false, "[]").replace("\"category\":\"figuras\"", "\"categories\":[]")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.categories").exists());

		// Reordenar: navidad primero
		mvc.perform(post("/api/admin/categories/reorder").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("[\"navidad\"]"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].slug", is("navidad")))
				.andExpect(jsonPath("$[0].products", is(1)));

		// Editar
		mvc.perform(put("/api/admin/categories/navidad").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Navidad 2026\",\"description\":\"\",\"icon\":\"star\",\"hue\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Navidad 2026")));

		// "figuras" tiene productos que solo están en ella -> no se puede eliminar
		mvc.perform(delete("/api/admin/categories/figuras").header("Authorization", auth))
				.andExpect(status().isConflict());

		// Eliminar "navidad": el Grinch queda solo en decoración (que pasa a ser la principal)
		mvc.perform(delete("/api/admin/categories/navidad").header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/products/grinch").header("Authorization", auth))
				.andExpect(jsonPath("$.category", is("decoracion")))
				.andExpect(jsonPath("$.categories", hasSize(1)));
	}

	@Test
	void cuadraturaSemanal() throws Exception {
		String auth = token();
		String soldAt = java.time.Instant.now().minusSeconds(86400).toString();
		String cut = java.time.Instant.now().plusSeconds(3600).toString();
		String json = productJson("Figura cuadrada", false, "[]").replace("\"hidden\":false}",
				"\"hidden\":false,\"cost\":{\"grams\":10,\"batchHours\":2,\"batchPieces\":1}}");
		mvc.perform(post("/api/admin/products").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isCreated());
		// 2 × 25990 = 51980 · materia prima 2 × 150 = 300 · máquina 10% = 5198 · ganancia 46482
		String sale = mvc.perform(post("/api/admin/sales").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"figura-cuadrada\",\"quantity\":2,\"soldAt\":\"" + soldAt + "\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String saleId = sale.replaceAll("^\\{\"id\":([0-9]+).*", "$1");

		mvc.perform(get("/api/admin/closings/preview").param("to", cut).header("Authorization", auth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.salesCount", is(1)))
				.andExpect(jsonPath("$.revenue", is(51980.0)))
				.andExpect(jsonPath("$.materials", is(300.0)))
				.andExpect(jsonPath("$.machine", is(5198.0)))
				.andExpect(jsonPath("$.profit", is(46482.0)));

		// Cerrar la semana retirando un poco menos de ganancia
		String closing = mvc.perform(post("/api/admin/closings").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"to\":\"" + cut + "\",\"profitWithdrawn\":40000}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.machineWithdrawn", is(5198.0)))
				.andExpect(jsonPath("$.materialsReinvested", is(300.0)))
				.andExpect(jsonPath("$.profitWithdrawn", is(40000.0)))
				.andReturn().getResponse().getContentAsString();
		String closingId = closing.replaceAll("^\\{\"id\":([0-9]+).*", "$1");

		mvc.perform(get("/api/admin/closings").header("Authorization", auth))
				.andExpect(jsonPath("$.machinePrice", is(1300000)))
				.andExpect(jsonPath("$.machineRecovered", is(5198.0)))
				.andExpect(jsonPath("$.machineRemaining", is(1294802.0)))
				.andExpect(jsonPath("$.pendingSales", is(0)))
				.andExpect(jsonPath("$.closings", hasSize(1)));

		// No se repite y la venta ya cerrada no se puede anular
		mvc.perform(post("/api/admin/closings").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
				.content("{\"to\":\"" + cut + "\"}"))
				.andExpect(status().isConflict());
		mvc.perform(delete("/api/admin/sales/" + saleId).header("Authorization", auth)).andExpect(status().isConflict());

		// Anular la cuadratura deja la venta pendiente otra vez
		mvc.perform(delete("/api/admin/closings/" + closingId).header("Authorization", auth)).andExpect(status().isNoContent());
		mvc.perform(get("/api/admin/closings").header("Authorization", auth))
				.andExpect(jsonPath("$.machineRecovered", is(0.0)))
				.andExpect(jsonPath("$.pendingSales", is(1)));
		mvc.perform(delete("/api/admin/sales/" + saleId).header("Authorization", auth)).andExpect(status().isNoContent());
	}

	@Test
	void vendedorSoloRegistraYVeSusVentasSinCostos() throws Exception {
		String admin = token();
		String json = productJson("Llavero perro", false, "[]").replace("\"hidden\":false}",
				"\"hidden\":false,\"cost\":{\"grams\":10,\"batchHours\":2,\"batchPieces\":1}}");
		mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content(json)).andExpect(status().isCreated()); // stock 5, precio 25990
		mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Llavero oculto", true, "[]"))).andExpect(status().isCreated());
		mvc.perform(post("/api/admin/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Vale\",\"username\":\"vale\",\"password\":\"secreta123\",\"role\":\"SELLER\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role", is("SELLER")));
		String vale = loginAs("vale", "secreta123");

		// Una venta del admin (la vendedora no debe verla)
		mvc.perform(post("/api/admin/sales").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"llavero-perro\",\"quantity\":1}")).andExpect(status().isCreated());

		// La vendedora vende: precio de catálogo (ignora el que manda), sin costos en la respuesta
		mvc.perform(post("/api/admin/sales").header("Authorization", vale).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"llavero-perro\",\"quantity\":2,\"unitPrice\":100,\"soldAt\":\"2020-01-01T12:00:00Z\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.unitPrice", is(25990)))
				.andExpect(jsonPath("$.soldBy", is("vale")))
				.andExpect(jsonPath("$.soldByName", is("Vale")))
				.andExpect(jsonPath("$.unitCost").doesNotExist())
				.andExpect(jsonPath("$.soldAt", not(startsWith("2020"))));
		mvc.perform(get("/api/admin/sales").header("Authorization", vale))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].unitCost").doesNotExist());
		mvc.perform(get("/api/admin/sales").header("Authorization", admin))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].unitCost").exists());
		mvc.perform(get("/api/admin/products/llavero-perro").header("Authorization", admin))
				.andExpect(jsonPath("$.stock", is(2)));

		// No puede vender productos ocultos
		mvc.perform(post("/api/admin/sales").header("Authorization", vale).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"llavero-oculto\",\"quantity\":1}")).andExpect(status().isNotFound());

		// Todo lo demás del panel le está prohibido
		String saleId = mvc.perform(get("/api/admin/sales").header("Authorization", vale))
				.andReturn().getResponse().getContentAsString().replaceAll("^\\[\\{\"id\":([0-9]+).*", "$1");
		mvc.perform(delete("/api/admin/sales/" + saleId).header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/products").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/settings/costs").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/closings").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/users").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/deliveries").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/accessories").header("Authorization", vale)).andExpect(status().isForbidden());
		mvc.perform(get("/api/auth/me").header("Authorization", vale)).andExpect(jsonPath("$.role", is("SELLER")));
	}

	@Test
	void entregasDeInventarioSumanStockAlAceptar() throws Exception {
		String admin = token();
		mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content(productJson("Llavero gato", false, "[]"))).andExpect(status().isCreated()); // stock 5
		mvc.perform(post("/api/admin/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Pedro\",\"username\":\"pedro\",\"password\":\"secreta123\",\"role\":\"STAFF\"}"))
				.andExpect(status().isCreated());
		String pedro = loginAs("pedro", "secreta123");

		// El trabajador puede elegir a quién entregar (todos menos él)
		mvc.perform(get("/api/admin/deliveries/recipients").header("Authorization", pedro))
				.andExpect(jsonPath("$[*].username", hasItem("admin")))
				.andExpect(jsonPath("$[*].username", not(hasItem("pedro"))));

		// El admin entrega 10 llaveros (en dos líneas que se suman) a Pedro
		String body = mvc.perform(post("/api/admin/deliveries").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"recipientUsername":"pedro","note":"Primera tanda",
						 "items":[{"productId":"llavero-gato","quantity":6},{"productId":"llavero-gato","quantity":4}]}"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("PENDING")))
				.andExpect(jsonPath("$.senderName", is("Administrador")))
				.andExpect(jsonPath("$.recipientName", is("Pedro")))
				.andExpect(jsonPath("$.totalUnits", is(10)))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andReturn().getResponse().getContentAsString();
		String id = body.replaceAll("^\\{\"id\":([0-9]+).*", "$1");

		// Mientras esté pendiente, el stock no cambia
		mvc.perform(get("/api/admin/products/llavero-gato").header("Authorization", admin))
				.andExpect(jsonPath("$.stock", is(5)));
		mvc.perform(get("/api/admin/deliveries/pending-count").header("Authorization", pedro))
				.andExpect(jsonPath("$.count", is(1)));

		// Solo quien recibe puede aceptar
		mvc.perform(post("/api/admin/deliveries/" + id + "/accept").header("Authorization", admin))
				.andExpect(status().isForbidden());

		// Comentario y aceptación
		mvc.perform(post("/api/admin/deliveries/" + id + "/comments").header("Authorization", pedro)
				.contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/admin/deliveries/" + id + "/accept").header("Authorization", pedro)
				.contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Recibidos, todo ok\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ACCEPTED")))
				.andExpect(jsonPath("$.resolvedBy", is("pedro")))
				.andExpect(jsonPath("$.comments[0].body", is("Recibidos, todo ok")))
				.andExpect(jsonPath("$.comments[0].authorName", is("Pedro")));
		mvc.perform(get("/api/admin/products/llavero-gato").header("Authorization", admin))
				.andExpect(jsonPath("$.stock", is(15)));

		// No se puede aceptar dos veces
		mvc.perform(post("/api/admin/deliveries/" + id + "/accept").header("Authorization", pedro))
				.andExpect(status().isConflict());

		// Rechazar y cancelar no suman stock
		String second = mvc.perform(post("/api/admin/deliveries").header("Authorization", admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientUsername\":\"pedro\",\"items\":[{\"productId\":\"llavero-gato\",\"quantity\":3}]}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		mvc.perform(post("/api/admin/deliveries/" + second.replaceAll("^\\{\"id\":([0-9]+).*", "$1") + "/reject")
				.header("Authorization", pedro).contentType(MediaType.APPLICATION_JSON)
				.content("{\"comment\":\"Faltan 2\"}"))
				.andExpect(jsonPath("$.status", is("REJECTED")));
		String third = mvc.perform(post("/api/admin/deliveries").header("Authorization", pedro)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientUsername\":\"admin\",\"items\":[{\"productId\":\"llavero-gato\",\"quantity\":1}]}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		mvc.perform(post("/api/admin/deliveries/" + third.replaceAll("^\\{\"id\":([0-9]+).*", "$1") + "/cancel")
				.header("Authorization", pedro))
				.andExpect(jsonPath("$.status", is("CANCELLED")));
		mvc.perform(get("/api/admin/products/llavero-gato").header("Authorization", admin))
				.andExpect(jsonPath("$.stock", is(15)));

		// Validaciones: a uno mismo, sin productos
		mvc.perform(post("/api/admin/deliveries").header("Authorization", pedro).contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientUsername\":\"pedro\",\"items\":[{\"productId\":\"llavero-gato\",\"quantity\":1}]}"))
				.andExpect(status().isBadRequest());
		mvc.perform(post("/api/admin/deliveries").header("Authorization", pedro).contentType(MediaType.APPLICATION_JSON)
				.content("{\"recipientUsername\":\"admin\",\"items\":[]}"))
				.andExpect(status().isBadRequest());

		mvc.perform(get("/api/admin/deliveries").header("Authorization", pedro)).andExpect(jsonPath("$", hasSize(3)));
		mvc.perform(get("/api/admin/deliveries").param("status", "ACCEPTED").header("Authorization", admin))
				.andExpect(jsonPath("$", hasSize(1)));
	}
}
