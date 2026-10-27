package com.tallerwebi.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfig.class);

  public EnvConfig() {
    try {
      // Intentamos cargar el .env desde el directorio actual
      Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

      dotenv
        .entries()
        .forEach(entry -> {
          System.setProperty(entry.getKey(), entry.getValue());
        });

      if (System.getProperty("MP_ACCESS_TOKEN") != null) {
        LOGGER.info(".env cargado correctamente y MP_ACCESS_TOKEN configurado.");
      } else {
        LOGGER.warn(".env cargado, pero MP_ACCESS_TOKEN no se encontró dentro del archivo.");
      }
    } catch (Exception e) {
      LOGGER.error("No se pudo cargar el archivo .env", e);
    }
  }
}
