package br.com.carloseduardo.logisticasistema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sistema")
public class ResetController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @DeleteMapping("/reset-master")
    public ResponseEntity<?> resetAll() {
        // 🔥 Apaga o banco de dados INTEIRO! Todas as coleções são zeradas.
        mongoTemplate.getDb().drop();
        return ResponseEntity.ok(Map.of("mensagem", "Banco de dados 100% zerado sem deixar rastros!"));
    }
}