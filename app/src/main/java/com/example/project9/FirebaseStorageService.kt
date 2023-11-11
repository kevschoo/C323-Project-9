package com.example.project9

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseStorageService : StorageService
{

    private val storageInstance = Firebase.storage("gs://c323-projects.appspot.com")
    private val storageReference = storageInstance.reference
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Uploads an image to Firebase Storage and stores its URL in Firestore under the user's document
     *
     * @param imageUri The URI of the image to be uploaded
     * @param onComplete A callback function invoked upon completion of the upload process. It receives the download URI of the uploaded image or null in case of failure
     */
    override fun uploadImage(imageUri: Uri, onComplete: (Uri?) -> Unit)
    {
        val fileRef = storageReference.child("images/${userId}/${imageUri.lastPathSegment}")
        val uploadTask = fileRef.putFile(imageUri)

        uploadTask.addOnSuccessListener{
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val imageInfo = mapOf("url" to uri.toString())
                firestore.collection("users").document(userId.orEmpty())
                    .collection("images").add(imageInfo)

                    .addOnSuccessListener {onComplete(uri)}

                    .addOnFailureListener {
                        Log.e("FirebaseStorageService", "Failed to store image URL in Firestore", it)
                        onComplete(null)
                    }
            }
        }
            .addOnFailureListener { exception ->
            Log.e("FirebaseStorageService", "Image upload failed", exception)
            onComplete(null)
        }
    }

    /**
     * Retrieves a flow of image URIs from Firestore. It listens to the collection of images under the current user's document
     *
     * @return A flow emitting lists of URIs as they are updated in the Firestore collection
     */
    override fun fetchImageUris(): Flow<List<Uri>> = callbackFlow {
        val collectionRef = firestore.collection("users").document(userId.orEmpty()).collection("images")
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null)
            {
                Log.e("GalleryFragment", "Error fetching images", e)
                close(e)
                return@addSnapshotListener
            }

            val uris = snapshot?.documents?.mapNotNull { it.getString("url")?.toUri() }.orEmpty()
            Log.d("GalleryFragment", "Fetched URIs: $uris")
            trySend(uris).isSuccess
        }

        awaitClose { listenerRegistration.remove() }
    }

}