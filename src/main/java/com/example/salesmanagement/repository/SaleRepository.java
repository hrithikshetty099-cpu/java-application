package com.example.salesmanagement.repository;

import com.example.salesmanagement.model.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @EntityGraph(attributePaths = {"customer", "items"})
    List<Sale> findTop8ByOrderBySaleDateDesc();
    @EntityGraph(attributePaths = {"customer", "items"})
    @Query("select distinct s from Sale s join s.customer c where " +
           "(:term = '' or lower(s.invoiceNumber) like lower(concat('%', :term, '%')) " +
           "or lower(c.name) like lower(concat('%', :term, '%'))) order by s.saleDate desc")
    List<Sale> search(@Param("term") String term);
    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);
    @EntityGraph(attributePaths = {"customer", "items"})
    List<Sale> findByCustomerIdOrderBySaleDateDesc(Long customerId);
    @Query("select coalesce(sum(s.total), 0) from Sale s where s.saleDate >= :from and s.saleDate < :to")
    BigDecimal totalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select coalesce(sum(s.total), 0) from Sale s")
    BigDecimal totalAll();
    @Query("select s from Sale s join fetch s.customer where s.saleDate >= :from and s.saleDate < :to order by s.saleDate desc")
    List<Sale> findBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
