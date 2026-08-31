package br.com.carloseduardo.logisticasistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class LogisticaSistemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticaSistemaApplication.class, args);

        // Mensagem de sucesso no console quando o sistema subir!
        System.out.println("\n=======================================================");
        System.out.println("🚀 SISTEMA DE LOGÍSTICA INICIADO COM SUCESSO! 🚀");
        System.out.println("=======================================================\n");
    }
}