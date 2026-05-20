package com.tang.prm.service

import com.google.common.truth.Truth.assertThat
import com.tang.prm.service.reminder.ReminderReceiver
import org.junit.jupiter.api.Test

class ReminderReceiverTest {

    @Test
    fun reminderReceiver_classExists() {
        val clazz = Class.forName("com.tang.prm.service.reminder.ReminderReceiver")
        assertThat(clazz).isNotNull()
    }

    @Test
    fun reminderReceiver_canBeInstantiated() {
        val receiver = ReminderReceiver()
        assertThat(receiver).isNotNull()
        assertThat(receiver).isInstanceOf(android.content.BroadcastReceiver::class.java)
    }

    @Test
    fun reminderReceiver_hasChannelIdConstant() {
        assertThat(ReminderReceiver.CHANNEL_ID).isEqualTo("tang_reminder_channel")
    }
}
