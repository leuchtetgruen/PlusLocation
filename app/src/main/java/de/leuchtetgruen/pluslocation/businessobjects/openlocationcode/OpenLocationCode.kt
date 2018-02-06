package de.leuchtetgruen.pluslocation.businessobjects.openlocationcode

import java.math.BigDecimal

// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Convert locations to and from convenient short codes.
 *
 * Open Location Codes are short, ~10 character codes that can be used instead of street
 * addresses. The codes can be generated and decoded offline, and use a reduced character set that
 * minimises the chance of codes including words.
 *
 * This provides both object and static methods.
 *
 * Create an object with:
 * de.leuchtetgruen.pluslocation.OpenLocationCode code = new de.leuchtetgruen.pluslocation.OpenLocationCode("7JVW52GR+2V");
 * de.leuchtetgruen.pluslocation.OpenLocationCode code = new de.leuchtetgruen.pluslocation.OpenLocationCode("52GR+2V");
 * de.leuchtetgruen.pluslocation.OpenLocationCode code = new de.leuchtetgruen.pluslocation.OpenLocationCode(27.175063, 78.042188);
 * de.leuchtetgruen.pluslocation.OpenLocationCode code = new de.leuchtetgruen.pluslocation.OpenLocationCode(27.175063, 78.042188, 11);
 *
 * Once you have a code object, you can apply the other methods to it, such as to shorten:
 * code.shorten(27.176, 78.05)
 *
 * Recover the nearest match (if the code was a short code):
 * code.recover(27.176, 78.05)
 *
 * Or decode a code into it's coordinates, returning a CodeArea object.
 * code.decode()
 *
 * @author Jiri Semecky
 * @author Doug Rinckes
 */
open class OpenLocationCode {

    /** The current code for objects.  */
    /**
     * Returns the string representation of the code.
     */
    val code: String

    /** Returns whether this [OpenLocationCode] is a full Open Location Code.  */
    val isFull: Boolean
        get() = code.indexOf(SEPARATOR.toInt()) == SEPARATOR_POSITION

    /** Returns whether this [OpenLocationCode] is a short Open Location Code.  */
    val isShort: Boolean
        get() =
            code.indexOf(SEPARATOR.toInt()) in 0..(SEPARATOR_POSITION - 1)

    /**
     * Returns whether this [OpenLocationCode] is a padded Open Location Code, meaning that it
     * contains less than 8 valid digits.
     */
    private val isPadded: Boolean
        get() = code.indexOf(PADDING_CHARACTER.toInt()) >= 0



    /**
     * Creates Open Location Code object for the provided code.
     * @param code A valid OLC code. Can be a full code or a shortened code.
     * @throws IllegalArgumentException when the passed code is not valid.
     * @constructor
     */
    @Throws(IllegalArgumentException::class)
    constructor(code: String) {
        if (!isValidCode(code.toUpperCase())) {
            throw IllegalArgumentException(
                    "The provided code '$code' is not a valid Open Location Code.")
        }
        this.code = code.toUpperCase()
    }

    /**
     * Creates Open Location Code.
     * @param latitude The latitude in decimal degrees.
     * @param longitude The longitude in decimal degrees.
     * @param codeLength The desired number of digits in the code.
     * @throws IllegalArgumentException if the code length is not valid.
     * @constructor
     */
    @Throws(IllegalArgumentException::class)
    @JvmOverloads constructor(latitude: Double, longitude: Double, codeLength: Int = CODE_PRECISION_NORMAL) {
        var latitude = latitude
        var longitude = longitude
        // Check that the code length requested is valid.
        if (codeLength < 4 || codeLength < PAIR_CODE_LENGTH && codeLength % 2 == 1) {
            throw IllegalArgumentException("Illegal code length " + codeLength)
        }
        // Ensure that latitude and longitude are valid.
        latitude = clipLatitude(latitude)
        longitude = normalizeLongitude(longitude)

        // Latitude 90 needs to be adjusted to be just less, so the returned code can also be decoded.
        if (latitude == LATITUDE_MAX.toDouble()) {
            latitude = latitude - 0.9 * computeLatitudePrecision(codeLength)
        }

        // Adjust latitude and longitude to be in positive number ranges.
        var remainingLatitude = BigDecimal(latitude).add(LATITUDE_MAX)
        var remainingLongitude = BigDecimal(longitude).add(LONGITUDE_MAX)

        // Count how many digits have been created.
        var generatedDigits = 0
        // Store the code.
        val codeBuilder = StringBuilder()
        // The precisions are initially set to ENCODING_BASE^2 because they will be immediately
        // divided.
        var latPrecision = ENCODING_BASE.multiply(ENCODING_BASE)
        var lngPrecision = ENCODING_BASE.multiply(ENCODING_BASE)
        while (generatedDigits < codeLength) {
            if (generatedDigits < PAIR_CODE_LENGTH) {
                // Use the normal algorithm for the first set of digits.
                latPrecision = latPrecision.divide(ENCODING_BASE)
                lngPrecision = lngPrecision.divide(ENCODING_BASE)
                val latDigit = remainingLatitude.divide(latPrecision, 0, BigDecimal.ROUND_FLOOR)
                val lngDigit = remainingLongitude.divide(lngPrecision, 0, BigDecimal.ROUND_FLOOR)
                remainingLatitude = remainingLatitude.subtract(latPrecision.multiply(latDigit))
                remainingLongitude = remainingLongitude.subtract(lngPrecision.multiply(lngDigit))
                codeBuilder.append(CODE_ALPHABET[latDigit.toInt()])
                codeBuilder.append(CODE_ALPHABET[lngDigit.toInt()])
                generatedDigits += 2
            } else {
                // Use the 4x5 grid for remaining digits.
                latPrecision = latPrecision.divide(GRID_ROWS)
                lngPrecision = lngPrecision.divide(GRID_COLUMNS)
                val row = remainingLatitude.divide(latPrecision, 0, BigDecimal.ROUND_FLOOR)
                val col = remainingLongitude.divide(lngPrecision, 0, BigDecimal.ROUND_FLOOR)
                remainingLatitude = remainingLatitude.subtract(latPrecision.multiply(row))
                remainingLongitude = remainingLongitude.subtract(lngPrecision.multiply(col))
                codeBuilder.append(
                        CODE_ALPHABET[row.toInt() * GRID_COLUMNS.toInt() + col.toInt()])
                generatedDigits += 1
            }
            // If we are at the separator position, add the separator.
            if (generatedDigits == SEPARATOR_POSITION) {
                codeBuilder.append(SEPARATOR)
            }
        }
        // If the generated code is shorter than the separator position, pad the code and add the
        // separator.
        if (generatedDigits < SEPARATOR_POSITION) {
            while (generatedDigits < SEPARATOR_POSITION) {
                codeBuilder.append(PADDING_CHARACTER)
                generatedDigits++
            }
            codeBuilder.append(SEPARATOR)
        }
        this.code = codeBuilder.toString()
    }

    /**
     * Decodes [OpenLocationCode] object into [CodeArea] object encapsulating
     * latitude/longitude bounding box.
     */
    open fun decode(): CodeArea {
        if (!isFullCode(code)) {
            throw IllegalStateException(
                    "Method decode() could only be called on valid full codes, code was $code.")
        }
        // Strip padding and separator characters out of the code.
        val decoded = code.replace(SEPARATOR.toString(), "")
                .replace(PADDING_CHARACTER.toString(), "")

        var digit = 0
        // The precisions are initially set to ENCODING_BASE^2 because they will be immediately
        // divided.
        var latPrecision = ENCODING_BASE.multiply(ENCODING_BASE)
        var lngPrecision = ENCODING_BASE.multiply(ENCODING_BASE)
        // Save the coordinates.
        var southLatitude = BigDecimal(0)
        var westLongitude = BigDecimal(0)

        // Decode the digits.
        while (digit < decoded.length) {
            if (digit < PAIR_CODE_LENGTH) {
                // Decode a pair of digits, the first being latitude and the second being longitude.
                latPrecision = latPrecision.divide(ENCODING_BASE)
                lngPrecision = lngPrecision.divide(ENCODING_BASE)
                var digitVal = CODE_ALPHABET.indexOf(decoded[digit].toInt())
                southLatitude = southLatitude.add(latPrecision.multiply(BigDecimal(digitVal)))
                digitVal = CODE_ALPHABET.indexOf(decoded[digit + 1].toInt())
                westLongitude = westLongitude.add(lngPrecision.multiply(BigDecimal(digitVal)))
                digit += 2
            } else {
                // Use the 4x5 grid for digits after 10.
                val digitVal = CODE_ALPHABET.indexOf(decoded[digit].toInt())
                val row = (digitVal / GRID_COLUMNS.toInt())
                val col = digitVal % GRID_COLUMNS.toInt()
                latPrecision = latPrecision.divide(GRID_ROWS)
                lngPrecision = lngPrecision.divide(GRID_COLUMNS)
                southLatitude = southLatitude.add(latPrecision.multiply(BigDecimal(row)))
                westLongitude = westLongitude.add(lngPrecision.multiply(BigDecimal(col)))
                digit += 1
            }
        }
        return CodeArea(
                southLatitude.subtract(LATITUDE_MAX),
                westLongitude.subtract(LONGITUDE_MAX),
                southLatitude.subtract(LATITUDE_MAX).add(latPrecision),
                westLongitude.subtract(LONGITUDE_MAX).add(lngPrecision))
    }

    /**
     * Returns short [OpenLocationCode] from the full Open Location Code created by removing
     * four or six digits, depending on the provided reference point. It removes as many digits as
     * possible.
     */
    fun shorten(referenceLatitude: Double, referenceLongitude: Double): OpenLocationCode {
        if (!isFull) {
            throw IllegalStateException("shorten() method could only be called on a full code.")
        }
        if (isPadded) {
            throw IllegalStateException("shorten() method can not be called on a padded code.")
        }

        val codeArea = decode()
        val range = Math.max(
                Math.abs(referenceLatitude - codeArea.centerLatitude),
                Math.abs(referenceLongitude - codeArea.centerLongitude))
        // We are going to check to see if we can remove three pairs, two pairs or just one pair of
        // digits from the code.
        for (i in 4 downTo 1) {
            // Check if we're close enough to shorten. The range must be less than 1/2
            // the precision to shorten at all, and we want to allow some safety, so
            // use 0.3 instead of 0.5 as a multiplier.
            if (range < computeLatitudePrecision(i * 2) * 0.3) {
                // We're done.
                return OpenLocationCode(code.substring(i * 2))
            }
        }
        throw IllegalArgumentException(
                "Reference location is too far from the Open Location Code center.")
    }

    /**
     * Returns an [OpenLocationCode] object representing a full Open Location Code from this
     * (short) Open Location Code, given the reference location.
     */
    fun recover(referenceLatitude: Double, referenceLongitude: Double): OpenLocationCode {
        var referenceLatitude = referenceLatitude
        var referenceLongitude = referenceLongitude
        if (isFull) {
            // Note: each code is either full xor short, no other option.
            return this
        }
        referenceLatitude = clipLatitude(referenceLatitude)
        referenceLongitude = normalizeLongitude(referenceLongitude)

        val digitsToRecover = SEPARATOR_POSITION - code.indexOf(SEPARATOR.toInt())
        // The precision (height and width) of the missing prefix in degrees.
        val prefixPrecision = Math.pow(ENCODING_BASE.toInt().toDouble(), (2 - digitsToRecover / 2).toDouble())

        // Use the reference location to generate the prefix.
        val recoveredPrefix = OpenLocationCode(referenceLatitude, referenceLongitude)
                .code
                .substring(0, digitsToRecover)
        // Combine the prefix with the short code and decode it.
        val recovered = OpenLocationCode(recoveredPrefix + code)
        val recoveredCodeArea = recovered.decode()
        // Work out whether the new code area is too far from the reference location. If it is, we
        // move it. It can only be out by a single precision step.
        var recoveredLatitude = recoveredCodeArea.centerLatitude
        var recoveredLongitude = recoveredCodeArea.centerLongitude

        // Move the recovered latitude by one precision up or down if it is too far from the reference,
        // unless doing so would lead to an invalid latitude.
        val latitudeDiff = recoveredLatitude - referenceLatitude
        if (latitudeDiff > prefixPrecision / 2 && recoveredLatitude - prefixPrecision > -LATITUDE_MAX.toInt()) {
            recoveredLatitude -= prefixPrecision
        } else if (latitudeDiff < -prefixPrecision / 2 && recoveredLatitude + prefixPrecision < LATITUDE_MAX.toInt()) {
            recoveredLatitude += prefixPrecision
        }

        // Move the recovered longitude by one precision up or down if it is too far from the
        // reference.
        val longitudeDiff = recoveredCodeArea.centerLongitude - referenceLongitude
        if (longitudeDiff > prefixPrecision / 2) {
            recoveredLongitude -= prefixPrecision
        } else if (longitudeDiff < -prefixPrecision / 2) {
            recoveredLongitude += prefixPrecision
        }

        return OpenLocationCode(
                recoveredLatitude, recoveredLongitude, recovered.code.length - 1)
    }

    /**
     * Returns whether the bounding box specified by the Open Location Code contains provided point.
     */
    fun contains(latitude: Double, longitude: Double): Boolean {
        val codeArea = decode()
        return (codeArea.getSouthLatitude() <= latitude
                && latitude < codeArea.getNorthLatitude()
                && codeArea.getWestLongitude() <= longitude
                && longitude < codeArea.getEastLongitude())
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as OpenLocationCode?
        return hashCode() == that!!.hashCode()
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun toString(): String {
        return code
    }

    companion object {

        // Provides a normal precision code, approximately 14x14 meters.
        val CODE_PRECISION_NORMAL = 10

        // Provides an extra precision code, approximately 2x3 meters.
        val CODE_PRECISION_EXTRA = 11

        // A separator used to break the code into two parts to aid memorability.
        private val SEPARATOR = '+'

        // The number of characters to place before the separator.
        private val SEPARATOR_POSITION = 8

        // The character used to pad codes.
        private val PADDING_CHARACTER = '0'

        // The character set used to encode the values.
        val CODE_ALPHABET = "23456789CFGHJMPQRVWX"

        // Note: The double type can't be used because of the rounding arithmetic due to floating point
        // implementation. Eg. "8.95 - 8" can give result 0.9499999999999 instead of 0.95 which
        // incorrectly classify the points on the border of a cell. Therefore all the calcuation is done
        // using BigDecimal.

        // The base to use to convert numbers to/from.
        private val ENCODING_BASE = BigDecimal(CODE_ALPHABET.length)

        // The maximum value for latitude in degrees.
        private val LATITUDE_MAX = BigDecimal(90)

        // The maximum value for longitude in degrees.
        private val LONGITUDE_MAX = BigDecimal(180)

        // Maxiumum code length using just lat/lng pair encoding.
        private val PAIR_CODE_LENGTH = 10

        // Number of columns in the grid refinement method.
        private val GRID_COLUMNS = BigDecimal(4)

        // Number of rows in the grid refinement method.
        private val GRID_ROWS = BigDecimal(5)

        /**
         * Encodes latitude/longitude into 10 digit Open Location Code. This method is equivalent to
         * creating the de.leuchtetgruen.pluslocation.OpenLocationCode object and getting the code from it.
         * @param latitude The latitude in decimal degrees.
         * @param longitude The longitude in decimal degrees.
         */
        fun encode(latitude: Double, longitude: Double): String? {
            return OpenLocationCode(latitude, longitude).code
        }

        /**
         * Encodes latitude/longitude into Open Location Code of the provided length. This method is
         * equivalent to creating the de.leuchtetgruen.pluslocation.OpenLocationCode object and getting the code from it.
         * @param latitude The latitude in decimal degrees.
         * @param longitude The longitude in decimal degrees.
         */
        fun encode(latitude: Double, longitude: Double, codeLength: Int): String? {
            return OpenLocationCode(latitude, longitude, codeLength).code
        }

        /**
         * Decodes code representing Open Location Code into [CodeArea] object encapsulating
         * latitude/longitude bounding box.
         *
         * @param code Open Location Code to be decoded.
         * @throws IllegalArgumentException if the provided code is not a valid Open Location Code.
         */
        @Throws(IllegalArgumentException::class)
        fun decode(code: String): CodeArea {
            return OpenLocationCode(code).decode()
        }

        /** Returns whether the provided Open Location Code is a full Open Location Code.  */
        @Throws(IllegalArgumentException::class)
        fun isFull(code: String): Boolean {
            return OpenLocationCode(code).isFull
        }

        /** Returns whether the provided Open Location Code is a short Open Location Code.  */
        @Throws(IllegalArgumentException::class)
        fun isShort(code: String): Boolean {
            return OpenLocationCode(code).isShort
        }

        /**
         * Returns whether the provided Open Location Code is a padded Open Location Code, meaning that it
         * contains less than 8 valid digits.
         */
        @Throws(IllegalArgumentException::class)
        fun isPadded(code: String): Boolean {
            return OpenLocationCode(code).isPadded
        }

        // Exposed static helper methods.

        /** Returns whether the provided string is a valid Open Location code.  */
        fun isValidCode(code: String?): Boolean {
            var code = code
            if (code == null || code.length < 2) {
                return false
            }
            code = code.toUpperCase()

            // There must be exactly one separator.
            val separatorPosition = code.indexOf(SEPARATOR.toInt())
            if (separatorPosition == -1) {
                return false
            }
            if (separatorPosition != code.lastIndexOf(SEPARATOR.toInt())) {
                return false
            }

            if (separatorPosition % 2 != 0) {
                return false
            }

            // Check first two characters: only some values from the alphabet are permitted.
            if (separatorPosition == 8) {
                // First latitude character can only have first 9 values.
                val index0 = CODE_ALPHABET.indexOf(code[0].toInt())
                if (index0 == null || index0 > 8) {
                    return false
                }

                // First longitude character can only have first 18 values.
                val index1 = CODE_ALPHABET.indexOf(code[1].toInt())
                if (index1 == null || index1 > 17) {
                    return false
                }
            }

            // Check the characters before the separator.
            var paddingStarted = false
            for (i in 0 until separatorPosition) {
                if (paddingStarted) {
                    // Once padding starts, there must not be anything but padding.
                    if (code[i] != PADDING_CHARACTER) {
                        return false
                    }
                    continue
                }
                if (CODE_ALPHABET.indexOf(code[i].toInt()) != -1) {
                    continue
                }
                if (PADDING_CHARACTER == code[i]) {
                    paddingStarted = true
                    // Padding can start on even character: 2, 4 or 6.
                    if (i != 2 && i != 4 && i != 6) {
                        return false
                    }
                    continue
                }
                return false // Illegal character.
            }

            // Check the characters after the separator.
            if (code.length > separatorPosition + 1) {
                if (paddingStarted) {
                    return false
                }
                // Only one character after separator is forbidden.
                if (code.length == separatorPosition + 2) {
                    return false
                }
                for (i in separatorPosition + 1 until code.length) {
                    if (CODE_ALPHABET.indexOf(code[i].toInt()) == -1) {
                        return false
                    }
                }
            }

            return true
        }

        /** Returns if the code is a valid full Open Location Code.  */
        fun isFullCode(code: String): Boolean {
            try {
                return OpenLocationCode(code).isFull
            } catch (e: IllegalArgumentException) {
                return false
            }

        }

        /** Returns if the code is a valid short Open Location Code.  */
        fun isShortCode(code: String): Boolean {
            try {
                return OpenLocationCode(code).isShort
            } catch (e: IllegalArgumentException) {
                return false
            }

        }

        // Private static methods.

        private fun clipLatitude(latitude: Double): Double {
            return Math.min(Math.max(latitude, (-LATITUDE_MAX.toInt()).toDouble()), LATITUDE_MAX.toInt().toDouble())
        }

        private fun normalizeLongitude(longitude: Double): Double {
            var longitude = longitude
            while (longitude < -LONGITUDE_MAX.toInt()) {
                longitude = longitude + LONGITUDE_MAX.toInt() * 2
            }
            while (longitude >= LONGITUDE_MAX.toInt()) {
                longitude = longitude - LONGITUDE_MAX.toInt() * 2
            }
            return longitude
        }

        /**
         * Compute the latitude precision value for a given code length. Lengths <= 10 have the same
         * precision for latitude and longitude, but lengths > 10 have different precisions due to the
         * grid method having fewer columns than rows. Copied from the JS implementation.
         */
        private fun computeLatitudePrecision(codeLength: Int): Double {
            return if (codeLength <= CODE_PRECISION_NORMAL) {
                Math.pow(ENCODING_BASE.toInt().toDouble(), Math.floor((codeLength / -2 + 2).toDouble()))
            } else Math.pow(ENCODING_BASE.toInt().toDouble(), -3.0) / Math.pow(GRID_ROWS.toInt().toDouble(), (codeLength - PAIR_CODE_LENGTH).toDouble())
        }
    }
}

private fun String.lastIndexOf(ch: Int): Int = this.lastIndexOf(ch.toChar())

private fun String.indexOf(ch: Int): Int = this.indexOf(ch.toChar())

