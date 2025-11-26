package com.itau.transferapi.application.port.output;

import com.itau.transferapi.domain.entity.Transfer;

/**
 * Porta de saída para notificação ao BACEN.
 * 
 * Define o contrato para notificação de transações
 * ao Banco Central do Brasil.
 */
public interface BacenNotificationPort {
    
    /**
     * Notifica o BACEN sobre uma transferência realizada.
     * 
     * @param transfer transferência a ser notificada
     * @return ID da notificação gerado pelo BACEN
     */
    String notifyTransfer(Transfer transfer);
    
    /**
     * Verifica o status de uma notificação anterior.
     * 
     * @param notificationId ID da notificação
     * @return status da notificação
     */
    NotificationStatus checkNotificationStatus(String notificationId);
    
    /**
     * Status possíveis da notificação ao BACEN.
     */
    enum NotificationStatus {
        PENDING,
        CONFIRMED,
        REJECTED,
        NOT_FOUND
    }
}


