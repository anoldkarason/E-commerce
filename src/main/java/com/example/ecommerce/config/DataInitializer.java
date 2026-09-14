package com.example.ecommerce.config;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Initializing sample products...");

            List<Product> products = List.of(
                    Product.builder()
                            .name("Modern Leather Sofa")
                            .description("Premium 3-seater leather sofa with wooden frame. Perfect for modern living rooms.")
                            .price(new BigDecimal("1250000.00"))
                            .stock(15)
                            .category("Sofas")
                            .image("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=500")
                            .build(),
                    Product.builder()
                            .name("Oak Dining Table")
                            .description("Solid oak dining table for 6 people. Handcrafted with natural finish.")
                            .price(new BigDecimal("850000.00"))
                            .stock(8)
                            .category("Tables")
                            .image("https://images.unsplash.com/photo-1617806118233-18e1de247200?w=500")
                            .build(),
                    Product.builder()
                            .name("Ergonomic Office Chair")
                            .description("High-back office chair with lumbar support and adjustable armrests.")
                            .price(new BigDecimal("450000.00"))
                            .stock(25)
                            .category("Chairs")
                            .image("https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=500")
                            .build(),
                    Product.builder()
                            .name("Queen Bed Frame")
                            .description("Elegant queen-size bed frame with upholstered headboard.")
                            .price(new BigDecimal("980000.00"))
                            .stock(10)
                            .category("Beds")
                            .image("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=500")
                            .build(),
                    Product.builder()
                            .name("Bookshelf Cabinet")
                            .description("5-tier wooden bookshelf with modern design. Assembly required.")
                            .price(new BigDecimal("320000.00"))
                            .stock(20)
                            .category("Storage")
                            .image("https://images.unsplash.com/photo-1594620302200-9a762244a156?w=500")
                            .build(),
                    Product.builder()
                            .name("Coffee Table Set")
                            .description("Set of 2 nesting coffee tables with glass tops and metal legs.")
                            .price(new BigDecimal("280000.00"))
                            .stock(18)
                            .category("Tables")
                            .image("https://images.unsplash.com/photo-1532372320572-cda25653a694?w=500")
                            .build()
            );

            productRepository.saveAll(products);
            log.info("Initialized {} products", products.size());
        }
    }
}