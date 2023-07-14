package com.csakitheone.streetmusic.model

import androidx.annotation.Keep

@Keep
data class Place(
    val name: String,
    val geoLink: String? = null,
) {
    companion object {

        private fun String.addressToGeo(): String = "geo:0,0?q=$this"
        private fun String.latLngToGeo(): String = "geo:0,0?q=$this"

        fun getValues(): List<Place> =
            listOf(VEB, MOL, CEMIX, ALGIDA, MAW, HARIBO, MD, KOSSUTH, GIZELLA, GATE)

        val MISSING = Place(
            name = "Nem található",
        )

        val VEB = Place(
            name = "VEB 2023 nagyszínpad (az Óváros téren)",
        )
        val MOL = Place(
            name = "MOL Nagyon Balaton színpad (a Hangvilla melletti téren)",
        )
        val CEMIX = Place(
            name = "Cemix színpad (a Fortuna udvarban)",
        )
        val ALGIDA = Place(
            name = "Algida színpad (a Kossuth utca teteje)",
        )
        val MAW = Place(
            name = "Man At Work színpad (a Vár Áruház előtt)",
            geoLink = "Veszprém, Cserhát ltp. 6, 8200".addressToGeo(),
        )
        val HARIBO = Place(
            name = "Haribo színpad (a Kossuth utca közepén)",
        )
        val MD = Place(
            name = "Meló-diák színpad (a Kölcsey könyvesboltnál)",
            geoLink = "47.0936528,17.9096424".latLngToGeo(),
        )
        val KOSSUTH = Place(
            name = "Kossuth utca - utcazenész megálló",
        )
        val GIZELLA = Place(
            name = "Gizella udvar - utcazenész megálló",
        )
        val GATE = Place(
            name = "Várkapu - utcazenész megálló",
        )

    }
}