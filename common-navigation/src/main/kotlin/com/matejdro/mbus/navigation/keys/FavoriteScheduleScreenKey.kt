package com.matejdro.mbus.navigation.keys

import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteScheduleScreenKey(val favoriteId: Long) : BaseScreenKey()
