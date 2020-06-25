package io.github.yangziwen.quickdao.core;

public interface BaseSoftDeletedRepository<E> extends BaseRepository<E> {

    /**
     * 逻辑删除的标识字段（不需要在entity中声明）
     *
     * @return
     */
    String getDeletedFlagColumn();

    /**
     * 已删除数据的逻辑删除标识字段值
     *
     * @return
     */
    Object getDeletedFlagValue();

    /**
     * 未删除数据的逻辑删除标识字段值
     *
     * @return
     */
    Object getNotDeletedFlagValue();

    /**
     * 数据表中的更新时间字段，返回空则逻辑删除时忽略更新时间
     *
     * @return
     */
    String getUpdateTimeColumn();

    /**
     * 数据表中更新时间字段的取值，返回空则逻辑删除时忽略更新时间 只能返回new Date().getTime() 或 "now()"，不能返回Date对象
     *
     * @return
     */
    Object getUpdateTimeValue();

}
