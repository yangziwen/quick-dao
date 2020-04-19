package io.github.yangziwen.quickdao.core;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class Stmt {

    private String field;

    private String alias;

    public Stmt() { }

    public Stmt(String field) {
        this.field = field;
    }

    public String getStmtAlias() {
        if (StringUtils.isBlank(alias)) {
            return field;
        }
        return alias;
    }

}
