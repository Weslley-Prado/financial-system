package com.itau.transferapi.application.port.input;

import com.itau.transferapi.application.dto.response.BalanceResponse;

/**
 * Porta de entrada para o caso de uso de Consulta de Saldo.
 * 
 * Define o contrato para consulta de saldo de conta corrente.
 */
public interface BalanceQueryUseCase {
    
    /**
     * Consulta o saldo de uma conta corrente.
     * 
     * @param accountNumber número da conta
     * @param agencyNumber número da agência
     * @return resposta com saldo e limites
     */
    BalanceResponse getBalance(String accountNumber, String agencyNumber);
}


