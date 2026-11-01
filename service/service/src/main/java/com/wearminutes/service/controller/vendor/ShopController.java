package com.wearminutes.service.controller.vendor;

import com.wearminutes.service.dto.entity.Shop;
import com.wearminutes.service.dto.request.ShopRequest;
import com.wearminutes.service.service.vendor.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/shops")
@CrossOrigin(origins = "*")
@Tag(name = "Vendor Shop Management", description = "Endpoints for vendors to register, update, list, and delete shops")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @PostMapping
    @Operation(summary = "Register a new shop")
    public ResponseEntity<Shop> registerShop(@RequestBody ShopRequest request) {
        Shop shop = shopService.registerShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(shop);
    }

    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Get all shops for a specific vendor")
    public ResponseEntity<List<Shop>> getShopsByVendor(@PathVariable Long vendorId) {
        List<Shop> shops = shopService.getShopsByVendor(vendorId);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shop details by ID")
    public ResponseEntity<Shop> getShopById(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update shop details")
    public ResponseEntity<Shop> updateShop(@PathVariable Long id, @RequestBody ShopRequest request) {
        Shop updatedShop = shopService.updateShop(id, request);
        return ResponseEntity.ok(updatedShop);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shop")
    public ResponseEntity<String> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok("Shop deleted successfully with ID: " + id);
    }
}
