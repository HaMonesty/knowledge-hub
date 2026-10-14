package com.knowledgehub.common;

import java.util.List;

/**
 * 通用分页响应。
 *
 * @param records 当前页数据
 * @param total 符合条件的总条数
 * @param page 请求页码
 * @param size 每页条数
 * @param <T> 每条数据的类型
 */
public record PageResult<T>(
        List<T> records,
        long total,
        int page,
        int size
) {
}