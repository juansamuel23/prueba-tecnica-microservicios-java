package com.example.productos_service.service.impl;

import com.example.productos_service.model.Producto;
import com.example.productos_service.repository.ProductoRepository;
import com.example.productos_service.service.ProductoService;
import com.example.productos_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public Producto obtenerProducto(String id) {
        return productoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    };

    @Override
    public List<Producto> listarProductos(Pageable pageable) {
        Page<Producto> page = productoRepository.findAll(pageable);
        return page.getContent();
    }

    @Override
    public Producto actualizarProducto(String id, Producto producto) {
        Producto existingProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        existingProducto.setNombre(producto.getNombre());
        existingProducto.setPrecio(producto.getPrecio());
        return productoRepository.save(existingProducto);
    }

    @Override
    public void eliminarProducto(String id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        productoRepository.delete(producto);
    }
}
