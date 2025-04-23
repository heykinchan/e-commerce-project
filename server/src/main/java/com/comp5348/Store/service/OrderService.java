package com.comp5348.Store.service;

import com.comp5348.Store.model.WarehouseStock;
import com.comp5348.Store.repository.OrderRepository;
import com.comp5348.Store.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.comp5348.Store.model.Order;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;

    // Store usage per order using orderId as the key
    public final Map<Long, List<Map.Entry<Integer, Integer>>> globalUsageMap;

    @Autowired
    public OrderService(OrderRepository orderRepository, WarehouseRepository warehouseRepository) {
        this.orderRepository = orderRepository;
        this.warehouseRepository = warehouseRepository;
        this.globalUsageMap = new HashMap<>();
    }

    @Transactional
    public void updateWarehouse(Order order) {
        List<WarehouseStock> stockList = warehouseRepository.findByProductId(order.getProductId());
        int remainingQuantity = order.getQuantity();
        List<Map.Entry<Integer, Integer>> usageList = new ArrayList<>();

        for (WarehouseStock stock : stockList) {
            if (remainingQuantity <= 0) break;

            int deducted = Math.min(stock.getStockLevel(), remainingQuantity);
            stock.setStockLevel(stock.getStockLevel() - deducted);
            remainingQuantity -= deducted;

            warehouseRepository.save(stock);
            usageList.add(new AbstractMap.SimpleEntry<>(stock.getWarehouseId(), deducted));
        }

        // Store usage for this specific order
        globalUsageMap.put(order.getOrderId(), usageList);
    }

    public List<Map.Entry<Integer, Integer>> getUsageByOrderId(Long orderId) {
        return globalUsageMap.getOrDefault(orderId, new ArrayList<>());
    }

    public void clearUsageByOrderId(Long orderId) {
        globalUsageMap.remove(orderId);
    }
}
