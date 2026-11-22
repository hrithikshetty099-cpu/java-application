package com.example.salesmanagement.repository;

import com.example.salesmanagement.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByNameAsc();
    List<Product> findByStockLessThanEqualOrderByStockAsc(int threshold);
    @Query("select p from Product p where p.stock <= p.lowStockThreshold order by p.stock asc")
    List<Product> findLowStock();
    boolean existsBySkuIgnoreCase(String sku);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
