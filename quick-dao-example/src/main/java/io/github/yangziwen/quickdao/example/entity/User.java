package io.github.yangziwen.quickdao.example.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.github.yangziwen.quickdao.example.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class User {

    @Id
    @Column
    private Long id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Gender gender;

    @Column
    private Integer age;

    @Column
    private Date createTime;

    @Column
    private Date updateTime;

}
