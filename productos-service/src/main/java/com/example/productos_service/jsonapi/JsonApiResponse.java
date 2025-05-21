package com.example.productos_service.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonApiResponse<T> {

    @JsonProperty("data")
    private T data; // Puede ser un JsonApiData<Attributes> o List<JsonApiData<Attributes>>

    // Constructor para una sola entidad
    public JsonApiResponse(JsonApiData<T> data) {
        this.data = (T) data; // Esto es un cast genérico, se manejará en el uso
    }

    // Constructor para una lista de entidades
    public JsonApiResponse(List<JsonApiData<T>> dataList) {
        this.data = (T) dataList; // Esto es un cast genérico, se manejará en el uso
    }
}
