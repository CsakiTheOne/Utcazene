package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.model.Musician
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Firestore {
    companion object {

        fun getAllMusicians(callback: (List<Musician>) -> Unit) {
            Firebase.firestore.collection("musicians").get()
                .addOnFailureListener { callback(listOf()) }
                .addOnSuccessListener { snapshot ->
                    callback(
                        snapshot.toObjects(Musician::class.java)
                            .sortedBy { it.name }
                    )
                }
        }

        fun addMusicians(musician: Musician) {
            Firebase.firestore.collection("musicians").add(musician)
        }

    }
}