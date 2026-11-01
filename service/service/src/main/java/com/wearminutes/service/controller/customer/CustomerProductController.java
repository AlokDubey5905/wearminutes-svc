package com.wearminutes.service.controller.customer;

import com.wearminutes.service.dto.entity.Product;
import com.wearminutes.service.service.vendor.VendorProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/products")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Product Viewing", description = "Endpoints for customers to view products")
public class CustomerProductController {

    @Autowired
    private VendorProductService vendorProductService;

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
}
