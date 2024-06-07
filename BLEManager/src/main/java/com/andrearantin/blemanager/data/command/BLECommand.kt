package com.andrearantin.blemanager.data.command

import com.andrearantin.blemanager.data.CrcConfig
import com.andrearantin.blemanager.data.SofEofConfig
import com.andrearantin.blemanager.utils.BLEManagerLogger

/**
 * Created by: Andrea Rantin
 * Date: 07/06/2024
 * Time: 17:04
 */

/**
 * Class representation for a command that can be sent using the BLE Service as a byte array.
 * This can be configured to use a header as the first byte such as 0x01(Needed for communication with some bluetooth modules
 * like Proteus-E). It also has the possibility to use a SOF/EOF byte config and CRC check config that both can be enabled and
 * disabled using a boolean.
 * @param cmd The byte value of the command to be sent
 * @param cmdData A data class representing the byte data to be sent with the command. Using the CommandDataConfig you can
 * set your own configuration like the data length and if the data is to be sent MSB first. You also need to set your implementation
 * to pack the cmdData bytes into the byte array that will be sent by the service
 * @property usesHeader Boolean to indicate whether to use a header byte or not. Defaults to true
 * @property headerVal Value of the header byte if used. Defaults to 0x01
 * @property cmdBytePos Position of the byte where the command value is stored. Defaults to 2
 * @property sofEofConfig Configuration for the SOF/EOF bytes
 * @property crcConfig Configuration for the CRC check
 * @property cmdDataConfig Configuration for the command data bytes
 * @see SofEofConfig
 * @see CrcConfig
 * @see CommandDataConfig
 */
data class BLECommand<T>(
    val cmd : Byte,
    val cmdData : T
){

    var usesHeader : Boolean = true
    var headerVal : Byte = DEFAULT_HEADER_VAL
    var cmdBytePos : Int = DEFAULT_CMD_BYTE_POS
    var sofEofConfig : SofEofConfig = SofEofConfig()
    var crcConfig : CrcConfig = CrcConfig()
    var cmdDataConfig : CommandDataConfig<T> = CommandDataConfig()

    companion object {
        const val DEFAULT_HEADER_VAL : Byte = 0x01
        const val DEFAULT_CMD_BYTE_POS : Int = 2
        private val TAG = BLECommand::class.qualifiedName
    }

    /**
     * Gets the byte array representation of the command.
     * @return A CommandBytesResult with an error code or OK.
     * The byte array is null if the error code is not OK
     */
    fun getCmdBytes() : CommandBytesResult {
        var nTotBytes = calculateCmdSize()
        val cmdBytes : ByteArray = ByteArray(nTotBytes)
        if (!setHeaderBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_HEADER)
        if (!setSofEofBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_SOF_EOF)
        if (!setCmdDataBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_CMD_DATA)
        if (!setCrcBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_CRC)
        return CommandBytesResult(cmdBytes,BytesResult.OK)
    }

    /**
     * Calculates the total number of bytes that the command will take up.
     * This varies depending on if the header and SOF/EOF and CRC configurations are used or not
     * and also on the data length of the command
     * @return the total number of bytes that the command will take up
     */
    private fun calculateCmdSize() : Int{
        var nTotBytes : Int = 0
        if (usesHeader) nTotBytes ++
        if (sofEofConfig.isUsed) nTotBytes += 2
        if (crcConfig.isUsed) nTotBytes ++
        nTotBytes += cmdDataConfig.cmdDataLen
        return nTotBytes
    }

    /**
     * Sets the header byte in the array if used.
     * @param cmdBytes The full byte array of the command
     * @return true if the header byte is not used and also if the byte could be set successfully. False if
     * the header byte is used but the array was empty and the byte could not be set
     */
    private fun setHeaderBytes(cmdBytes: ByteArray) : Boolean {
        if (usesHeader){
            if (cmdBytes.isEmpty()) return false
            cmdBytes[0] = headerVal
            BLEManagerLogger.d(TAG, "BYTES SET HEADER: ${cmdBytes.contentToString()}")
            return true
        }
        return true
    }

    /**
     * Sets the SOF/EOF bytes in the array if used.
     * @param cmdBytes The full byte array of the command
     * @return true if the config is not used and also if the bytes could be set successfully. False if the
     * configuration is used but the bytes could not be set(e.g the array size was < 2)
     */
    private fun setSofEofBytes(cmdBytes : ByteArray) : Boolean{
        if (!sofEofConfig.isUsed) return true
        if (cmdBytes.size < 2) return false
        cmdBytes[sofEofConfig.sofBytePos] = sofEofConfig.sofVal
        cmdBytes[sofEofConfig.eofBytePos] = sofEofConfig.eofVal
        BLEManagerLogger.d(TAG, "BYTES SET SOF EOF: ${cmdBytes.contentToString()}")
        return true
    }

    /**
     * Sets the command data bytes in the array.
     * @param cmdBytes The full byte array of the command
     * @return true if the implementation of packDataFun was successful. False if the bytes could not be set(e.g the
     * size of the whole array was < cmdDataLen + 1 since the array needs at least one another byte for the cmd value)
     */
    private fun setCmdDataBytes(cmdBytes: ByteArray) : Boolean{

        /*
        This has +1 in the check because the array needs at least 1 other byte for the command value
        even if all the other configurations are not used.
        */
        if (cmdBytes.size < cmdDataConfig.cmdDataLen + 1) return false
        cmdBytes[cmdBytePos] = cmd
        BLEManagerLogger.d(TAG, "BYTES SET CMD DATA: ${cmdBytes.contentToString()}")
        return cmdDataConfig.packDataFun(cmdData,cmdBytes)
    }

    /**
     * Sets the CRC bytes in the array if used
     * @param cmdBytes The full byte array of the command
     * @return true if the CRC configuration is not used and also if the bytes could be set successfully. False if
     * the configuration is used but the bytes could not be set(e.g the length of the array used to calc the crc was
     * bigger than the command array or if there was an error in the forming of the crc array.
     */
    private fun setCrcBytes(cmdBytes: ByteArray) : Boolean {
        if (!crcConfig.isUsed) return true
        if (cmdBytes.size < crcConfig.crcLen) return false
        val crcArray = cmdBytes.sliceArray(crcConfig.crcDataStartPos..crcConfig.crcDataEndPos)
        if (crcArray.size != crcConfig.crcLen) return false
        val crc = crcConfig.calcCrcFun(crcArray,crcConfig.crcLen)
        cmdBytes[crcConfig.crcBytePos] = crc
        BLEManagerLogger.d(TAG, "BYTES SET CRC: ${cmdBytes.contentToString()}")
        return true
    }
}
