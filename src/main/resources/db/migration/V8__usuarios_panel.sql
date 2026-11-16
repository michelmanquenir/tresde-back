-- Usuarios creados por el administrador (trabajadores). Al cambiar la contraseña se cierran las sesiones
-- abiertas: los tokens emitidos antes de sessions_since dejan de valer.
ALTER TABLE users ADD COLUMN sessions_since TIMESTAMP WITH TIME ZONE;
