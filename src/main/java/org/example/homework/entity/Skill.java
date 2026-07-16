package org.example.homework.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Builder.Default
    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    private List<Employee> employees = new ArrayList<>();
}
