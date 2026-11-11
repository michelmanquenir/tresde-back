-- Los clientes inician sesión con su correo, que puede superar los 60 caracteres.
ALTER TABLE users ALTER COLUMN username SET DATA TYPE VARCHAR(255);
