package com.mielchende.order.dto;

/* Resumen para el panel de administración: solicitudes pendientes de atender.
   Es un contador accionable, no una métrica de ventas */
public record OrderSummaryDto(long pendingOrders) { }