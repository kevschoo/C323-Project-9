package com.example.project9

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAccountService : AccountService
{

    private val auth: FirebaseAuth = Firebase.auth

    /**
     * A Flow emitting the current user. It will emit null if there is no authenticated user
     */
    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.let { User(it.uid) }).isSuccess}
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    /**
     * Gets the unique identifier of the current user. If no user is authenticated, an empty string is returned
     */
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    /**
     * Checks if there is an authenticated user
     *
     * @return True if a user is authenticated, false otherwise
     */
    override fun hasUser(): Boolean {return auth.currentUser != null }

    /**
     * Signs in a user with the provided email and password
     *
     * @param email User's email address
     * @param password User's password
     */
    override suspend fun signIn(email: String, password: String) { auth.signInWithEmailAndPassword(email, password).await() }

    /**
     * Signs up a new user with the provided email and password
     *
     * @param email User's email address
     * @param password User's password
     */
    override suspend fun signUp(email: String, password: String) { auth.createUserWithEmailAndPassword(email, password).await() }


    /**
     * Signs out the currently authenticated user
     */
    override suspend fun signOut() { auth.signOut() }

    /**
     * Deletes the account of the currently authenticated user
     */
    override suspend fun deleteAccount() { auth.currentUser?.delete()?.await() }
}
