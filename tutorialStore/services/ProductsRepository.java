package com.atserc.tutorialStore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.atserc.tutorialStore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer>{

}
