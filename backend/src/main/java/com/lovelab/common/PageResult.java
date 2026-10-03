package com.lovelab.common;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long total) {
    public static void validate(int page, int size) {
        if (page < 0 || page > 10000 || size < 1 || size > 50) throw ApiException.bad("page须为0至10000，size须为1至50");
    }
}
