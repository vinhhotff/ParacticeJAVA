package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @EntityGraph(attributePaths = {"category"})
    List<Book> findAll();

    @EntityGraph(attributePaths = {"category"})
    List<Book> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    @EntityGraph(attributePaths = {"category"})
    List<Book> findByAuthor(String author);
}
