package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.extensions

import de.leuchtetgruen.pluslocation.businessobjects.openlocationcode.OpenLocationCode

fun OpenLocationCode.regionCode() : String = this.code.substring(0, 4)

fun OpenLocationCode.zoneCode() : String = this.code.substring(0, 6)

fun OpenLocationCode.neighbourhoodCode() : String = this.code.substring(0, 8)


/**
 * Will return all neighbourhood codes around the current neighbourhood
 */
fun OpenLocationCode.neighbourHoodCodes() : List<String> {
    val myNeighbourhoodCode = neighbourhoodCode()

    val shuffleLetters = myNeighbourhoodCode.substring(6, 8)

    val shuffledFirstLetters = shuffleSet(OpenLocationCode.CODE_ALPHABET, shuffleLetters[0])
    val shuffledLastLetters = shuffleSet(OpenLocationCode.CODE_ALPHABET, shuffleLetters[1])

    val prefix = myNeighbourhoodCode.substring(0, 6)
    return shuffledFirstLetters.map {
        val firstLetter = it
        shuffledLastLetters.map {
            prefix + firstLetter + it
        }
    }.flatten().filterNot { it == this.code }
}

private fun shuffleSet(letterSet : String, elem : Char) : List<Char> {
    val idx = letterSet.indexOf(elem)

    val prevIdx = if (idx == 0) {
        letterSet.length - 1
    }
    else {
        idx - 1
    }

    val nextIdx = if (idx == (letterSet.length - 1)) {
        0
    }
    else {
        idx + 1
    }

    return listOf(letterSet[prevIdx], letterSet[idx], letterSet[nextIdx])
}