package org.reactive.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"customerId", "productId"}))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", referencedColumnName = "id")
    @JsonBackReference
    private Customer customer;

    @Column
    private Long productId;

    @Transient
    private String code;

    @Transient
    private String name;

    @Transient
    private String description;

}
