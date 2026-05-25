package com.example.inventory_service;

import com.example.inventory_service.controller.InventoryController;
import com.example.inventory_service.controller.ProductController;
import com.example.inventory_service.entity.CinemaInventory;
import com.example.inventory_service.entity.Product;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.service.ProductService;
import com.example.inventory_service.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryApiContractTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;
    @Mock
    private StockService stockService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductController(productService),
                new InventoryController(stockService)
        ).build();
    }

    @Test
    void productEndpointsRespectContract() throws Exception {
        Product product = product();
        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(productService.getProductById(1L)).thenReturn(product);
        when(productService.getAvailableProductsByCinema(2L)).thenReturn(List.of(product));
        when(productService.createProduct(any())).thenReturn(product);
        when(productService.updateProduct(eq(1L), any())).thenReturn(product);
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Combo"));
        mockMvc.perform(get("/api/products/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(get("/api/products/cinema/2")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Combo\",\"category\":\"COMBO\",\"price\":50000}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Combo\",\"category\":\"COMBO\",\"price\":50000}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/products/1")).andExpect(status().isNoContent());
    }

    @Test
    void inventoryEndpointsRespectContract() throws Exception {
        CinemaInventory inventory = inventory();
        StockMovement movement = new StockMovement();
        movement.setId(3L);
        movement.setMovementType(StockMovement.MovementType.IN);
        movement.setQuantity(10);

        when(stockService.getAllInventory()).thenReturn(List.of(inventory));
        when(stockService.getInventoryByCinema(2L)).thenReturn(List.of(inventory));
        when(stockService.getLowStockInventory()).thenReturn(List.of(inventory));
        when(stockService.createOrUpdateInventory(any())).thenReturn(inventory);
        when(stockService.adjustStock(any())).thenReturn(inventory);
        when(stockService.getAllMovements()).thenReturn(List.of(movement));
        when(stockService.getMovementsByInventory(4L)).thenReturn(List.of(movement));

        mockMvc.perform(get("/api/inventory")).andExpect(status().isOk()).andExpect(jsonPath("$[0].cinemaId").value(2));
        mockMvc.perform(get("/api/inventory/cinema/2")).andExpect(status().isOk());
        mockMvc.perform(get("/api/inventory/low-stock")).andExpect(status().isOk());
        mockMvc.perform(post("/api/inventory").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinemaId\":2,\"productId\":1,\"quantity\":20,\"minStockLevel\":5}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/inventory").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinemaId\":2,\"productId\":1,\"quantity\":20,\"minStockLevel\":5}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/inventory/adjust").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinemaId\":2,\"productId\":1,\"quantity\":5,\"movementType\":\"ADJUSTMENT\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/inventory/restock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinemaId\":2,\"productId\":1,\"quantity\":5}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/inventory/movements")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(3));
        mockMvc.perform(get("/api/inventory/4/movements")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(3));
    }

    private Product product() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Combo");
        product.setCategory(Product.ProductCategory.COMBO);
        product.setPrice(BigDecimal.valueOf(50000));
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setIsAvailable(true);
        return product;
    }

    private CinemaInventory inventory() {
        CinemaInventory inventory = new CinemaInventory();
        inventory.setId(4L);
        inventory.setCinemaId(2L);
        inventory.setProduct(product());
        inventory.setQuantity(20);
        inventory.setMinStockLevel(5);
        return inventory;
    }
}
