package com.andrearantin.blemanager.data

/**
 * Class to represent the result of a characteristic data elaboration
 * @param status result of the data elaboration. It will be BLEDataResult.OK with a valid bytearray inside data
 * if the elaboration is successful. It will contain an error with null data otherwise
 * @param data the byte data received. Null if there was an error
 */
data class BLEDataEvent(
    val status : BLEDataResult,
    val data : ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BLEDataEvent

        if (status != other.status) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

}
