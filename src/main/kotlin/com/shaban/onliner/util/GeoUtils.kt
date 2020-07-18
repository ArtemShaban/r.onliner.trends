package com.shaban.onliner.util

import org.locationtech.spatial4j.context.SpatialContext
import org.locationtech.spatial4j.shape.Rectangle

fun Rectangle.splitByX(): Pair<Rectangle, Rectangle> {
    val halfWidth = this.width / 2
    val left = SpatialContext.GEO.shapeFactory.rect(this.minX, this.minX + halfWidth, this.minY, this.maxY)
    val right = SpatialContext.GEO.shapeFactory.rect(left.maxX, this.maxX, this.minY, this.maxY)
    return left to right
}

fun Rectangle.splitByY(): Pair<Rectangle, Rectangle> {
    val halfHeight = this.height / 2
    val bottom = SpatialContext.GEO.shapeFactory.rect(this.minX, this.maxX, this.minY, this.minY + halfHeight)
    val top = SpatialContext.GEO.shapeFactory.rect(this.minX, this.maxX, bottom.maxY, this.maxY)
    return bottom to top
}