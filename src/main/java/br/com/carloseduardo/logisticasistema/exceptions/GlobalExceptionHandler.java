package br.com.carloseduardo.logisticasistema.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Esse metodo captura qualquer erro (Exception) do sistema
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {

        // Imprime o erro no console em vermelho para você ver no IntelliJ
        System.err.println("\n❌ ERRO TAL: " + ex.getMessage() + "\n");
        ex.printStackTrace(); // Mostra a linha exata onde o erro ocorreu

        // Prepara a resposta JSON bonitinha para o Front-end ler
        Map<String, String> erroResponse = new HashMap<>();
        erroResponse.put("erro", "Erro no servidor: " + ex.getMessage());

        // Devolve o erro com o status 500 (Internal Server Error)
        return new ResponseEntity<>(erroResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}