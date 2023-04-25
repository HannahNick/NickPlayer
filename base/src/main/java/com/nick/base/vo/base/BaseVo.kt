package com.nick.base.vo.base


class BaseVo<T> {
    var code = 0
    var message: String = ""
    var data: T? = null
}