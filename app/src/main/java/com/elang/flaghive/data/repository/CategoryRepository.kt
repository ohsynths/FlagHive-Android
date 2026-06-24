package com.elang.flaghive.data.repository

import com.elang.flaghive.data.firebase.FirestoreCollections
import com.elang.flaghive.data.model.Category
import com.elang.flaghive.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.CATEGORIES)
                .orderBy("name")
                .get()
                .await()

            val categories = snapshots.documents.map { doc ->
                doc.toObject(Category::class.java)!!.copy(id = doc.id)
            }
            Resource.Success(categories)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get categories")
        }
    }

    suspend fun getCategoryById(categoryId: String): Resource<Category> {
        return try {
            val doc = firestore.collection(FirestoreCollections.CATEGORIES)
                .document(categoryId)
                .get()
                .await()

            val category = doc.toObject(Category::class.java)?.copy(id = doc.id)
                ?: return Resource.Error("Category not found")

            Resource.Success(category)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get category")
        }
    }

    suspend fun createCategory(category: Category): Resource<String> {
        return try {
            val docRef = firestore.collection(FirestoreCollections.CATEGORIES)
                .add(category)
                .await()

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create category")
        }
    }

    suspend fun updateCategory(categoryId: String, category: Category): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.CATEGORIES)
                .document(categoryId)
                .set(category.copy(id = categoryId))
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update category")
        }
    }

    suspend fun deleteCategory(categoryId: String): Resource<Unit> {
        return try {
            firestore.collection(FirestoreCollections.CATEGORIES)
                .document(categoryId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete category")
        }
    }
}
