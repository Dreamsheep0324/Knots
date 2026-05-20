package com.tang.prm.service

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.tang.prm.service.reminder.ReminderReceiver
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ReminderReceiverTest {

    @Test
    fun reminderReceiver_classExists() {
        val clazz = Class.forName("com.tang.prm.service.reminder.ReminderReceiver")
        assertThat(clazz).isNotNull()
    }

    @Test
    fun reminderReceiver_canBeInstantiated() {
        val context = mockk<Context>(relaxed = true)
        val intent = mockk<Intent>(relaxed = true)
        val receiver = ReminderReceiver()
        assertThat(receiver).isNotNull()
        assertThat(receiver).isInstanceOf(android.content.BroadcastReceiver::class.java)
    }
}
