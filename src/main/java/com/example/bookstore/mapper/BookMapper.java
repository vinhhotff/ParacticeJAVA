package com.example.bookstore.mapper;

import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toBook(BookRequest request);

    @Mapping(source = "category.name" , target = "categoryName")
    BookResponse toBookResponse(Book book);
}
