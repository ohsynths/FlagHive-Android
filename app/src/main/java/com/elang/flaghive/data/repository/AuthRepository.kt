package com.elang.flaghive.data.repository

import com.elang.flaghive.data.firebase.FirestoreCollections
import com.elang.flaghive.data.model.User
import com.elang.flaghive.data.model.UserRole
import com.elang.flaghive.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Login failed")
            getUserData(uid)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("Registration failed")

            val user = User(
                uid = uid,
                email = email,
                displayName = displayName,
                role = UserRole.USER
            )

            firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .set(user)
                .await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    fun logout() {
        auth.signOut()
    }

    private suspend fun getUserData(uid: String): Resource<User> {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)?.copy(uid = uid)
                ?: return Resource.Error("User not found")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user data")
        }
    }
}
