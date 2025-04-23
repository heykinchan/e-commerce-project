package com.comp5348.Store.controller;

import com.comp5348.Store.model.Order;
import com.comp5348.Store.model.WarehouseStock;
import com.comp5348.Store.repository.WarehouseRepository;
import com.comp5348.Store.repository.OrderRepository;
import com.comp5348.Store.service.OrderService;
import com.comp5348.Store.service.StoreMQService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final MessageChannel emailOut;
    private final MessageChannel bankOut;
    private final MessageChannel deliveryOut;
    private final StoreMQService storeMQService;
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderRepository orderRepository, WarehouseRepository warehouseRepository, MessageChannel emailOut, MessageChannel bankOut, MessageChannel deliveryOut, StoreMQService storeMQService, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.emailOut = emailOut;
        this.bankOut = bankOut;
        this.deliveryOut = deliveryOut;
        this.storeMQService = storeMQService;
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(@RequestBody Order orderRequest) throws JsonProcessingException {

        // Check stock
        List<WarehouseStock> stockList = warehouseRepository.findByProductId(orderRequest.getProductId());
        int totalAvailableStock = stockList.stream()
                .mapToInt(WarehouseStock::getStockLevel)
                .sum();
        if (totalAvailableStock < orderRequest.getQuantity()) {
            return ResponseEntity.badRequest().body("Insufficient stock, please try again!");
        }
        // Request payment

        // Update Stock >> Function in StoreService
//        int remainingQuantity = orderRequest.getQuantity();
//        for (WarehouseStock stock : stockList) {
//            if (remainingQuantity <= 0) break;
//
//            int deducted = Math.min(stock.getStockLevel(), remainingQuantity);
//            stock.setStockLevel(stock.getStockLevel() - deducted);
//            remainingQuantity -= deducted;
//
//            warehouseRepository.save(stock);
//            globalUsageList.add(new AbstractMap.SimpleEntry<>(stock.getWarehouseId(), deducted));
//        }

        // Update order status
        orderRequest.setStatus("PENDING");
        orderRequest.setCreatedAt(Timestamp.from(Instant.now()));
        orderRepository.save(orderRequest);
        storeMQService.requestTransfer(orderRequest.getOrderId(), orderRequest.getTotalAmount(),"pending",bankOut);
        return ResponseEntity.ok("order completed successfully, waiting for paying");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/updateStatus/{orderId}")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Order updatedOrder) {

        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(updatedOrder.getStatus());
            orderRepository.save(order);

            return ResponseEntity.ok("Order " + orderId + " status updated to " + updatedOrder.getStatus() + ".");
        } else {
            return ResponseEntity.status(404).body("Order not found.");
        }
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) throws JsonProcessingException {
        storeMQService.cancelOrder(orderId,deliveryOut,emailOut,bankOut);
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            if ("PAID".equals(order.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Paid orders cannot be cancelled.");
            }

            // Retrieve usage for the specific order
            List<Map.Entry<Integer, Integer>> usageList = orderService.getUsageByOrderId(orderId);

            for (Map.Entry<Integer, Integer> entry : usageList) {
                Integer warehouseId = entry.getKey();
                Integer quantity = entry.getValue();
                try {
                    WarehouseStock stock = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));

                    stock.setStockLevel(stock.getStockLevel() + quantity);
                    warehouseRepository.save(stock);
                } catch (RuntimeException e) {
                    logger.warn("Failed to update stock for warehouse {}: {}", warehouseId, e.getMessage());
                }
            }

            // Clear the usage after cancellation
            orderService.clearUsageByOrderId(orderId);

            // Update order status to CANCELLED
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            return ResponseEntity.ok("Order cancelled and stock returned.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found.");
        }
    }


}



