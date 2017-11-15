package tech.ketc.numeri.domain.repository

class OAuthFailureException(override val message: String? = null) : Exception()

class AlreadyExistsException(existence: String, specified: String) : RuntimeException("a $existence with the specified $specified already exists")

class NotExistsException(existence: String, specified: String) : RuntimeException("a $existence with the specified $specified does not exist")