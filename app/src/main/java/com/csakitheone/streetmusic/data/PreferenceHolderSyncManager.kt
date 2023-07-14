package com.csakitheone.streetmusic.data

class PreferenceHolderSyncManager {
    companion object {

        private var onChangedListeners = mutableListOf<(id: String) -> Unit>()

        fun addOnChangedListener(listener: (id: String) -> Unit) {
            onChangedListeners.add(listener)
        }

        fun removeOnChangedListener(listener: (id: String) -> Unit) {
            onChangedListeners.remove(listener)
        }

        fun onChanged(id: String) {
            onChangedListeners.forEach { it(id) }
        }

    }
}