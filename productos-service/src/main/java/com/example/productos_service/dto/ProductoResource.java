package com.example.productos_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Clase principal para representar un recurso JSON API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResource {

    @JsonProperty("data")
    private ProductData data;

    //Clase Interna para el objeto 'data' de JSON API
    @Data
    @NoArgsConstructor
    public static class ProductData{
        private String id;
        private String type;
        private ProductoAttributes attributes;

        public ProductData(String id, String type, ProductoAttributes attributes){
            this.id = id;
            this.type = type;
            this.attributes = attributes;
        }
    }
}
