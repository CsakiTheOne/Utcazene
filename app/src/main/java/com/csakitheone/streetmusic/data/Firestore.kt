package com.csakitheone.streetmusic.data

import android.util.Log
import com.csakitheone.streetmusic.model.Musician
import com.csakitheone.streetmusic.util.Auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class Firestore {
    companion object {
        private const val COLLECTION_MUSICIANS = "musicians"
        private const val COLLECTION_USERS = "users"
    }

    class Musicians {
        companion object {
            fun export(callback: (String) -> Unit) {
                getAll { musicians ->
                    callback(Gson().toJson(musicians))
                }
            }

            fun getAll(callback: (List<Musician>) -> Unit) {
                Firebase.firestore.collection(COLLECTION_MUSICIANS).get()
                    .addOnFailureListener { callback(listOf()) }
                    .addOnSuccessListener { snapshot ->
                        callback(
                            snapshot.toObjects(Musician::class.java)
                                .sortedBy { it.name }
                        )
                    }
            }

            fun setAll(musicians: List<Musician>, callback: (Boolean) -> Unit) {
                Firebase.firestore.collection(COLLECTION_MUSICIANS).get()
                    .addOnFailureListener { callback(false) }
                    .addOnSuccessListener { querySnapshot ->
                        Firebase.firestore.runTransaction { transaction ->
                            querySnapshot.forEach {
                                transaction.delete(it.reference)
                            }
                            musicians.forEach { musician ->
                                val newRef = Firebase.firestore.collection(COLLECTION_MUSICIANS).document()
                                transaction.set(newRef, musician)
                            }
                        }.addOnCompleteListener { callback(it.isSuccessful) }
                    }
            }

            fun add(musician: Musician) {
                Firebase.firestore
                    .collection(COLLECTION_MUSICIANS)
                    .add(musician)
            }

            fun addAll(musicians: Collection<Musician>, callback: (Boolean) -> Unit = {}) {
                Firebase.firestore.runTransaction { transaction ->
                    musicians.forEach { musician ->
                        val newRef = Firebase.firestore.collection(COLLECTION_MUSICIANS).document()
                        transaction.set(newRef, musician)
                    }
                }.addOnCompleteListener { callback(it.isSuccessful) }
            }

            fun mergeYears(
                callback: () -> Unit = {},
            ) {
                getAll { musicians ->
                    val groupsByName = musicians.groupBy { it.name.lowercase().trim() }
                    val allMerged = groupsByName.flatMap {
                        var merged = it.value
                        while (merged.size > 1) {
                            merged = listOf(it.value.first().merge(it.value.last()))
                        }
                        merged
                    }.sortedBy { it.name }
                    Log.i("Firestore", allMerged.joinToString("\n"))
                    Firebase.firestore.collection(COLLECTION_MUSICIANS).get()
                        .addOnFailureListener { callback() }
                        .addOnSuccessListener { querySnapshot ->
                            Firebase.firestore.runTransaction { transaction ->
                                querySnapshot.forEach { documentSnapshot ->
                                    transaction.delete(documentSnapshot.reference)
                                }
                            }
                                .addOnFailureListener { callback() }
                                .addOnSuccessListener {
                                    Firebase.firestore.runTransaction { transaction ->
                                        allMerged.forEach { musician ->
                                            transaction.set(
                                                Firebase.firestore.collection(COLLECTION_MUSICIANS)
                                                    .document(musician.name),
                                                musician
                                            )
                                        }
                                    }.addOnCompleteListener { callback() }
                                }
                        }
                }
            }
        }
    }

    class Users {
        companion object {
            fun isSelfAdmin(callback: (Boolean) -> Unit) {
                if (Auth.user == null) {
                    callback(false)
                    return
                }
                Firebase.firestore.collection(COLLECTION_USERS).document(Auth.user!!.uid).get()
                    .addOnFailureListener {
                        callback(false)
                    }
                    .addOnSuccessListener {
                        callback(it.getBoolean("isAdmin") == true)
                    }
            }
        }
    }
}