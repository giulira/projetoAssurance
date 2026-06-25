package br.com.assurance.projetoassurance;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    ResponseEntity<String> retornarQqCoisaEC2(){
        return ResponseEntity.ok("UP");
    }
}
