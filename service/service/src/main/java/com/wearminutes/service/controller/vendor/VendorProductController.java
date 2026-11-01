package com.wearminutes.service.controller.vendor;

import com.wearminutes.service.dto.entity.Product;
import com.wearminutes.service.dto.request.ProductRequest;
import com.wearminutes.service.service.vendor.VendorProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/products")
@CrossOrigin(origins = "*")
@Tag(name = "Vendor Inventory Management", description = "Endpoints for vendors to add, update, list, and delete products")
public class VendorProductController {

    @Autowired
    private VendorProductService vendorProductService;

    @PostMapping
    @Operation(summary = "Add a new product to inventory")
    public ResponseEntity<Product> addProduct(@RequestBody ProductRequest request) {
        Product product = vendorProductService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/shop/{shopId}")
    @Operation(summary = "Get all products for a specific shop")
    public ResponseEntity<List<Product>> getProductsByShop(@PathVariable Long shopId) {
        List<Product> products = vendorProductService.getProductsByShop(shopId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = vendorProductService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product details and stock")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        Product updatedProduct = vendorProductService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product from inventory")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        vendorProductService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully with ID: " + id);
    }
}
