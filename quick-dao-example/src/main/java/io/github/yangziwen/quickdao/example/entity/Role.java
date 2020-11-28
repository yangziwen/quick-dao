package io.github.yangziwen.quickdao.example.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role")
public class Role {

    @Id
    @Column
    private Long id;

    @Column
    private String username;

    @Column
    private String roleName;

    @Column
    private Date createTime;

    @Column
    private Date updateTime;

}
