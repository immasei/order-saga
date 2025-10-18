package com.example.bank.service;

public interface DtoMapper<E, Req, Res> {
    E toEntity(Req request);
    Res toResponse(E entity);
}