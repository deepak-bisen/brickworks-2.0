package com.brickwork.products.product.controller;

import com.brickwork.products.product.dto.ProductDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequestMapping("/api/products")
public interface ProductController {

    @GetMapping("/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable("productId") String productId);

    @GetMapping("/all")
    ResponseEntity<List<ProductDTO>> getAllProducts();

    @GetMapping("/category/{category}")
    ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Sets the HTTP status code to 201 Created on success
    ResponseEntity<?> createProduct(@ModelAttribute ProductDTO productDTO, @RequestParam("imageFile") MultipartFile imageFile); // @RequestBody tells Spring to convert the incoming JSON into a Product object

    @PatchMapping(value = "/{productID}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> updateProduct(@PathVariable("productID") String productID,
                                    @ModelAttribute ProductDTO productDTO,
                                    @RequestPart(value = "imageFile", required = false) MultipartFile imageFile);

    @DeleteMapping("/{productId}")
    ResponseEntity<Void> deleteProduct(@PathVariable String productId);

    @PutMapping("/{id}/deduct-stock")
    ResponseEntity<Void> deductStock(@PathVariable String id, @RequestParam int quantity);
}