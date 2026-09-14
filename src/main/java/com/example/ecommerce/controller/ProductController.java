package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.ProductDto;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductDto>> getAllProducts() {
        return ApiResponse.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<ProductDto>> getByCategory(@PathVariable String category) {
        return ApiResponse.success(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductDto>> search(@RequestParam String keyword) {
        return ApiResponse.success(productService.searchProducts(keyword));
    }
}