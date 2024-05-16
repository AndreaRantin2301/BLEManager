package com.andrearantin.blemanager

import android.bluetooth.BluetoothGattCharacteristic
import com.andrearantin.blemanager.data.BLEConnectionEvent
import com.andrearantin.blemanager.data.BLEDataEvent
import com.andrearantin.blemanager.data.BLEDataResult
import com.andrearantin.blemanager.data.CrcConfig
import com.andrearantin.blemanager.data.SofEofConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

/**
 * Class to manage BLE received data and send updates on connection events
 * * @property connectionEventChannel channel for delivering connectivity change results
 * * @property characteristicDataChannel channel for delivering characteristic data(Such as when the application receives something from the BLE device
 */
class BLEDataManager() : BLEServiceInterface {

    companion object {
        const val DEFAULT_DATA_LEN : Int = 9

    }

    var dataLen : Int = DEFAULT_DATA_LEN
    var sofEofConfig : SofEofConfig = SofEofConfig()
    var crcConfig : CrcConfig = CrcConfig()

    val connectionEventChannel = Channel<BLEConnectionEvent>()
    val characteristicDataChannel = Channel<BLEDataEvent>()

    override fun onBLEConnectionStateChanged(bleConnectionEvent: BLEConnectionEvent) {
        runBlocking {
            connectionEventChannel.send(bleConnectionEvent)
        }
    }

    override fun onBLEDataReceived(bluetoothGattCharacteristic: BluetoothGattCharacteristic) {
        val data : ByteArray = bluetoothGattCharacteristic.value
        runBlocking {
            if (!checkSofEof(data)) characteristicDataChannel.send(BLEDataEvent(BLEDataResult.SOF_EOF_ERROR,null))
            if (!checkCrc(data)) characteristicDataChannel.send(BLEDataEvent(BLEDataResult.CRC_ERROR,null))
            characteristicDataChannel.send(BLEDataEvent(BLEDataResult.OK,data))
        }
    }

    private fun checkSofEof(data : ByteArray) : Boolean{
        if (!sofEofConfig.isUsed) return true
        if (dataLen < 2) return false
        if (data[sofEofConfig.sofBytePos] != sofEofConfig.sofVal) return false
        if (data[sofEofConfig.eofBytePos] != sofEofConfig.eofVal) return false
        return true
    }

    private fun checkCrc(data : ByteArray) : Boolean{
        if (!crcConfig.isUsed) return true
        if (dataLen < crcConfig.crcLen) return false
        val crcArray : ByteArray = data.sliceArray(crcConfig.crcDataStartPos..crcConfig.crcDataEndPos)
        if (crcArray.size != crcConfig.crcLen) return false
        val androidCrc = crcConfig.calcCrcFun(crcArray,crcConfig.crcLen)
        val deviceCrc = data[crcConfig.crcBytePos]
        if (androidCrc != deviceCrc) return false
        return true
    }
}