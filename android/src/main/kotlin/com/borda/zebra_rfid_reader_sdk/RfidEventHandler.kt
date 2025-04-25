package com.borda.zebra_rfid_reader_sdk

import android.util.Log
import com.borda.zebra_rfid_reader_sdk.utils.*
import com.zebra.rfid.api3.*
import java.util.Date

/**
 * Handles RFID events such as tag reads, status changes, and disconnections.
 *
 * @property reader The RFID reader instance.
 * @property tagHandlerEvent The event handler for tag data events.
 * @property tagFindHandler The event handler for tag find events.utils
 */
class RfidEventHandler(
    private var reader: RFIDReader,
    private var tagHandlerEvent: TagDataEventHandler,
    private var tagFindHandler: TagDataEventHandler,
    private var readTagEvent: TagDataEventHandler,
) :
    RfidEventsListener {

    /**
     * Handles RFID read events.
     *
     * @param e The RFID read event.
     */
    override fun eventReadNotify(e: RfidReadEvents) {
        val myTags: Array<TagData> = reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (index in myTags.indices) {
                if (myTags[index].isContainsLocationInfo) {
                    TagLocationingResponse.setDistancePercent(myTags[index].LocationInfo.relativeDistance.toInt())
                    tagFindHandler.sendEvent(TagLocationingResponse.toJson())
                } else {
                    val tagData = TagDataResponse(
                        myTags[index].tagID,
                        Date(System.currentTimeMillis()).toString()
                    )
                    readTagEvent.sendEvent(tagData.toJson())
                }
            }
        }
    }

    /**
     * Handles RFID status events.
     *
     * @param rfidStatusEvents The RFID status event.
     */
    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {

        /// Battery Event
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.BATTERY_EVENT) {
            val batteryData: IEvents.BatteryData = rfidStatusEvents.StatusEventData.BatteryData
            Log.d(LOG_TAG, "Battery Event: $batteryData")
            Log.d(LOG_TAG, "IS CHARGING -> ${batteryData.getCharging()}")

            ReaderResponse.setConnectionStatus(ConnectionStatus.connected)
            ReaderResponse.setBatteryLevel(batteryData.getLevel().toString())
            tagHandlerEvent.sendEvent(ReaderResponse.toJson())

        }

        /// Disconnection Event
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            Log.d(LOG_TAG, "DISCONNECTION_EVENT")
            reader.disconnect()

            ReaderResponse.disconnected()
            TagLocationingResponse.reset()
            tagHandlerEvent.sendEvent(ReaderResponse.toJson())
            tagFindHandler.sendEvent(TagLocationingResponse.toJson())

        }

        /// Handheld Trigger Event
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {

            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                Log.d(LOG_TAG, "HANDHELD_TRIGGER_PRESSED")
                onTriggerPressed()
            }
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                Log.d(LOG_TAG, "HANDHELD_TRIGGER_RELEASED")
                onTriggerReleased()
            }
        }
    }

    /**
     * Performs an RFID inventory operation.
     */
    @Synchronized
    fun onTriggerPressed() {
        // check reader connection
        if (!isReaderConnected()) return
        try {
            var triggerMode: TriggerMode = BordaHandheldTrigger.getMode()

            if (triggerMode == TriggerMode.INVENTORY_PERFORM) {
                reader.Actions.Inventory.perform()
                Thread.sleep(500);


            } else if (triggerMode == TriggerMode.TAG_LOCATIONING_PERFORM) {
                val tagLocationing = TagLocationingResponse.getTag()
                reader.Actions.TagLocationing.Perform(tagLocationing, null, null);
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    /**
     * Stops the RFID inventory operation.
     */
    @Synchronized
    fun onTriggerReleased() {
        // check reader connection
        if (!isReaderConnected()) return
        try {
            var triggerMode: TriggerMode = BordaHandheldTrigger.getMode()

            if (triggerMode == TriggerMode.INVENTORY_PERFORM) {
                reader.Actions.Inventory.stop()

            } else if (triggerMode == TriggerMode.TAG_LOCATIONING_PERFORM) {
                reader.Actions.TagLocationing.Stop()
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        }
    }

    /**
     * Checks if the RFID reader is connected.
     *
     * @return True if the RFID reader is connected, false otherwise.
     */
    private fun isReaderConnected(): Boolean {
        return if (reader.isConnected) true else {
            Log.d(LOG_TAG, "READER NOT CONNECTED")
            false
        }
    }
}