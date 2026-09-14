package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductDto;
import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto getProductById(Long id);
    List<ProductDto> getProductsByCategory(String category);
    List<ProductDto> searchProducts(String keyword);
}
