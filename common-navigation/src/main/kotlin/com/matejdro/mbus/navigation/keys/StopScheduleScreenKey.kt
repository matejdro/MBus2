package com.matejdro.mbus.navigation.keys

import kotlinx.serialization.Serializable

@Serializable
data class StopScheduleScreenKey(val stopId: Int) : BaseScreenKey()
