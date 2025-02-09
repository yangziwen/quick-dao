package io.github.yangziwen.quickdao.example.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.github.yangziwen.quickdao.core.annotation.NestedKeyword;
import io.github.yangziwen.quickdao.example.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @Column
    @GeneratedValue
    private Object id;

    @Column
    @NestedKeyword
    private String username;

    @Column
    private String email;

    @Column
    @NestedKeyword
    private Gender gender;

    @Column
    @NestedKeyword
    private String city;

    @Column
    private Integer age;

    @Column
    private Date createTime;

    @Column
    private Date updateTime;

    @Transient
    private BigDecimal avgAge;

    @Transient
    private BigDecimal maxAge;

    @Transient
    private BigDecimal minAge;

    @Transient
    private Integer count;

    @Transient
    private Integer distinctCount;

}
