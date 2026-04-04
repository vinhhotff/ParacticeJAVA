package com.example.bookstore.service.impl;

import com.example.bookstore.model.Category;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private com.example.bookstore.repository.CategoryRepository CategoryRepository;

    @Override
    public List<Category> findAll() {
        return CategoryRepository.findAll();
    }

}
