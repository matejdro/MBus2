package com.matejdro.mbus.common.exceptions

import si.inova.kotlinova.core.outcome.CauseException

class BackendErrorException(message: String?, cause: Throwable? = null) : CauseException(message, cause)
