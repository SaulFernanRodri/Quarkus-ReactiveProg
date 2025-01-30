package org.reactive.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String code;
    public String name;
    public String description;
}
