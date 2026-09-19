package com.sekitakumi.nothingfoldlauncher.data

fun appRef(packageName: String): String = "app:$packageName"

fun folderRef(folderId: String): String = "folder:$folderId"

fun reconcileHomeOrder(storedOrder: List<String>, validRefs: List<String>): List<String> {
    val validSet = validRefs.toSet()
    val storedSet = storedOrder.toSet()
    val kept = storedOrder.filter { it in validSet }
    val appended = validRefs.filter { it !in storedSet }
    return kept + appended
}

fun swapHomeOrder(order: List<String>, refA: String, refB: String): List<String> {
    val indexA = order.indexOf(refA)
    val indexB = order.indexOf(refB)
    if (indexA == -1 || indexB == -1) return order
    return order.toMutableList().also {
        it[indexA] = refB
        it[indexB] = refA
    }
}

fun moveHomeOrderToEnd(order: List<String>, ref: String): List<String> {
    if (ref !in order) return order
    return (order - ref) + ref
}
