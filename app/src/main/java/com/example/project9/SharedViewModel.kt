package com.example.project9


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData

/**
 * ViewModel shared across multiple fragments, handling business logic and data storage
 * @param application The application this ViewModel is associated with
 */
class SharedViewModel(application: Application) : AndroidViewModel(application)
{

    private val accountService: AccountService = FirebaseAccountService()
    private val storageService: StorageService = FirebaseStorageService()

    private val _authenticationState = MutableLiveData<AuthenticationState>()
    val authenticationState: LiveData<AuthenticationState>
        get() = _authenticationState

    private val _selectedImageUri = MutableLiveData<Uri>()
    val selectedImageUri: LiveData<Uri> = _selectedImageUri


    /**
     * Setting up authentication state for the user
     */
    init {
        viewModelScope.launch {
            accountService.currentUser.collect { user ->
                if (user != null)
                {
                    _authenticationState.value = AuthenticationState.AUTHENTICATED
                    user.id?.let { userId -> viewModelScope.launch {} }

                }
                else {
                    _authenticationState.value = AuthenticationState.UNAUTHENTICATED
                }
            }
        }
    }

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    /**
     * Attempts to sign in the user
     * @param email The user's email
     * @param password The user's password
     */
    fun signIn(email: String, password: String)
    {
        viewModelScope.launch {
            try
            {
                accountService.signIn(email, password)
            }
            catch (e: FirebaseAuthException)
            {
                _authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                _errorMessage.value = handleFirebaseAuthException(e)
            }
            catch (e: Exception)
            {
                _errorMessage.value = "An unexpected error occurred. Please try again later."
                Log.e("SharedViewModel", "Sign In Error: ", e)
            }
        }
    }

    /**
     * Selects an image URI for further operations
     * @param uri The URI of the selected image
     */
    fun selectImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }


    /**
     * Attempts to sign up the user
     * @param email The user's email
     * @param password The user's password
     */
    fun signUp(email: String, password: String)
    {
        viewModelScope.launch {
            try
            {
                accountService.signUp(email, password)
            }
            catch (e: FirebaseAuthException)
            {
                _authenticationState.value = AuthenticationState.INVALID_AUTHENTICATION
                _errorMessage.value = handleFirebaseAuthException(e)
            }
            catch (e: Exception)
            {
                _errorMessage.value = "An unexpected error occurred. Please try again later."
                Log.e("SharedViewModel", "Sign Up Error: ", e)
            }
        }
    }

    /**
     * Handles exceptions from Firebase authentication
     * @param e The FirebaseAuthException to handle
     * @return A string message describing the error
     */
    private fun handleFirebaseAuthException(e: FirebaseAuthException): String
    {
        return when (e.errorCode)
        {
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_USER_DISABLED" -> "The user account has been disabled."
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD" -> "Invalid email or password."
            else -> "An unknown error occurred. Please try again."
        }
    }

    /**
     * Signs the user out
     */
    fun signOut()
    {
        viewModelScope.launch {
            accountService.signOut()
            _authenticationState.value = AuthenticationState.UNAUTHENTICATED
        }
    }

    /**
     * Fetches a live data list of image URIs from storage service
     */
    val imagesFlow: LiveData<List<Uri>> = liveData {emitSource(storageService.fetchImageUris().asLiveData())}

    /**
     * Uploads an image to storage and executes a callback upon completion
     * @param imageUri The URI of the image to upload
     * @param onComplete A callback function to be invoked when the upload operation is complete Returns the download URI if successful, null otherwise
     */
    fun uploadImage(imageUri: Uri, onComplete: (Uri?) -> Unit) {storageService.uploadImage(imageUri, onComplete)}
}
/**
 * Enum representing the possible authentication states of a user
 */
enum class AuthenticationState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}