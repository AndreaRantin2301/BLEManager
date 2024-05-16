package com.andrearantin.blemanager.data.command

data class CommandDataConfig<T>(
    internal var cmdDataStartPos : Int = DEFAULT_CMD_DATA_START_POS,
    internal var cmdDataEndPos : Int = DEFAULT_CMD_DATA_END_POS,
    internal var cmdDataLen : Int = DEFAULT_CMD_DATA_LEN,
    internal var isMsbFirst : Boolean = true,
    internal var packDataFun : (T,ByteArray) -> Boolean = {_,_ -> false}

){
    companion object {
        const val DEFAULT_CMD_DATA_START_POS : Int = 3
        const val DEFAULT_CMD_DATA_END_POS : Int = 6
        const val DEFAULT_CMD_DATA_LEN : Int = 4
    }
}
