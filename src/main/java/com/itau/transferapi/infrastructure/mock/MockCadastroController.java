package com.itau.transferapi.infrastructure.mock;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock da API de Cadastro para desenvolvimento e testes.
 * 
 * Este controller simula a API externa de Cadastro,
 * permitindo testes locais sem dependência de serviços externos.
 * 
 * Ativo apenas nos perfis: local, test
 */
@Slf4j
@RestController
@RequestMapping("/mock/cadastro/api/v1/clients")
@Profile({"local", "test"})
@Hidden
public class MockCadastroController {
    
    private final Map<UUID, ClientData> clients = new ConcurrentHashMap<>();
    
    public MockCadastroController() {
        // Inicializa com alguns clientes de teste
        initializeTestData();
    }
    
    private void initializeTestData() {
        UUID client1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID client2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID client3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        
        clients.put(client1, new ClientData(client1, "João Silva", "12345678900", true));
        clients.put(client2, new ClientData(client2, "Maria Santos", "98765432100", true));
        clients.put(client3, new ClientData(client3, "Carlos Oliveira", "11122233344", false));
    }
    
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientData> getClient(@PathVariable UUID clientId) {
        log.debug("[MOCK CADASTRO] Buscando cliente: {}", clientId);
        
        ClientData client = clients.get(clientId);
        if (client == null) {
            log.debug("[MOCK CADASTRO] Cliente não encontrado: {}", clientId);
            return ResponseEntity.notFound().build();
        }
        
        log.debug("[MOCK CADASTRO] Cliente encontrado: {}", client.name());
        return ResponseEntity.ok(client);
    }
    
    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<ClientData> getClientByDocument(@PathVariable String documentNumber) {
        log.debug("[MOCK CADASTRO] Buscando cliente por documento: {}", documentNumber);
        
        return clients.values().stream()
            .filter(c -> c.documentNumber().equals(documentNumber))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ClientData> createClient(@RequestBody ClientData client) {
        log.debug("[MOCK CADASTRO] Criando cliente: {}", client.name());
        clients.put(client.id(), client);
        return ResponseEntity.ok(client);
    }
    
    public record ClientData(
        UUID id,
        String name,
        String documentNumber,
        boolean active
    ) {}
}


