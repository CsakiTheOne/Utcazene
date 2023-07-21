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
            geoLink = "47.094403,17.9067059".latLngToGeo(),
        )
        val MOL = Place(
            name = "MOL Nagyon Balaton nagyszínpad (a Hangvilla melletti téren)",
            geoLink = "47.0921502,17.9081989".latLngToGeo(),
        )
        val CEMIX = Place(
            name = "Cemix színpad (a Fortuna udvarban)",
            geoLink = "47.0929544,17.9085487".latLngToGeo(),
        )
        val ALGIDA = Place(
            name = "Algida színpad (a Kossuth utca teteje)",
            geoLink = "47.0934186,17.9121363".latLngToGeo(),
        )
        val MAW = Place(
            name = "Man At Work színpad (a Vár Áruház előtt)",
            geoLink = "Veszprém, Cserhát ltp. 6, 8200".addressToGeo(),
        )
        val HARIBO = Place(
            name = "Haribo színpad (a Kossuth utca közepén)",
            geoLink = "47.0931386,17.9104762".latLngToGeo(),
        )
        val MD = Place(
            name = "Meló-diák színpad (a Kölcsey könyvesboltnál)",
            geoLink = "47.0936528,17.9096424".latLngToGeo(),
        )
        val KOSSUTH = Place(
            name = "Kossuth utca - utcazenész megálló",
            geoLink = "47.0927414,17.9089058".latLngToGeo(),
        )
        val GIZELLA = Place(
            name = "Gizella udvar - utcazenész megálló",
            geoLink = "47.0934582,17.9088092".latLngToGeo(),
        )
        val GATE = Place(
            name = "Várkapu - utcazenész megálló",
            geoLink = "47.0951115,17.905739",
        )

    }
}