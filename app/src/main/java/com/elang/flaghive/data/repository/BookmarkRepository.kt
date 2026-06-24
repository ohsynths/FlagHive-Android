package com.elang.flaghive.data.repository

import com.elang.flaghive.data.firebase.FirestoreCollections
import com.elang.flaghive.data.model.Bookmark
import com.elang.flaghive.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getBookmarks(userId: String): Resource<List<Bookmark>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.BOOKMARKS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val bookmarks = snapshots.documents.map { doc ->
                doc.toObject(Bookmark::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(bookmarks)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get bookmarks")
        }
    }

    suspend fun addBookmark(userId: String, writeupId: String): Resource<String> {
        return try {
            val bookmark = Bookmark(
                userId = userId,
                writeupId = writeupId
            )

            val docRef = firestore.collection(FirestoreCollections.BOOKMARKS)
                .add(bookmark)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add bookmark")
        }
    }

    suspend fun removeBookmark(bookmarkId: String): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.BOOKMARKS)
                .document(bookmarkId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove bookmark")
        }
    }

    suspend fun isBookmarked(userId: String, writeupId: String): Resource<Boolean> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.BOOKMARKS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("writeupId", writeupId)
                .limit(1)
                .get()
                .await()

            Resource.Success(snapshots.documents.isNotEmpty())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check bookmark")
        }
    }

    suspend fun getBookmarkId(userId: String, writeupId: String): Resource<String> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.BOOKMARKS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("writeupId", writeupId)
                .limit(1)
                .get()
                .await()

            val bookmarkId = snapshots.documents.firstOrNull()?.id
                ?: return Resource.Error("Bookmark not found")

            Resource.Success(bookmarkId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get bookmark")
        }
    }
}
