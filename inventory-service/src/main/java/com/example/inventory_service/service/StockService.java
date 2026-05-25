package com.example.inventory_service.service;

import com.example.inventory_service.entity.BookingProduct;
import com.example.inventory_service.entity.CinemaInventory;
import com.example.inventory_service.entity.Product;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.dto.InventoryRequest;
import com.example.inventory_service.dto.StockAdjustmentRequest;
import com.example.inventory_service.event.BookingEvent;
import com.example.inventory_service.repository.BookingProductRepository;
import com.example.inventory_service.repository.CinemaInventoryRepository;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private static final String REFERENCE_TYPE_BOOKING = "BOOKING";

    public List<CinemaInventory> getAllInventory() {
        return cinemaInventoryRepository.findAll();
    }

    public List<CinemaInventory> getInventoryByCinema(Long cinemaId) {
        return cinemaInventoryRepository.findByCinemaId(cinemaId);
    }

    public List<CinemaInventory> getLowStockInventory() {
        return cinemaInventoryRepository.findLowStock();
    }

    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAll();
    }

    public List<StockMovement> getMovementsByInventory(Long inventoryId) {
        return stockMovementRepository.findByCinemaInventoryIdOrderByCreatedAtDesc(inventoryId);
    }

    @Transactional
    public CinemaInventory createOrUpdateInventory(InventoryRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));

        CinemaInventory inventory = cinemaInventoryRepository
                .findByCinemaIdAndProductIdForUpdate(request.getCinemaId(), request.getProductId())
                .orElseGet(CinemaInventory::new);

        inventory.setCinemaId(request.getCinemaId());
        inventory.setProduct(product);
        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        if (request.getMinStockLevel() != null) {
            inventory.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            inventory.setMaxStockLevel(request.getMaxStockLevel());
        }
        return cinemaInventoryRepository.save(inventory);
    }

    @Transactional
    public CinemaInventory adjustStock(StockAdjustmentRequest request) {
        CinemaInventory inventory = cinemaInventoryRepository
                .findByCinemaIdAndProductIdForUpdate(request.getCinemaId(), request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory not found for cinema "
                        + request.getCinemaId() + " and product " + request.getProductId()));

        StockMovement.MovementType movementType = request.getMovementType() != null
                ? request.getMovementType()
                : StockMovement.MovementType.ADJUSTMENT;
        int quantity = request.getQuantity() != null ? request.getQuantity() : 0;

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        if (movementType == StockMovement.MovementType.IN || movementType == StockMovement.MovementType.RETURN) {
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.setLastRestockedAt(LocalDateTime.now());
        } else if (movementType == StockMovement.MovementType.OUT) {
            if (inventory.getQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
        } else {
            inventory.setQuantity(quantity);
        }

        CinemaInventory saved = cinemaInventoryRepository.save(inventory);
        StockMovement movement = buildStockMovement(saved, movementType, quantity, null,
                request.getNotes() != null ? request.getNotes() : "Manual stock adjustment");
        movement.setPerformedBy(request.getPerformedBy());
        stockMovementRepository.save(movement);
        return saved;
    }

    private final CinemaInventoryRepository cinemaInventoryRepository;
    private final ProductRepository productRepository;
    private final BookingProductRepository bookingProductRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public void deductStockForConfirmedBooking(BookingEvent event) {
        List<BookingEvent.ProductItem> productItems = event.getProductItems();
        if (productItems == null || productItems.isEmpty()) {
            log.info("Booking {} has no products. Skipping stock deduction.", event.getBookingReference());
            return;
        }

        if (bookingProductRepository.existsByBookingId(event.getBookingId())) {
            log.info("Stock was already deducted for booking {}. Skipping duplicate event.", event.getBookingReference());
            return;
        }

        for (BookingEvent.ProductItem item : productItems) {
            validateProductItem(item);

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            CinemaInventory inventory = cinemaInventoryRepository
                    .findByCinemaIdAndProductIdForUpdate(event.getCinemaId(), item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for cinema "
                            + event.getCinemaId() + " and product " + item.getProductId()));

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product " + item.getProductId()
                        + ". Available: " + inventory.getQuantity()
                        + ", requested: " + item.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            cinemaInventoryRepository.save(inventory);

            bookingProductRepository.save(buildBookingProduct(event, item, product));
            stockMovementRepository.save(buildStockMovement(
                    inventory,
                    StockMovement.MovementType.OUT,
                    item.getQuantity(),
                    event.getBookingId(),
                    "Stock deducted for booking " + event.getBookingReference()));
        }
    }

    @Transactional
    public void restoreStockForCancelledBooking(BookingEvent event) {
        List<BookingProduct> bookingProducts = bookingProductRepository.findByBookingId(event.getBookingId());
        if (bookingProducts.isEmpty()) {
            log.info("No deducted stock found for booking {}. Skipping stock restore.", event.getBookingReference());
            return;
        }

        for (BookingProduct bookingProduct : bookingProducts) {
            CinemaInventory inventory = cinemaInventoryRepository
                    .findByCinemaIdAndProductIdForUpdate(event.getCinemaId(), bookingProduct.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for cinema "
                            + event.getCinemaId() + " and product " + bookingProduct.getProduct().getId()));

            inventory.setQuantity(inventory.getQuantity() + bookingProduct.getQuantity());
            cinemaInventoryRepository.save(inventory);

            stockMovementRepository.save(buildStockMovement(
                    inventory,
                    StockMovement.MovementType.RETURN,
                    bookingProduct.getQuantity(),
                    event.getBookingId(),
                    "Stock restored for cancelled booking " + event.getBookingReference()));
        }

        bookingProductRepository.deleteByBookingId(event.getBookingId());
    }

    private void validateProductItem(BookingEvent.ProductItem item) {
        if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new RuntimeException("Invalid product item in booking event");
        }
    }

    private BookingProduct buildBookingProduct(BookingEvent event, BookingEvent.ProductItem item, Product product) {
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : product.getPrice();

        BookingProduct bookingProduct = new BookingProduct();
        bookingProduct.setBookingId(event.getBookingId());
        bookingProduct.setProduct(product);
        bookingProduct.setQuantity(item.getQuantity());
        bookingProduct.setUnitPrice(unitPrice);
        bookingProduct.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        return bookingProduct;
    }

    private StockMovement buildStockMovement(
            CinemaInventory inventory,
            StockMovement.MovementType movementType,
            Integer quantity,
            Long bookingId,
            String notes) {
        StockMovement movement = new StockMovement();
        movement.setCinemaInventory(inventory);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setReferenceType(REFERENCE_TYPE_BOOKING);
        movement.setReferenceId(bookingId);
        movement.setNotes(notes);
        return movement;
    }
}
