package com.csakitheone.streetmusic.util

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.csakitheone.streetmusic.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Auth {
    companion object {

        private val REQUEST_CODE = 80

        val isSignedIn: Boolean
            get() = Firebase.auth.currentUser != null

        var isSignedInState by mutableStateOf(isSignedIn)
            private set

        fun signInBegin(activity: Activity) {
            val oneTapClient = Identity.getSignInClient(activity)
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(activity.getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .setAutoSelectEnabled(false)
                .build()
            oneTapClient.beginSignIn(signInRequest)
                .addOnFailureListener {
                    Toast.makeText(activity, "Sign in failed", Toast.LENGTH_SHORT).show()
                    Log.e("Auth", it.message ?: "")
                }
                .addOnSuccessListener {
                    activity.startIntentSenderForResult(
                        it.pendingIntent.intentSender,
                        REQUEST_CODE,
                        null,
                        0,
                        0,
                        0,
                    )
                }
        }

        fun onActivityResult(activity: Activity, requestCode: Int, data: Intent?, callback: () -> Unit = {}) {
            if (requestCode != REQUEST_CODE) return

            val oneTapClient = Identity.getSignInClient(activity)
            val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = googleCredential.googleIdToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity) { task ->
                    callback()
                    isSignedInState = isSignedIn
                }
        }

        fun signOut() {
            Firebase.auth.signOut()
            isSignedInState = isSignedIn
        }

    }
}