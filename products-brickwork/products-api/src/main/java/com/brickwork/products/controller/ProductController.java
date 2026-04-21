package com.brickwork.products.controller;

import com.brickwork.products.dto.ProductDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequestMapping("/api/products")
public interface ProductController {

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable("productId") String productId);

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts();

    @GetMapping("/category/{category}")
    ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable("category") String category);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Sets the HTTP status code to 201 Created on success
    public ResponseEntity<?> createProduct(@RequestPart("productDTO") ProductDTO productDTO, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile); // @RequestBody tells Spring to convert the incoming JSON into a Product object

    @PutMapping(value = "{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@PathVariable String productID, @RequestPart("productDTO") ProductDTO productDTO, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile);

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId);


}