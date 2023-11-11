package com.example.project9

import kotlinx.coroutines.flow.Flow
/**
 * Service interface for account management such as authentication and account deletion
 */
interface AccountService {
    /**
     * A Flow emitting the current user. It will emit null if there is no authenticated user
     */
    val currentUser: Flow<User?>

    /**
     * Gets the unique identifier of the current user. If no user is authenticated, an empty string is returned
     */
    val currentUserId: String

    /**
     * Checks if there is an authenticated user
     *
     * @return True if a user is authenticated, false otherwise
     */
    fun hasUser(): Boolean

    /**
     * Signs in a user with the provided email and password
     *
     * @param email User's email address
     * @param password User's password
     */
    suspend fun signIn(email: String, password: String)

    /**
     * Signs up a new user with the provided email and password
     *
     * @param email User's email address
     * @param password User's password
     */
    suspend fun signUp(email: String, password: String)

    /**
     * Signs out the currently authenticated user.
     */
    suspend fun signOut()

    /**
     * Deletes the account of the currently authenticated user.
     */
    suspend fun deleteAccount()
}