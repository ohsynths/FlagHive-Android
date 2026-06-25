package com.elang.flaghive.data.firebase

import com.elang.flaghive.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SeedData {

    private val defaultCategories = listOf(
        Category(name = "Web Exploitation", description = "Vulnerabilities and exploits in web applications"),
        Category(name = "Cryptography", description = "Encryption, decryption, and cryptographic challenges"),
        Category(name = "Reverse Engineering", description = "Analyzing and deconstructing binaries and software"),
        Category(name = "Forensics", description = "Digital forensics, file analysis, and investigation"),
        Category(name = "Binary Exploitation", description = "Buffer overflows, ROP, and binary exploitation (Pwn)"),
        Category(name = "Miscellaneous", description = "Various challenges including OSINT, steganography, and more")
    )

    suspend fun seedIfEmpty(firestore: FirebaseFirestore) {
        val snapshot = firestore.collection(FirestoreCollections.CATEGORIES)
            .limit(1)
            .get()
            .await()

        if (snapshot.documents.isNotEmpty()) return

        for (category in defaultCategories) {
            firestore.collection(FirestoreCollections.CATEGORIES)
                .add(category)
                .await()
        }
    }
}
