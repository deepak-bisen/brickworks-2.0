package com.brickwork.products.product.controller.impl;

import com.brickwork.products.product.controller.ProductController;
import com.brickwork.products.product.dto.ProductDTO;
import com.brickwork.products.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class ProductControllerImpl implements ProductController {

    @Autowired
    private ProductService productService;

    @Override
    public ResponseEntity<byte[]> getProductImage(String productId) {
        var image = productService.getProductImage(productId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getData());
    }

    @Override
    public ResponseEntity<ProductDTO> getProductById(String productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @Override
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Override
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @Override
    public ResponseEntity<ProductDTO> createProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        ProductDTO saveProduct = productService.createProduct(productDTO, imageFile);
        return new ResponseEntity<>(saveProduct, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(String productId) {
        productService.deleteProduct(productId);
        log.info("Product deleted successfully: {}", productId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deductStock(String id, int quantity) {
        productService.deductStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ProductDTO> updateProduct(String productID, ProductDTO productDTO,
                                                    @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        ProductDTO updatedProduct = productService.updateProduct(productID, productDTO, imageFile);
        return ResponseEntity.ok(updatedProduct);
    }
}