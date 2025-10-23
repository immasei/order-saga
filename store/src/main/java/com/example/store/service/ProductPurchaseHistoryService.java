package com.example.store.service;

import com.example.store.model.OrderItems;
import com.example.store.model.ProductPurchaseHistory;
import com.example.store.repository.ProductPurchaseHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductPurchaseHistoryService {

    @Autowired
    private ProductPurchaseHistoryRepository historyRepo;

    public void updatePurchaseHistory(List<OrderItems> orderItems) {
        for (OrderItems item : orderItems) {
            UUID productId = item.getProduct().getId();
            ProductPurchaseHistory history = historyRepo.findByProductId(productId)
                    .orElse(new ProductPurchaseHistory(item.getProduct(), 0));

            history.setPurchaseCount(history.getPurchaseCount() + item.getQuantity());
            historyRepo.save(history);
        }
    }
}

