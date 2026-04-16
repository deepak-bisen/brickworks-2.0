package com.brickwork.products.controller.impl;

import com.brickwork.products.controller.ProductController;
import com.brickwork.products.dto.ProductDTO;
import com.brickwork.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProductControllerImpl implements ProductController {

    @Autowired
    private ProductService productService;

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
    public ResponseEntity<?> createProduct(ProductDTO productDTO, MultipartFile imageFile) { // @RequestBody tells Spring to convert the incoming JSON into a Product object
        try {
            ProductDTO saveProduct = productService.createProduct(productDTO, imageFile);
            return new ResponseEntity<>(saveProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a product by its ID.
     * This is a protected endpoint and requires an authenticated JWT.
     *
     * @param productId The ID of the product to delete.
     * @return A 204 No Content response on success, or 404 Not Found.
     */

    public ResponseEntity<Void> deleteProduct(String productId) {
        try {
            productService.deleteProduct(productId);
            System.out.println("deletion successful");
            // Return a 204 No Content status, which is the standard for a successful DELETE.
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // If the service throws an exception (e.g., "Product not found"),
            // return a 404 Not Found.
            System.out.println("deletion not done!");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates an existing product by its ID.
     * This is a protected endpoint and requires an authenticated JWT.
     *
     * @param productID  The ID of the product to update.
     * @param productDTO The new product data from the request body.
     * @return A 200 OK response with the updated product, or 404 Not Found.
     */
    public ResponseEntity<?> updateProduct(String productID, ProductDTO productDTO, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(productID, productDTO, imageFile);
            // Return 200 OK with the updated product
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            // If the service throws "Product not found", return 404
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}