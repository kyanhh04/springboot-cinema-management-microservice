package com.example.inventory_service.service;

import com.example.inventory_service.entity.CinemaInventory;
import com.example.inventory_service.entity.Product;
import com.example.inventory_service.repository.CinemaInventoryRepository;
import com.example.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CinemaInventoryRepository cinemaInventoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getAvailableProductsByCinema(Long cinemaId) {
        return cinemaInventoryRepository.findByCinemaId(cinemaId).stream()
                .filter(inventory -> inventory.getQuantity() > 0)
                .map(CinemaInventory::getProduct)
                .filter(product -> Boolean.TRUE.equals(product.getIsAvailable())
                        && product.getStatus() == Product.ProductStatus.ACTIVE)
                .toList();
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setCostPrice(productDetails.getCostPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setIsAvailable(productDetails.getIsAvailable());
        product.setStatus(productDetails.getStatus());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
