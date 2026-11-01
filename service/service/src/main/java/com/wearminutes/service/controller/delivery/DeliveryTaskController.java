package com.wearminutes.service.controller.delivery;

import com.wearminutes.service.dto.entity.DeliveryTask;
import com.wearminutes.service.dto.request.DeliveryTaskUpdateRequest;
import com.wearminutes.service.service.DeliveryTaskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery/tasks")
@CrossOrigin(origins = "*")
public class DeliveryTaskController {

    private final DeliveryTaskService deliveryTaskService;

    public DeliveryTaskController(DeliveryTaskService deliveryTaskService) {
        this.deliveryTaskService = deliveryTaskService;
    }

    @GetMapping
    @Operation(summary = "List assigned and unassigned delivery tasks")
    public ResponseEntity<List<DeliveryTask>> getTasks(Authentication authentication) {
        return ResponseEntity.ok(deliveryTaskService.getTasksForDeliveryPerson(authentication.getName()));
    }

    @PostMapping("/{taskId}/claim")
    @Operation(summary = "Claim an unassigned delivery task")
    public ResponseEntity<DeliveryTask> claimTask(@PathVariable Long taskId, Authentication authentication) {
        return ResponseEntity.ok(deliveryTaskService.claimTask(taskId, authentication.getName()));
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "Advance a delivery task; completing requires the order delivery OTP")
    public ResponseEntity<DeliveryTask> updateTask(
            @PathVariable Long taskId,
            @RequestBody DeliveryTaskUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(deliveryTaskService.updateTask(
                taskId,
                authentication.getName(),
                request.getStatus(),
                request.getDeliveryOtp()
        ));
    }
}
