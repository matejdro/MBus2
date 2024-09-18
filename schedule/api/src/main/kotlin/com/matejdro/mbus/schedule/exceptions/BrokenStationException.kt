package com.matejdro.mbus.schedule.exceptions

import si.inova.kotlinova.core.outcome.CauseException

class BrokenStationException : CauseException(isProgrammersFault = false)
