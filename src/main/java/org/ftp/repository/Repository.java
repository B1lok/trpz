package org.ftp.repository;

public interface Repository<T, K> {
  T readById(K id);
  T create(T entity);
  T update(T entity);
  void delete(K id);
}
