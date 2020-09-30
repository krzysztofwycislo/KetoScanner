package pl.handsome.club.ketoscanner.repository

import java.lang.Exception

data class RepositoryError(
    val message: String,
    val exception: Exception? = null
)
