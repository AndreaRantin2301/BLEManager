package com.andrearantin.blemanager.data

/**
 * Class representing the SOF and EOF checks configuration in your app. You can change byte positions and values for SOF and EOF
 * @param isUsed when set to false all SOF and EOF checks on the received data are skipped. Defaults to true
 * @param sofVal value assigned to the SOF byte that will be checked in every packet
 * @param eofVal value assigned to the EOF byte that will be checked in every packet
 * @param sofBytePos position of the SOF byte in the received data packet.
 * @param eofBytePos position of the EOF byte in the received data packet.
 */
data class SofEofConfig(
    internal var sofBytePos : Int = DEFAULT_SOF_BYTE_POS,
    internal var eofBytePos : Int = DEFAULT_EOF_BYTE_POS,
    internal var sofVal : Byte = DEFAULT_SOF_VAL,
    internal var eofVal : Byte = DEFAULT_EOF_VAL,
    internal var isUsed : Boolean = true
){
    companion object {
        const val DEFAULT_SOF_BYTE_POS : Int = 1
        const val DEFAULT_EOF_BYTE_POS : Int = 8
        const val DEFAULT_SOF_VAL : Byte = 0xAA.toByte()
        const val DEFAULT_EOF_VAL : Byte = 0xBB.toByte()
    }
}
