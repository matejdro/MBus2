package com.matejdro.mbus.navigation.keys

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteScheduleScreenKey(val favoriteId: Long) : BaseScreenKey()
