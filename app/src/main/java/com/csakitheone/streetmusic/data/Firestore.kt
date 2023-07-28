package com.csakitheone.streetmusic.data

import android.util.Log
import com.csakitheone.streetmusic.model.Musician
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Firestore {
    companion object {

        private const val COLLECTION_MUSICIANS = "musicians"

        fun getAllMusicians(callback: (List<Musician>) -> Unit) {
            Firebase.firestore.collection(COLLECTION_MUSICIANS).get()
                .addOnFailureListener { callback(listOf()) }
                .addOnSuccessListener { snapshot ->
                    callback(
                        snapshot.toObjects(Musician::class.java)
                            .sortedBy { it.name }
                    )
                }
        }

        fun addMusician(musician: Musician) {
            Firebase.firestore
                .collection(COLLECTION_MUSICIANS)
                .add(musician)
        }

        fun addMusicians(musicians: Collection<Musician>, callback: (Boolean) -> Unit = {}) {
            val batches = musicians.chunked(10)
            batches.forEach { batch ->
                Firebase.firestore.runTransaction { transaction ->
                    batch.forEach { musician ->
                        val newRef = Firebase.firestore.collection(COLLECTION_MUSICIANS).document()
                        transaction.set(newRef, musician)
                    }
                }.addOnCompleteListener { callback(it.isSuccessful) }
            }
        }

        fun mergeByYear() {
            getAllMusicians { musicians ->
                val groupsByName = musicians.groupBy { it.name.toLowerCase().trim() }
                val allMerged = groupsByName.flatMap {
                    var merged = it.value
                    while (merged.size > 1) {
                        merged = listOf(it.value.first().merge(it.value.last()))
                    }
                    merged
                }.sortedBy { it.name }
                Log.i("Firestore", allMerged.joinToString("\n"))
                Firebase.firestore.collection(COLLECTION_MUSICIANS).get()
                    .addOnSuccessListener { querySnapshot ->
                        Firebase.firestore.runTransaction { transaction ->
                            querySnapshot.forEach { documentSnapshot ->
                                transaction.delete(documentSnapshot.reference)
                            }
                        }
                            .addOnSuccessListener {
                                Firebase.firestore.runTransaction { transaction ->
                                    allMerged.forEach { musician ->
                                        transaction.set(
                                            Firebase.firestore.collection(COLLECTION_MUSICIANS)
                                                .document(musician.name),
                                            musician
                                        )
                                    }
                                }
                            }
                    }
            }
        }

    }
}