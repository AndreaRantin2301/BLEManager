package com.andrearantin.blemanager.data.command

import com.andrearantin.blemanager.data.CrcConfig
import com.andrearantin.blemanager.data.SofEofConfig
import com.andrearantin.blemanager.utils.BLEManagerLogger

//TODO COMMENTARE
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

    fun getCmdBytes() : CommandBytesResult {
        var nTotBytes = calculateCmdSize()
        val cmdBytes : ByteArray = ByteArray(nTotBytes)
        if (!setHeaderBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_HEADER)
        if (!setSofEofBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_SOF_EOF)
        if (!setCmdDataBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_CMD_DATA)
        if (!setCrcBytes(cmdBytes)) return CommandBytesResult(null,BytesResult.ERROR_CRC)
        return CommandBytesResult(cmdBytes,BytesResult.OK)
    }

    private fun calculateCmdSize() : Int{
        var nTotBytes : Int = 0
        if (usesHeader) nTotBytes ++
        if (sofEofConfig.isUsed) nTotBytes += 2
        if (crcConfig.isUsed) nTotBytes ++
        nTotBytes += cmdDataConfig.cmdDataLen
        return nTotBytes
    }

    private fun setHeaderBytes(cmdBytes: ByteArray) : Boolean {
        if (usesHeader){
            if (cmdBytes.isEmpty()) return false
            cmdBytes[0] = headerVal
            BLEManagerLogger.d(TAG, "BYTES SET HEADER: ${cmdBytes.contentToString()}")
            return true
        }
        return true
    }

    private fun setSofEofBytes(cmdBytes : ByteArray) : Boolean{
        if (!sofEofConfig.isUsed) return true
        if (cmdBytes.size < 2) return false
        cmdBytes[sofEofConfig.sofBytePos] = sofEofConfig.sofVal
        cmdBytes[sofEofConfig.eofBytePos] = sofEofConfig.eofVal
        BLEManagerLogger.d(TAG, "BYTES SET SOF EOF: ${cmdBytes.contentToString()}")
        return true
    }

    private fun setCmdDataBytes(cmdBytes: ByteArray) : Boolean{
        if (cmdBytes.size < cmdDataConfig.cmdDataLen + 1) return false
        cmdBytes[cmdBytePos] = cmd
        BLEManagerLogger.d(TAG, "BYTES SET CMD DATA: ${cmdBytes.contentToString()}")
        return cmdDataConfig.packDataFun(cmdData,cmdBytes)
    }

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
