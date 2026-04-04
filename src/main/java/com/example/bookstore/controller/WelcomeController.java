package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiResponse;
import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.model.Book;
import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class WelcomeController {
    @Autowired
    private BookService bookService;

    @GetMapping
    public ApiResponse<List<BookResponse>> getBooks() {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getAllBooks())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'USER')")
    public ApiResponse<BookResponse> getBook(@PathVariable long id) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.getBookById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<BookResponse> addBook(@Valid @RequestBody BookRequest bookRequest) {
        return ApiResponse.<BookResponse>builder()
                .message("Tạo sách thành công")
                .result(bookService.saveBook(bookRequest))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable long id, @RequestBody BookRequest bookRequest) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.updateBook(id, bookRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteBook(@PathVariable long id) {
        bookService.deleteBook(id);
        return ApiResponse.<Void>builder()
                .message("Xóa thành công")
                .build();
    }

    // FIX 1: Bọc ApiResponse cho API thêm sách vào Category
    @PostMapping("/categories/{categoryId}/books")
    public ApiResponse<BookResponse> addBookToCategory(@PathVariable Long categoryId, @RequestBody BookRequest bookRequest) {
        return ApiResponse.<BookResponse>builder()
                .message("Đã thêm sách vào thể loại")
                .result(bookService.addBooktoCategory(categoryId, bookRequest))
                .build();
    }

    // FIX 2: Sửa tên path {categoryid} thành {categoryId} cho đồng nhất và bọc ApiResponse
    @GetMapping("/categories/{categoryId}/books")
    public ApiResponse<List<BookResponse>> getBooksByCategory(@PathVariable Long categoryId) {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getBooksByCategory(categoryId))
                .build();
    }

    // FIX 3: Bọc ApiResponse cho API tìm kiếm
    @GetMapping("/search")
    public ApiResponse<List<BookResponse>> getBooksByTitle(@RequestParam String title) {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getBooksByTitle(title))
                .build();
    }

    @GetMapping("/paged")
    public ApiResponse<Page<BookResponse>> getBooksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return ApiResponse.<Page<BookResponse>>builder()
                .result(bookService.getAllBooksPaged(pageable))
                .build();
    }
}