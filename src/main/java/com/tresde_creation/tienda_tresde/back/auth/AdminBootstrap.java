package com.tresde_creation.tienda_tresde.back.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tresde_creation.tienda_tresde.back.config.AppProperties;

/** Crea el usuario administrador inicial si la tabla de usuarios está vacía. */
@Component
public class AdminBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final AppProperties props;

	public AdminBootstrap(UserRepository users, PasswordEncoder encoder, AppProperties props) {
		this.users = users;
		this.encoder = encoder;
		this.props = props;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (users.count() > 0) return;
		AppProperties.Admin admin = props.admin();
		users.save(new User(admin.username().toLowerCase(), encoder.encode(admin.password()), admin.name(), "ADMIN"));
		log.info("Usuario administrador '{}' creado.", admin.username());
		if ("admin".equals(admin.password())) {
			log.warn("El administrador usa la contraseña por defecto. Define ADMIN_PASSWORD antes de publicar.");
		}
	}
}
