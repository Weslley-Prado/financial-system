package com.itau.transferapi.application.port.input;

import com.itau.transferapi.application.dto.request.TransferRequest;
import com.itau.transferapi.application.dto.response.TransferResponse;

/**
 * Porta de entrada para o caso de uso de Transferência.
 * 
 * Define o contrato para execução de transferências bancárias,
 * seguindo o padrão Ports & Adapters (Hexagonal Architecture).
 */
public interface TransferUseCase {
    
    /**
     * Executa uma transferência bancária entre contas.
     * 
     * Fluxo:
     * 1. Valida os dados da requisição
     * 2. Busca dados do cliente na API de Cadastro
     * 3. Valida se a conta de origem está ativa
     * 4. Valida limite disponível
     * 5. Valida limite diário
     * 6. Executa a transferência
     * 7. Notifica o BACEN
     * 
     * @param request dados da transferência
     * @return resposta com detalhes da transferência
     */
    TransferResponse execute(TransferRequest request);
}


