package com.csakitheone.streetmusic.data

import com.csakitheone.streetmusic.model.Musician

class TeremEventProvider {
    companion object {

        const val description = "Negyedik éve kerül megrendezésre az Unlock Fest a Teremben, " +
                "underground zenekarokat felsorakoztató négy napos fesztivál. A színes zenei " +
                "paletta idén sem marad el, a hazai színtér mellett olasz, német és cseh " +
                "zenekarokat is elcsíphetsz, a koncertek után tánczenei mulatság kifulladásig!"

        val musicians = mapOf(
            19 to listOf(
                Musician(
                    name = "Monoton//Monokrom",
                    description = "Zajzene Kádártáról\n" +
                            "https://monotonmonokrom.bandcamp.com/track/f-zf-i-b-l",
                    country = "HU",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Bandit",
                    description = "Veszprémi country",
                    country = "HU",
                    youtubeUrl = "https://youtu.be/G8EVGzI4zwY",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Szolnoki Lajos akusztik",
                    description = "Aranytorkú angyali hangok",
                    country = "HU",
                    youtubeUrl = "https://youtu.be/OIPMeF_3dYU",
                    tags = listOf(Musician.TAG_TEREM),
                )
            ),
            20 to listOf(
                Musician(
                    name = "Furunculus",
                    description = "Disszonáns hangok Győrből",
                    country = "HU",
                    youtubeUrl = "https://youtu.be/U4uJlnngCQM",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "HangmanJack",
                    description = "Groove metál Zirc\n" +
                            "https://www.facebook.com/hangmanjackmusic",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/332127416_3532473983640908_3036307963015805040_n.jpg?_nc_cat=109&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=d4dpSZQ8PnIAX_85RAV&_nc_ht=scontent.fbud6-3.fna&oh=00_AfAnW0MIicyLHVJzyUr0CZ54JyM2FF2AWz_zYgPBhkqrMw&oe=64BADCB1",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Escalate",
                    description = "Vegan sxe hardcore Szhely-Veszprém-Dunaharaszti tengely\n" +
                            "https://www.facebook.com/escalatexvx\n" +
                            "https://escalatexvx.bandcamp.com/",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-4.fna.fbcdn.net/v/t39.30808-6/324625146_753451276304485_5157741398887060313_n.jpg?_nc_cat=111&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=b6MVxzVuwW0AX_OzbyC&_nc_ht=scontent.fbud6-4.fna&oh=00_AfAjsx809LM_vXon0qJSZcFnEdibr2eyS1I31xaeDRXp9w&oe=64BB6D22",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Erased",
                    description = "Modern metál Győr\n" +
                            "https://www.facebook.com/erasedhu",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/324243239_729023742071525_2993557980063174680_n.jpg?_nc_cat=100&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=6mHW3LQEqwsAX9hVJRn&_nc_ht=scontent.fbud6-3.fna&oh=00_AfCAaLVs_P7nYX2guxyd1nR-k6T--0-EYj9vXpfwnU_QAA&oe=64BA6ACC",
                    tags = listOf(Musician.TAG_TEREM),
                ),
            ),
            21 to listOf(
                Musician(
                    name = "One Day In Fukushima",
                    description = "Old school grindcore a dél-olasz Eboli városából\n" +
                            "https://onedayinfukushima.bandcamp.com/\n" +
                            "https://www.facebook.com/odifgrind/",
                    country = "IT",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/263577266_288453829960115_4033253818061243372_n.jpg?_nc_cat=101&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=gtQzMb7DiYcAX-SW6og&_nc_ht=scontent.fbud6-3.fna&oh=00_AfA9SF3AQNmMjmBGMFmsmX4TmROD2mX1cQy16BXnsX0a4w&oe=64BA92CF",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "BORU",
                    description = "Doom/sludge Szeged\n" +
                            "https://www.facebook.com/borudoom\n" +
                            "https://boru.bandcamp.com",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/324025248_1327713274688326_5704892652992047091_n.jpg?_nc_cat=106&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=9_TKZ5o50NkAX-ZSdp_&_nc_oc=AQmHXes2w4loG_LM8sIxBDx8Q0RO-DdtMZevtUAOWeyv7NpIAfz_VmpVjhJOIcbQiv0y_4BpVP9_M0bYr9CF7rAj&_nc_ht=scontent.fbud6-3.fna&oh=00_AfAcaLn6EeqymFpBxvrlChPE_CEpLnUIVwPyP7-J-gX5LA&oe=64BAB2AB",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Cestode",
                    description = "Sludge/punk Lipcse\n" +
                            "https://vaultofheaven.bandcamp.com/album/demo",
                    country = "D",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Total Fraud",
                    description = "Hardcorepunk Berlin\n" +
                            "https://vaultofheaven.bandcamp.com/album/first-demo",
                    country = "D",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Lightchaser",
                    description = "Post-hardcore Esztergom\n" +
                            "https://www.facebook.com/lightchaserhc",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t1.6435-9/168126544_137116428413555_4218572342740342691_n.jpg?_nc_cat=101&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=-3Y-IhxxxnoAX_QvZNg&_nc_ht=scontent.fbud6-3.fna&oh=00_AfAeLRNSn7aUOQv472tgotfL7SAQTSeWPXh-Zm7boCKE_Q&oe=64DDC13E",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Wishes",
                    description = "Melodic-hardcore Budapest\n" +
                            "https://www.facebook.com/wishesbp",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/310920610_638861054424150_5867805372688736259_n.jpg?_nc_cat=101&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=4PIAPHJH0dMAX9S94-9&_nc_ht=scontent.fbud6-3.fna&oh=00_AfDw-oJhgPap_uTER2aSxFkoXlFO0C6oq4QAL-qMh5hNNA&oe=64BC3057",
                    tags = listOf(Musician.TAG_TEREM),
                ),
            ),
            22 to listOf(
                Musician(
                    name = "afewyearslater",
                    description = "Poppunk Budapest\n" +
                            "https://www.facebook.com/afewyearslater\n" +
                            "https://afewyearslater.bandcamp.com/",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-3.fna.fbcdn.net/v/t39.30808-6/344155652_558139676405890_7970140373196773129_n.jpg?_nc_cat=106&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=BSj6os6nYAkAX8h3dLZ&_nc_ht=scontent.fbud6-3.fna&oh=00_AfBBsqWQLn058NJTMVmDxSEyojBeZjla8eAirFsHQzEKNQ&oe=64BBD528",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "DINYA",
                    description = "Screamo/punk Csehország\n" +
                            "https://dinya.bandcamp.com/\n" +
                            "https://www.instagram.com/dinya_hc/",
                    country = "CZ",
                    imageUrl = "https://instagram.fbud6-4.fna.fbcdn.net/v/t51.2885-19/345984137_970575180960130_6867576486872385123_n.jpg?stp=dst-jpg_s150x150&_nc_ht=instagram.fbud6-4.fna.fbcdn.net&_nc_cat=110&_nc_ohc=7q57esoIjEQAX_7jdIA&edm=ABmJApABAAAA&ccb=7-5&oh=00_AfDOaVlaS0QjcQ3sfnISSckSkupEnc_JuD8aCXSquOEHwQ&oe=64BAB511&_nc_sid=b41fef",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Juggler",
                    description = "80's magyar punk Budapest-Szeged\n" +
                            "https://www.facebook.com/jugglervagyok\n" +
                            "https://jugglervagyok.bandcamp.com/",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-4.fna.fbcdn.net/v/t39.30808-6/346065737_539354791739258_4851432124768911728_n.jpg?_nc_cat=102&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=89t9rz-QDVUAX8ijDLm&_nc_ht=scontent.fbud6-4.fna&oh=00_AfAcLU1qNjKsU_EsUNj5ufxHw-bRJo-EnCoOmzkNWTBYPw&oe=64BBEAFB",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Kihalás",
                    description = "D-beat a magyar ugarról\n" +
                            "https://kihals.bandcamp.com/",
                    country = "HU",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Kronstadt",
                    description = "Blackened hardcore Csehország\n" +
                            "https://www.facebook.com/KronstadtCzech\n" +
                            "https://kronstadtblack.bandcamp.com/",
                    country = "CZ",
                    imageUrl = "https://scontent.fbud6-4.fna.fbcdn.net/v/t39.30808-6/332276563_718930153112085_3471371853576622998_n.jpg?_nc_cat=104&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=hBEgKrOP6dsAX-ZvHu_&_nc_ht=scontent.fbud6-4.fna&oh=00_AfBdbPb3zo0avQPKPo2mn2XuUjE8FGmIt5UbS3WB85yTrA&oe=64BC1EF4",
                    tags = listOf(Musician.TAG_TEREM),
                ),
                Musician(
                    name = "Lord Iron Livah",
                    description = "Local punk-rap\n" +
                            "https://www.facebook.com/haszkovouberalles\n" +
                            "https://siraluminumkidney.bandcamp.com/",
                    country = "HU",
                    imageUrl = "https://scontent.fbud6-4.fna.fbcdn.net/v/t39.30808-6/347407625_3663767677226943_7029005164474855706_n.jpg?_nc_cat=104&cb=99be929b-59f725be&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=ELWDyhY6mUgAX_LiTuv&_nc_ht=scontent.fbud6-4.fna&oh=00_AfANtci5eq3UzRKYkZ2rZAch6TOL22aIAkFAVobVbRWjPg&oe=64BBE92F",
                    tags = listOf(Musician.TAG_TEREM),
                ),
            ),
        )

    }
}