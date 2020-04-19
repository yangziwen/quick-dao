package io.github.yangziwen.quickdao.core;

import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionStmt<E> extends Stmt {

    private final InvokedMethodExtractor<E> extractor;

    private final SqlFunctionExpression<E> expression;

    public FunctionStmt(SqlFunctionExpression<E> expression, InvokedMethodExtractor<E> extractor) {
        this.expression = expression;
        this.extractor = extractor;
    }

}
