package com.nguyenduc.repository;

import java.util.List;

public interface IGeneralRepository<T> {
    List<T> findAll();

    List<T> findByName(String name);

    T findById(Long id);

    void remove(Long id);

    void save(T t);
}
