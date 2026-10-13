package com.tallerwebi.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para cargar las variables de entorno desde un archivo .env
 * al iniciar la aplicación Spring.
 */
@Configuration
public class EnvConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfig.class);

  /**
   * Constructor que lee el archivo .env e inyecta las propiedades en el sistema.
   */
  public EnvConfig() {
    try {
      Dotenv dotenv = Dotenv.load();
      dotenv
        .entries()
        .forEach(entry -> {
          System.setProperty(entry.getKey(), entry.getValue());
        });
      LOGGER.info(".env cargado correctamente.");
    } catch (Exception e) {
      LOGGER.warn("No se encontró el archivo .env o hubo un error al cargarlo.");
    }
  }
}
