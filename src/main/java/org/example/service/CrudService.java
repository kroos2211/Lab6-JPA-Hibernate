package org.example.service;

import org.example.dao.GenericDAO;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CrudService<T, ID extends Serializable> {
    private final GenericDAO<T, ID> dao;
    public CrudService(GenericDAO<T, ID> dao) { this.dao = Objects.requireNonNull(dao); }
    public T getById(ID id) { return dao.find(Objects.requireNonNull(id, "id")); }
    public List<T> getAll() { return dao.findAll(); }
    public T create(T entity) { return dao.create(Objects.requireNonNull(entity, "entity")); }
    public T update(T entity) { return dao.update(Objects.requireNonNull(entity, "entity")); }
    public void delete(T entity) { dao.delete(Objects.requireNonNull(entity, "entity")); }
}
