package com.sekitakumi.nothingfoldlauncher.data

// Favorite (shown on home) and hidden are treated as one mutually exclusive
// state: turning one on automatically turns the other off.

fun toggleFavorite(favorites: Set<String>, hidden: Set<String>, packageName: String): Pair<Set<String>, Set<String>> {
    val newFavorites = if (packageName in favorites) favorites - packageName else favorites + packageName
    val newHidden = if (packageName in newFavorites) hidden - packageName else hidden
    return newFavorites to newHidden
}

fun toggleHidden(favorites: Set<String>, hidden: Set<String>, packageName: String): Pair<Set<String>, Set<String>> {
    val newHidden = if (packageName in hidden) hidden - packageName else hidden + packageName
    val newFavorites = if (packageName in newHidden) favorites - packageName else favorites
    return newFavorites to newHidden
}
