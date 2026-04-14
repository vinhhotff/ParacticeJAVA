package com.example.bookstore.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "InvalidatedToken")
public class InvalidatedToken {

    @Id
    String id;
    Date expiryTime;
}
