package com.wearminutes.service.service;

import com.wearminutes.service.dto.entity.DeliveryTask;
import com.wearminutes.service.dto.entity.Order;
import com.wearminutes.service.dto.entity.User;
import com.wearminutes.service.event.DeliveryTaskEvent;
import com.wearminutes.service.repository.DeliveryTaskRepository;
import com.wearminutes.service.repository.OrderRepository;
import com.wearminutes.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeliveryTaskService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryTaskService.class);
    private static final List<DeliveryTask.Status> OPEN_STATUSES =
            List.of(DeliveryTask.Status.ASSIGNED, DeliveryTask.Status.IN_PROGRESS);

    private final DeliveryTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public DeliveryTaskService(
            DeliveryTaskRepository taskRepository,
            UserRepository userRepository,
            OrderRepository orderRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "delivery-tasks", groupId = "delivery-task-processor")
    @Transactional
    public void processDeliveryTask(DeliveryTaskEvent event) {
        if (event == null || event.getOrderId() == null) {
            throw new IllegalArgumentException("Delivery task event must include an orderId");
        }
        if (taskRepository.findByOrderId(event.getOrderId()).isPresent()) {
            logger.info("Delivery task already exists for orderId={}", event.getOrderId());
            return;
        }

        DeliveryTask task = new DeliveryTask();
        task.setOrderId(event.getOrderId());
        Optional<User> assignee = userRepository.findByRoleAndIsActiveTrueOrderByIdAsc("DELIVERY")
                .stream()
                .min((left, right) -> Long.compare(
                        openTaskCount(left.getId()),
                        openTaskCount(right.getId())
                ));

        if (assignee.isPresent()) {
            task.setDeliveryPersonId(assignee.get().getId());
            task.setStatus(DeliveryTask.Status.ASSIGNED);
            logger.info("Assigned delivery task orderId={} deliveryPersonId={}",
                    event.getOrderId(), assignee.get().getId());
        } else {
            task.setStatus(DeliveryTask.Status.UNASSIGNED);
            logger.warn("No active delivery personnel; created unassigned task for orderId={}", event.getOrderId());
        }
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<DeliveryTask> getTasksForDeliveryPerson(String email) {
        User deliveryPerson = getActiveDeliveryPerson(email);
        List<DeliveryTask> tasks = taskRepository.findByDeliveryPersonIdOrderByCreatedAtDesc(deliveryPerson.getId());
        tasks.addAll(taskRepository.findByStatusOrderByCreatedAtAsc(DeliveryTask.Status.UNASSIGNED));
        return tasks;
    }

    @Transactional
    public DeliveryTask claimTask(Long taskId, String email) {
        User deliveryPerson = getActiveDeliveryPerson(email);
        DeliveryTask task = taskRepository.findWithLockingById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery task not found: " + taskId));
        if (task.getStatus() != DeliveryTask.Status.UNASSIGNED) {
            throw new IllegalStateException("Only unassigned tasks can be claimed");
        }
        task.setDeliveryPersonId(deliveryPerson.getId());
        task.setStatus(DeliveryTask.Status.ASSIGNED);
        return taskRepository.save(task);
    }

    @Transactional
    public DeliveryTask updateTask(Long taskId, String email, DeliveryTask.Status requestedStatus, String deliveryOtp) {
        User deliveryPerson = getActiveDeliveryPerson(email);
        DeliveryTask task = taskRepository.findWithLockingById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery task not found: " + taskId));
        if (!deliveryPerson.getId().equals(task.getDeliveryPersonId())) {
            throw new IllegalStateException("Delivery task is not assigned to the authenticated user");
        }

        Order order = orderRepository.findById(task.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order not found for orderId=" + task.getOrderId()));
        if (task.getStatus() == DeliveryTask.Status.ASSIGNED
                && requestedStatus == DeliveryTask.Status.IN_PROGRESS) {
            task.setStatus(DeliveryTask.Status.IN_PROGRESS);
            order.setOrderStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        } else if (task.getStatus() == DeliveryTask.Status.IN_PROGRESS
                && requestedStatus == DeliveryTask.Status.COMPLETED) {
            if (deliveryOtp == null || !deliveryOtp.equals(order.getDeliveryOtp())) {
                throw new IllegalArgumentException("A valid delivery OTP is required to complete the task");
            }
            task.setStatus(DeliveryTask.Status.COMPLETED);
            order.setOrderStatus(Order.OrderStatus.DELIVERED);
        } else {
            throw new IllegalStateException("Invalid delivery task status transition: "
                    + task.getStatus() + " -> " + requestedStatus);
        }
        orderRepository.save(order);
        return taskRepository.save(task);
    }

    private long openTaskCount(Long deliveryPersonId) {
        return taskRepository.countByDeliveryPersonIdAndStatusIn(deliveryPersonId, OPEN_STATUSES);
    }

    private User getActiveDeliveryPerson(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated delivery user was not found"));
        if (!"DELIVERY".equals(user.getRole()) || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Only active delivery personnel can process tasks");
        }
        return user;
    }
}
