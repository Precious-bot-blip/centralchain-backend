package com.b2b.inventorysystem.service;

import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderResponse;
import com.b2b.inventorysystem.dto.purchaseorder.PurchaseOrderUpdateRequest;
import com.b2b.inventorysystem.entity.*;
import com.b2b.inventorysystem.exception.BadRequestException;
import com.b2b.inventorysystem.exception.ConflictException;
import com.b2b.inventorysystem.exception.ResourceNotFoundException;
import com.b2b.inventorysystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;

    public PurchaseOrderService(PurchaseOrderRepository poRepository,
                                InventoryRepository inventoryRepository,
                                UserRepository userRepository,
                                DeliveryRepository deliveryRepository) {
        this.poRepository = poRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.deliveryRepository = deliveryRepository;
    }

    // READ — Admin/Manager sees ALL
    // Supplier sees their own
    public List<PurchaseOrderResponse> getAllOrders(Integer requestingUserId, String requestingRole) {
        List<PurchaseOrder> orders;

        if ("SUPPLIER".equals(requestingRole)) {
            // Suppliers only sees their own assigned POs
            orders = poRepository.findByAssignedSupplierUserId(requestingUserId);
        } else if ("DRIVER".equals(requestingRole)) {
            // Drivers only sees POs assigned to them
            orders = poRepository.findByAssignedDriverUserId(requestingUserId);
        } else {
            // Admin/Manager sees everything
            orders = poRepository.findAll();
        }

        return orders.stream().map(this::toResponse).toList();
    }

    public PurchaseOrderResponse getOrderById(Integer id) {
        return toResponse(findOrderById(id));
    }

    public PurchaseOrderResponse getOrderByNumber(String poNumber) {
        PurchaseOrder po = poRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found: " + poNumber));
        return toResponse(po);
    }

    public List<PurchaseOrderResponse> getOrdersByStatus(String status) {
        return poRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public List<PurchaseOrderResponse> getOrdersBySupplier(Integer supplierId) {
        return poRepository.findByAssignedSupplierUserId(supplierId).stream().map(this::toResponse).toList();
    }

    public List<PurchaseOrderResponse> getOrdersByDriver(Integer driverId) {
        return poRepository.findByAssignedDriverUserId(driverId).stream().map(this::toResponse).toList();
    }

    public List<PurchaseOrderResponse> getOrdersByCreator(Integer userId) {
        return poRepository.findByCreatedByUserUserId(userId).stream().map(this::toResponse).toList();
    }

    // CREATE
    @Transactional
    public PurchaseOrderResponse createOrder(PurchaseOrderCreateRequest request) {
        Inventory item = inventoryRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.getItemId()));

        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found: " + request.getCreatedByUserId()));

        User supplier = userRepository.findById(request.getAssignedSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getAssignedSupplierId()));

        if (!"SUPPLIER".equals(supplier.getRole().getRoleName())) {
            throw new BadRequestException("Assigned user is not a SUPPLIER: " + supplier.getEmail());
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setItem(item);
        po.setQuantity(request.getQuantity());
        po.setStatus("PENDING");
        po.setCreatedByUser(creator);
        po.setAssignedSupplier(supplier);

        PurchaseOrder saved = poRepository.save(po);
        return toResponse(saved);
    }

    // UPDATE
    @Transactional
    public PurchaseOrderResponse updateOrder(Integer id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder po = findOrderById(id);

        if (!"PENDING".equals(po.getStatus())) {
            throw new ConflictException("Cannot update PO — only PENDING orders can be modified. Current status: " + po.getStatus());
        }

        User supplier = userRepository.findById(request.getAssignedSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getAssignedSupplierId()));

        if (!"SUPPLIER".equals(supplier.getRole().getRoleName())) {
            throw new BadRequestException("Assigned user is not a SUPPLIER: " + supplier.getEmail());
        }

        po.setQuantity(request.getQuantity());
        po.setAssignedSupplier(supplier);

        PurchaseOrder updated = poRepository.save(po);
        return toResponse(updated);
    }

    // STATE TRANSITION: PENDING -> APPROVED
    @Transactional
    public PurchaseOrderResponse approveOrder(Integer id) {
        PurchaseOrder po = findOrderById(id);

        if (!"PENDING".equals(po.getStatus())) {
            throw new ConflictException("Cannot approve — only PENDING orders can be approved. Current status: " + po.getStatus());
        }

        po.setStatus("APPROVED");
        return toResponse(poRepository.save(po));
    }

    // STATE TRANSITION: APPROVED -> PROCESSING (supplier accepts)
    @Transactional
    public PurchaseOrderResponse acceptOrder(Integer id, Integer supplierId) {
        PurchaseOrder po = findOrderById(id);

        if (!"APPROVED".equals(po.getStatus())) {
            throw new ConflictException("Cannot accept — only APPROVED orders can be accepted. Current: " + po.getStatus());
        }

        // Supplier can only accept their own POs
        if (!po.getAssignedSupplier().getUserId().equals(supplierId)) {
            throw new ConflictException("You are not the assigned supplier for this order");
        }

        po.setStatus("PROCESSING");
        return toResponse(poRepository.save(po));
    }


    // STATE TRANSITION: APPROVED -> DISPATCHED
    @Transactional
    public PurchaseOrderResponse dispatchOrder(Integer id, Integer driverId) {
        PurchaseOrder po = findOrderById(id);

        if (!"APPROVED".equals(po.getStatus())) {
            throw new ConflictException("Cannot dispatch — only APPROVED orders can be dispatched. Current status: " + po.getStatus());
        }

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        if (!"DRIVER".equals(driver.getRole().getRoleName())) {
            throw new BadRequestException("Assigned user is not a DRIVER: " + driver.getEmail());
        }

        po.setStatus("DISPATCHED");
        po.setAssignedDriver(driver);

        Delivery delivery = new Delivery();
        delivery.setPurchaseOrder(po);
        delivery.setDriver(driver);
        delivery.setDeliveryStatus("ASSIGNED");

        PurchaseOrder saved = poRepository.save(po);
        deliveryRepository.save(delivery);

        return toResponse(saved);
    }

    // STATE TRANSITION: DISPATCHED -> DELIVERED
    @Transactional
    public PurchaseOrderResponse deliverOrder(Integer id) {
        PurchaseOrder po = findOrderById(id);

        if (!"DISPATCHED".equals(po.getStatus())) {
            throw new ConflictException("Cannot mark delivered — only DISPATCHED orders can be delivered. Current status: " + po.getStatus());
        }

        Inventory item = po.getItem();
        int newQuantity = item.getQuantityInStock() - po.getQuantity();

        if (newQuantity < 0) {
            throw new BadRequestException(
                    "Cannot fulfill — insufficient stock. Item: " + item.getSku()
                            + ", Available: " + item.getQuantityInStock()
                            + ", Required: " + po.getQuantity());
        }

        item.setQuantityInStock(newQuantity);
        inventoryRepository.save(item);

        po.setStatus("DELIVERED");

        deliveryRepository.findByPurchaseOrderPoId(po.getPoId())
                .ifPresent(d -> {
                    d.setDeliveryStatus("DELIVERED");
                    d.setDeliveredAt(java.time.LocalDateTime.now());
                    deliveryRepository.save(d);
                });

        return toResponse(poRepository.save(po));
    }

    // STATE TRANSITION: ANY -> CANCELLED (except DELIVERED)
    @Transactional
    public PurchaseOrderResponse cancelOrder(Integer id) {
        PurchaseOrder po = findOrderById(id);

        if ("DELIVERED".equals(po.getStatus())) {
            throw new ConflictException("Cannot cancel — order already DELIVERED");
        }
        if ("CANCELLED".equals(po.getStatus())) {
            throw new ConflictException("Order is already CANCELLED");
        }

        po.setStatus("CANCELLED");
        return toResponse(poRepository.save(po));
    }

    // DELETE (only PENDING or CANCELLED)
    @Transactional
    public void deleteOrder(Integer id) {
        PurchaseOrder po = findOrderById(id);

        if ("DISPATCHED".equals(po.getStatus()) || "DELIVERED".equals(po.getStatus())) {
            throw new ConflictException("Cannot delete PO in status: " + po.getStatus());
        }

        poRepository.delete(po);
    }

    // HELPERS
    private PurchaseOrder findOrderById(Integer id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found: " + id));
    }

    private String generatePoNumber() {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "PO-" + year + "-";

        Optional<String> latest = poRepository.findLatestPoNumberWithPrefix(prefix);
        int nextNumber = 1;

        if (latest.isPresent()) {
            String latestNumber = latest.get();
            String suffix = latestNumber.substring(latestNumber.lastIndexOf('-') + 1);
            nextNumber = Integer.parseInt(suffix) + 1;
        }

        return String.format("%s%05d", prefix, nextNumber);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getPoId(),
                po.getPoNumber(),
                po.getItem().getItemId(),
                po.getItem().getSku(),
                po.getItem().getItemName(),
                po.getQuantity(),
                po.getStatus(),
                po.getCreatedByUser().getUserId(),
                po.getCreatedByUser().getFullName(),
                po.getAssignedSupplier().getUserId(),
                po.getAssignedSupplier().getFullName(),
                po.getAssignedDriver() != null ? po.getAssignedDriver().getUserId() : null,
                po.getAssignedDriver() != null ? po.getAssignedDriver().getFullName() : null,
                po.getCreatedAt()
        );
    }
}