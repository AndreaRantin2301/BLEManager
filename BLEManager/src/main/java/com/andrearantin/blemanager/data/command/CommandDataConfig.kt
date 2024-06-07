package com.andrearantin.blemanager.data.command

import com.andrearantin.blemanager.utils.Unused

/**
 * Created by: Andrea Rantin
 * Date: 07/06/2024
 * Time: 17:35
 */

/**
 * Class representing the data bytes configuration of your command. You have to provide this class with an implementation
 * of the packDataFun method to pack the data bytes into the full command array
 * @param cmdDataStartPos The position of the byte at which the command data starts. Defaults to 3
 * @param cmdDataEndPos The position of the byte at which the command data ends. Defaults to 6
 * @param cmdDataLen The total length of the command data. Defaults to 4
 * @param isMsbFirst Whether the most significant byte is the first one or the last one
 * @property packDataFun Function to pack the data bytes into the full command array.
 * You have to provide this function with your own implementation
 */
data class CommandDataConfig<T>(
    internal var cmdDataStartPos : Int = DEFAULT_CMD_DATA_START_POS,
    internal var cmdDataEndPos : Int = DEFAULT_CMD_DATA_END_POS,
    internal var cmdDataLen : Int = DEFAULT_CMD_DATA_LEN,
    @Unused
    internal var isMsbFirst : Boolean = true,
    internal var packDataFun : (T,ByteArray) -> Boolean = {_,_ -> false}

){
    companion object {
        const val DEFAULT_CMD_DATA_START_POS : Int = 3
        const val DEFAULT_CMD_DATA_END_POS : Int = 6
        const val DEFAULT_CMD_DATA_LEN : Int = 4
    }
}
