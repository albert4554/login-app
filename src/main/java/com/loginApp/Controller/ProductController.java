package com.loginApp.Controller;

import com.loginApp.dto.ProductDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")  // <-- path base para todos los endpoints de este controlador
public class ProductController {

    @GetMapping
    public List<ProductDto> obtenerProductos() {
        return List.of(
                new ProductDto(1L, "Laptop", 1500.0),
                new ProductDto(2L, "Smartphone", 800.0),
                new ProductDto(3L, "Auriculares", 150.0)
        );
    }
}
