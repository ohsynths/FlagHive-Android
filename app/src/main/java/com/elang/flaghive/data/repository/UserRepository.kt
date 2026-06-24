package com.elang.flaghive.data.repository

import com.elang.flaghive.data.firebase.FirestoreCollections
import com.elang.flaghive.data.model.User
import com.elang.flaghive.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val doc = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()

            val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
                ?: return Resource.Error("User not found")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user profile")
        }
    }

    suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.USERS)
                .get()
                .await()

            val users = snapshots.documents.map { doc ->
                doc.toObject(User::class.java)!!.copy(uid = doc.id)
            }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get users")
        }
    }

    suspend fun deleteUser(uid: String): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete user")
        }
    }
}
