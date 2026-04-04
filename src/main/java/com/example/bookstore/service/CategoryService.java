package com.example.bookstore.service;

import com.example.bookstore.model.Category;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {

    public List<Category> findAll();

}
