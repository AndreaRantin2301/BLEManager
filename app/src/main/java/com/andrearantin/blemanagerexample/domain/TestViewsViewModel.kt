package com.andrearantin.blemanagerexample.domain

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrearantin.blemanager.BLEDataManager
import com.andrearantin.blemanager.BLEService
import com.andrearantin.blemanager.data.BLEConnectionEvent
import com.andrearantin.blemanager.data.BLEDataResult
import com.andrearantin.blemanager.data.CrcConfig
import com.andrearantin.blemanager.data.SofEofConfig
import com.andrearantin.blemanager.data.command.BLECommand
import com.andrearantin.blemanager.data.command.BytesResult
import com.andrearantin.blemanager.data.command.CommandBytesResult
import com.andrearantin.blemanager.data.command.CommandDataConfig
import com.andrearantin.blemanager.utils.BLEManagerLogger
import com.andrearantin.blemanagerexample.NDKBridge
import com.andrearantin.blemanagerexample.TestViewsActivity
import com.andrearantin.blemanagerexample.data.TestCmdData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TestViewsViewModel @Inject constructor(
    private val bleService: BLEService,
    private val bleDataManager: BLEDataManager
) :ViewModel() {

    companion object {
        private val TAG = TestViewsViewModel::class.qualifiedName
    }
    init {
        bleDataManager.crcConfig = bleDataManager.crcConfig.copy(isUsed = false)
        bleDataManager.sofEofConfig = bleDataManager.sofEofConfig.copy(isUsed = false)
        viewModelScope.launch {
            bleDataManager.connectionEventChannel.consumeAsFlow().collect { bleConnectionEvent ->
                when(bleConnectionEvent){
                    //MANAGE UI AND LOGIC AS NEEDED DEPENDING ON ACTION
                    BLEConnectionEvent.BLE_CONNECTED -> {
                        BLEManagerLogger.d(TAG, "BLE DEVICE CONNECTED")
                    }
                    BLEConnectionEvent.BLE_DISCONNECTED -> {
                        BLEManagerLogger.d(TAG, "BLE DEVICE DISCONNECTED")
                    }
                    BLEConnectionEvent.BLE_SERVICES_DISCOVERED -> {
                        BLEManagerLogger.d(TAG, "BLE DEVICE READY")
                    }
                }
            }

            bleDataManager.characteristicDataChannel.consumeAsFlow().collect {dataEvent ->
                when(dataEvent.status){
                    BLEDataResult.SOF_EOF_ERROR -> {
                        //SOF EOF ERROR DO NEEED LOGIC
                        BLEManagerLogger.d(TAG, "SOF EOF ERROR")
                    }
                    BLEDataResult.CRC_ERROR -> {
                        //CRC ERROR DO NEEDED LOGIC
                        BLEManagerLogger.d(TAG, "CRC ERROR")
                    }
                    BLEDataResult.OK -> {
                        //DATA OK MANAGE IT AS YOU WISH
                        BLEManagerLogger.d(TAG, "DATA OK")
                    }
                }
            }
        }
    }

    fun testConnection(){
        bleService.serviceUuid = UUID.fromString("6e400001-c352-11e5-953d-0002a5d5c51b")
        bleService.readUuid = UUID.fromString("6e400003-c352-11e5-953d-0002a5d5c51b")
        bleService.writeUuid = UUID.fromString("6e400002-c352-11e5-953d-0002a5d5c51b")

        //TEST ADDRESS, YOU CAN GET THIS FROM A BLE DEVICE FROM A SCAN
        val deviceAddress = "00:18:DA:50:22:9E"
        Log.w("TEST", "TEST CONNECT")
        val res = bleService.connect(deviceAddress)
        Log.w("TEST", "CONNECTION RES: $res")
    }

    fun testGetCmdAck(){
        val bleCommand : BLECommand<Unit> = BLECommand(0x41,Unit)
        bleCommand.usesHeader = true
        val sofEofConfig : SofEofConfig = SofEofConfig(
            sofBytePos = 1,
            eofBytePos = 8,
            sofVal = 0xAA.toByte(),
            eofVal = 0xBB.toByte(),
            isUsed = true
        )
        bleCommand.sofEofConfig = sofEofConfig
        val crcConfig : CrcConfig = CrcConfig(
            crcBytePos = 7,
            crcLen = 5,
            crcDataStartPos = 2,
            crcDataEndPos = 6,
            calcCrcFun = { byteArray, len ->
                NDKBridge.crcFast(byteArray,len)
            },
            isUsed = true
        )
    }

    fun testGetCmd(){
        val bleCommand : BLECommand<TestCmdData> = BLECommand(0x41, TestCmdData(147,2192))
        bleCommand.usesHeader = true
        val sofEofConfig : SofEofConfig = SofEofConfig(
            sofBytePos = 1,
            eofBytePos = 8,
            sofVal = 0xAA.toByte(),
            eofVal = 0xBB.toByte(),
            isUsed = true
        )
        bleCommand.sofEofConfig = sofEofConfig
        val crcConfig : CrcConfig = CrcConfig(
            crcBytePos = 7,
            crcLen = 5,
            crcDataStartPos = 2,
            crcDataEndPos = 6,
            calcCrcFun = { byteArray, len ->
                NDKBridge.crcFast(byteArray,len)
            },
            isUsed = true
        )
        bleCommand.crcConfig = crcConfig

        val cmdDataConfig : CommandDataConfig<TestCmdData> = CommandDataConfig(
            cmdDataStartPos = 2,
            cmdDataEndPos = 6,
            cmdDataLen = 5,
            packDataFun = {cmdData, cmdBytes ->
                  packData(cmdData,cmdBytes)
            },
            isMsbFirst = true
        )
        bleCommand.cmdDataConfig = cmdDataConfig
        val cmdBytesResult : CommandBytesResult = bleCommand.getCmdBytes()
        if (cmdBytesResult.result == BytesResult.OK){
            Log.w(TAG, "GET BYTES OK ${cmdBytesResult.cmdBytes.contentToString()}")
        }else{
            Log.w(TAG, "GET BYTES ERROR ${cmdBytesResult.result}")
        }
    }

    private fun packData(testCmdData: TestCmdData,cmdBytes : ByteArray) : Boolean {
        if (cmdBytes.size < 4) return false
        cmdBytes[3] = testCmdData.threshold1.shr(8).toByte()
        cmdBytes[4] = testCmdData.threshold1.toByte()
        cmdBytes[5] = testCmdData.threshold2.shr(8).toByte()
        cmdBytes[6] = testCmdData.threshold2.toByte()
        return true
    }
}