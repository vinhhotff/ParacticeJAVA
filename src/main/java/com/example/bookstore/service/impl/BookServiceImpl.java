package com.example.bookstore.service.impl;

import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.exception.AppException;
import com.example.bookstore.exception.ErrorCode;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {


    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream().map(bookMapper::toBookResponse).toList();
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        return bookMapper.toBookResponse(book);
    }

    @Override
    public BookResponse saveBook(BookRequest request) {
        Book book = bookMapper.toBook(request);
        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Dùng mapper để cập nhật dữ liệu từ request vào existingBook
        // (Hoặc set thủ công nếu chưa cấu hình update mapping)
        existingBook.setTitle(request.getTitle());
        existingBook.setAuthor(request.getAuthor());

        return bookMapper.toBookResponse(bookRepository.save(existingBook));
    }

    @Override
    public void deleteBook(Long id) {
        if(!bookRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOOK_NOT_EXISTED);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public BookResponse addBooktoCategory(Long categoryId, BookRequest request) {
        return categoryRepository.findById(categoryId).map(category -> {
            Book book = bookMapper.toBook(request);
            book.setCategory(category);
            return bookMapper.toBookResponse(bookRepository.save(book));
        }).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<BookResponse> getBooksByCategory(Long categoryId) {
        if(!categoryRepository.existsById(categoryId)){
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND); // Nên dùng lỗi Category
        }
        List<Book> books = bookRepository.findByCategoryId(categoryId);
        // Dùng máy biến hình (Mapper) để chuyển sang DTO
        return books.stream().map(bookMapper::toBookResponse).toList();
    }

    @Override
    public List<BookResponse> getBooksByTitle(String title) {
        List<Book> books;
        if (title != null && !title.isEmpty()) {
            books = bookRepository.findByTitleContainingIgnoreCase(title);
        } else {
            books = bookRepository.findAll();
        }
        // Chuyển List<Book> -> List<BookResponse>
        return books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }
    @Override
    public Page<BookResponse> getAllBooksPaged(Pageable pageable) {
        Page<Book> bookPage = bookRepository.findAll(pageable);

        // Dùng hàm map của Page để chuyển Entity sang DTO
        return bookPage.map(bookMapper::toBookResponse);
    }


}
