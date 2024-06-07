package com.andrearantin.blemanager.data

/**
 * Created by: Andrea Rantin
 * Date: 07/06/2024
 * Time: 17:35
 */

/**
 * Class representing the configuration of the CRC in your app. You can change the bytes positions and crc length as you need
 * @param isUsed when set to false all CRC checks on the received data are skipped. Defaults to true
 * @param calcCrcFun function used to calculate the CRC. Remember to set this if you plan on using this class
 * @param crcBytePos position of the CRC byte in the received data
 * @param crcLen number of bytes on which the CRC is calculated
 * @param crcDataStartPos starting byte position of the data on which the CRC is calculated
 * @param crcDataEndPos ending byte position of the data on which the CRC is calculated
 */
data class CrcConfig(
    internal var crcBytePos : Int = DEFAULT_CRC_BYTE_POS,
    internal var crcLen : Int = DEFAULT_CRC_LEN,
    internal var crcDataStartPos : Int = DEFAULT_CRC_DATA_START_POS,
    internal var crcDataEndPos : Int = DEFAULT_CRC_DATA_END_POS,
    internal var calcCrcFun : ((ByteArray,Int) -> Byte) = {_,_ -> 0},
    internal var isUsed : Boolean = true
){
    companion object {
        const val DEFAULT_CRC_BYTE_POS : Int = 7
        const val DEFAULT_CRC_LEN : Int = 5
        const val DEFAULT_CRC_DATA_START_POS : Int = 2
        const val DEFAULT_CRC_DATA_END_POS : Int = 7
    }
}
