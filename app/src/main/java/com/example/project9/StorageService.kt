package com.example.project9

import android.net.Uri
import kotlinx.coroutines.flow.Flow
interface StorageService
{
    fun uploadImage(imageUri: Uri, onComplete: (Uri?) -> Unit)
    fun fetchImageUris(): Flow<List<Uri>>
}