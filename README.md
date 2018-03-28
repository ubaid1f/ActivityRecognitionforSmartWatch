# Activity Recognition Application for Smartwatch 
The designed smartwatch activity recognition application depends on the smartphone because of the limited resources such as power consumption, computation capabilities, and storage capacity. Therefore, connecting the smartwatch with the smartphone is mandatory. The sensory data collected by the smartwatch’s accelerometer is send to the smartphone via Bluetooth. The data are stored and processed in the smartphone and the result of the processed data is sent back to the smartwatch, in order to show it on the screen. Similarly, the activity recognition application processed the user activity on per second basis. But it stores and display the result on the screen when an activity change is identified. In this way the user will not see the same activity repeatedly. The description of each class is described as follows:

i)	MainActivity:
This class is the main body of the application, which handles the overall process. It manages the accelerometer and Bluetooth communication.
 
ii)	BluetoothChatService:
This class deals with the Bluetooth chat between smartphone and smartwatch, based on the Android Open Source Project. We have customized the code in such a way that it sends the data in Json format.

iii)	MessageType:
This class contains message type enums (Start, Stop, Data).
a)	Start: When the application receives a “Start” message, it starts the activity recognition process on the smartwatch.
b)	Stop: When the application receives a “Stop” message, it stops the activity recognition process on the smartwatch.
c)	Data: The “Data” message shows that the message contains sensory data. The smartwatch sends the accelerometer data along with a “Data” tag to the smartphone. The smartphone receives the data and store it into a text file. 

iv)	SensorObject:
This class is responsible for saving, getting, and setting of sensory data.
