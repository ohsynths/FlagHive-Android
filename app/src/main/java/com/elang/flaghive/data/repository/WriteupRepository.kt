package com.elang.flaghive.data.repository

import com.elang.flaghive.data.firebase.FirestoreCollections
import com.elang.flaghive.data.model.Writeup
import com.elang.flaghive.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WriteupRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getWriteups(): Resource<List<Writeup>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.WRITEUPS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val writeups = snapshots.documents.map { doc ->
                doc.toObject(Writeup::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(writeups)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get writeups")
        }
    }

    suspend fun getWriteupsByCategory(categoryId: String): Resource<List<Writeup>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.WRITEUPS)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val writeups = snapshots.documents.map { doc ->
                doc.toObject(Writeup::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(writeups)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get writeups by category")
        }
    }

    suspend fun getWriteupsByAuthor(authorId: String): Resource<List<Writeup>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.WRITEUPS)
                .whereEqualTo("authorId", authorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val writeups = snapshots.documents.map { doc ->
                doc.toObject(Writeup::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(writeups)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get writeups by author")
        }
    }

    suspend fun getWriteupById(writeupId: String): Resource<Writeup> {
        return try {
            val doc = firestore.collection(FirestoreCollections.WRITEUPS)
                .document(writeupId)
                .get()
                .await()

            val writeup = doc.toObject(Writeup::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Writeup not found")

            Resource.Success(writeup)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get writeup")
        }
    }

    suspend fun createWriteup(writeup: Writeup): Resource<String> {
        return try {
            val docRef = firestore.collection(FirestoreCollections.WRITEUPS)
                .add(writeup)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create writeup")
        }
    }

    suspend fun updateWriteup(writeupId: String, writeup: Writeup): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.WRITEUPS)
                .document(writeupId)
                .set(writeup.copy(id = writeupId))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update writeup")
        }
    }

    suspend fun deleteWriteup(writeupId: String): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.WRITEUPS)
                .document(writeupId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete writeup")
        }
    }

    suspend fun searchWriteups(query: String): Resource<List<Writeup>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.WRITEUPS)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val writeups = snapshots.documents.map { doc ->
                doc.toObject(Writeup::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(writeups)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search writeups")
        }
    }
}
