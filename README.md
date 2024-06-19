This library provides an implementation of a service for BLE connectivity and data communication with a device.
You can connect to a BLE device and send and receive data from it.

This library uses dagger-hilt for dependency injection so you also need to include it in your project to use this.
It is also strongly recommended to use dagger-hilt for dependency injection in your project too since it makes
setting this up much easier

# Usage

## Manifest

First to use this library you have to declare the service in your manifest like this:

```

<service
    android:name="com.andrearantin.blemanager.BLEService"
    android:enabled="true"
    android:stopWithTask="true" />

```

## Init

The **BLEService** class works alongside a **BLEDataManager** class that is responsible for data handling and connectivity state change.
To use them you simply have to inject them with dagger-hilt like this:

```

@HiltViewModel
class YourViewModel @Inject constructor(
    private val bleService: BLEService,
    private val bleDataManager: BLEDataManager
) :ViewModel() {
    //YOUR LOGIC HERE
}

```

## Connecting to a device

To connect to a device you have to set the service UIDs in the **BLEService** and call the **connect()** function like this:

```

 bleService.serviceUuid = UUID.fromString("0000aaa0-0000-1000-8000-aabbccddeeff")
 bleService.writeUuid = UUID.fromString("0000aaa1-0000-1000-8000-aabbccddeeff")
 bleService.readUuid = UUID.fromString("0000aaa2-0000-1000-8000-aabbccddeeff")

val deviceAddress = "67:94:F3:A2:07:6F" //PUT YOUR DEVICE ADDRESS HERE
val res = bleService.connect(deviceAddress)

```
The **connect()** function returns a boolean that is true in case of a successfull gatt connection or false if the connection
could not be estabilished or the deviceAddress provided was not valid

### Listen for connectivity state changes

The **BLEDataManager** class listens to connectivity state changes from the **BLEService** and sends values trough a channel whenever it detects a connectivity state change.
The value returned are as follow:

- BLEConnectionEvent.BLE_CONNECTED --> The service has successfully connected to the device but the services have not yet been discovered. You cannot send or receive data yet
- BLEConnectionEvent.BLE_DISCONNECTED --> The device disconnected
- BLEConnectionEvent.BLE_SERVICES_DISCOVERED --> The services needed for communication have been discovered. You can start sending and receiving data from the device

To receive those updates you need to setup a channel to receive values like this: 

```

 bleDataManager.connectionEventChannel.consumeAsFlow().collect { bleConnectionEvent ->
    when(bleConnectionEvent){
        //MANAGE UI AND LOGIC AS NEEDED DEPENDING ON ACTION
        BLEConnectionEvent.BLE_CONNECTED -> {
           //YOUR DEVICE CONNECTED LOGIC HERE
        }
        BLEConnectionEvent.BLE_DISCONNECTED -> {
            //YOUR DEVICE DISCONNECTED LOGIC HERE
        }
        BLEConnectionEvent.BLE_SERVICES_DISCOVERED -> {
            //READY TO RECEIVE AND TRASMIT DATA
        }
    }
}

```

## Setting up to transmit data

### Command structure

This library has a command structure where commands who have a cmd value and a data class that holds all the command's data to be transmitted are converted in
a byte array. The class that represents a command is called **BLECommand** and you need to specify with a data class what data the command holds if any.
Here are some examples.

#### Command that has no data

A command that has no data to be transmitted is only composed of a byte that specifies the command value(i.e 0x41)

```

val commandWithNoData : BLECommand<Unit> = BLECommand(0x41,Unit)

```

#### Command that has data

A command that has data also has a data class instance that comes with it that holds all of the command's data(For example integer values that need to be sent to the BLE device)

```

val bleCommandWithData : BLECommand<CmdData> = BLECommand(0x41, CmdData(147,2192))

```

```

data class CmdData(
    val valueToSend1 : Int,
    val valueToSend2 : Int
)

```

#### Additional customization

##### Header byte

Some bluetooth modules like the Proteus-E may require you to send a byte with a specific value like 0x01 as the first byte in order to receive what you send. 
To take this into account the **BLECommand** class has a **usesHeader** boolean parameter that can be toggled depending on the needs of your app. 
This parameter defaults to **true**. The command also has a **headerVal** parameter that specifies the value of that byte in case you need to change it.
That parameter defaults to **0x01**.
Take for example a command that would generate a byte array like this: **0x41 0x60 0x61 0x62**.
If the **usesHeader** parameter is set to true the array would actually look like this: **0x01 0x41 0x60 0x61 0x62**(Where 0x01 is the value of the header byte)

Example

```
bleCommand.usesHeader = true //THIS WILL ADD THE HEADER BYTE
bleCommand.headerVal = 0x01 //THE HEADER BYTE WILL BE 0x01

```

##### SOF/EOF bytes configuration

If you want some integrity check between packets sent and received with the BLE device, it might be a good idea to implement a SOF/EOF bytes check where both ends check
if the first and last byte of the array are equal to a defined value. To accomodate this the **BLECommand** class has a data class parameter called **SofEofConfig** 
that can be configured.

In this data class you can specify the position of both the SOF and EOF byte and their value. It also has a boolean flag to enable/disable it(If disabled no SOF/EOF bytes will be added to the byte array of the command)

Example

```

val sofEofConfig : SofEofConfig = SofEofConfig(
    sofBytePos = 1,
    eofBytePos = 8, 
    sofVal = 0xAA.toByte(),
    eofVal = 0xBB.toByte(),
    isUsed = true
)
bleCommand.sofEofConfig = sofEofConfig

```

##### CRC configuration

If you want additional integrity check like a CRC check on data sent with the command you can configure the **CrcConfig** data class of **BLECommand**. This class specifies the position in the byte array
of the byte that contains the result of the CRC check aswell as the lenght of the data on which the CRC is calculated on, and the start and end index of the data on which the CRC is calculated on. It also has a **calcCrcFun** parameter that takes in your implementation of a function to calculate the CRC that wants 2 parameters: the command byte array and the CRC lenght, and returns a byte that corresponds to the CRC value. It also has a boolean flag to disable or enable any CRC checks on the command byte array and addition of the CRC byte.

Example

```

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

```
##### Command data configuration

To configure how the data from the data class of the command is packed into the byte array you can use the class **CommandDataConfig** of **BLECommand**. It specifies the start and end index of where the data
needs to be in the command byte array, aswell as the total data lenght. It also has a **packDataFun** parameter that takes in your implementation of a function that packs the command data into a byte array to be added to the command bytes. It wants 2 parameters: the data class representing the command data(I.e our CmdData from our previous example) and the byte array in which the data will be packed into bytes. It needs to return a boolean that indicates whether the packing was successfull or not

Example

```

val cmdDataConfig : CommandDataConfig<TestCmdData> = CommandDataConfig(
    cmdDataStartPos = 2,
    cmdDataEndPos = 6,
    cmdDataLen = 5,
    packDataFun = {cmdData, cmdBytes ->
          packData(cmdData,cmdBytes)
    },
    isMsbFirst = true //NOT USED FOR THE MOMENT
)
bleCommand.cmdDataConfig = cmdDataConfig

```

### Get byte array from command and transmit

Once the command is configured you can get its byte array representation and send it to the BLE device like this:

```

val cmdBytesResult : CommandBytesResult = bleCommand.getCmdBytes()
    if (cmdBytesResult.result == BytesResult.OK){
        bleService.writeBytes(cmdBytesResult.cmdBytes) //SEND THE COMMAND TO THE BLE DEVICE
    }else{
        //MANAGE THE ERROR ACCORDING TO YOUR NEEDS
    }

```

The **getCmdBytes** function returns a **CommandBytesResult**. The **CommandBytesResult** contains a **result** param indicating if the operation was succesful and a **cmdBytes** param that contains
the command byte array representation if the operation was successful or is null if there was an error.

Possible results from **getCmdBytes**

- BytesResult.ERROR_HEADER --> There was an error related to the header byte. **cmdBytes** will be **null** in that case
- BytesResult.ERROR_SOF_EOF --> There was an error related to the SOF/EOF bytes. **cmdBytes** will be **null** in that case
- BytesResult.ERROR_CRC --> There was an error related to the CRC function or configuration. **cmdBytes** will be **null** in that case
- BytesResult.ERROR_CMD_DATA --> There was an error related to the packCmdData function or how the command data configuration was set up. **cmdBytes** will be **null** in that case
- BytesResult.OK --> Everything was successful. **cmdBytes** will contain the byte array representation of the command
