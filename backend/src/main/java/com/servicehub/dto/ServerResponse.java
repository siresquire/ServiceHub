package com.servicehub.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerResponse <T>{
    private String message;
    private T data;

    public ServerResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public ServerResponse(String message) {
        this(message, null);
    }

}
