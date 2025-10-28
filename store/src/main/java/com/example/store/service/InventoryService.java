//package com.example.store.service.saga;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class InventoryService {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @RabbitListener(queues = "inventory.queue")
//    public void handleDeductInventory(OrderRequest request) {
//        InventoryReply reply = new InventoryReply(request.getOrderId());
//        try {
//            // Local Transaction: Deduct stock
//            System.out.println("[Inventory] Deducting stock. Product ID: " + request.getProductId());
//            // inventoryRepository.deductStock(request.getProductId(), request.getQuantity());
//            reply.setSuccess(true);
//        } catch (Exception e) {
//            System.out.println("[Inventory] Insufficient stock or an error occurred!");
//            reply.setSuccess(false);
//        }
//
//        // Send reply to orchestrator
//        rabbitTemplate.convertAndSend("order.inventory.reply.queue", reply);
//    }
//
//    // Method listening for compensation operation
//    @RabbitListener(queues = "inventory.compensation.queue")
//    public void handleRestoreInventory(RestoreStockRequest request) {
//        System.out.println("[Inventory] COMPENSATION: Restoring stock. Order ID: " + request.getOrderId());
//        // inventoryRepository.restoreStock(...);
//    }
//}
