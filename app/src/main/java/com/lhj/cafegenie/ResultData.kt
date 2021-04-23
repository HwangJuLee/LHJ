package com.lhj.cafegenie

data class ResultData(
    var lastBuildDate: String = "",
    var total: Int = 0,
    var start: Int = 0,
    var display: Int = 0,
    var category: String = "",
    var items: List<Items>
)

data class Items(
    var title: String = "",
    var originallink: String = "",
    var link: String = "",
    var description: String = "",
    var roadAddress: String = "",
    var telephone: String = "",
    var mapx: Int = 0,
    var mapy: Int = 0
)