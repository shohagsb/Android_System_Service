package me.shohag.system_service_events.utils

object Constants {
    const val ACTION_START_SERVICE = "ACTION_START_OR_RESUME_SERVICE"//For starting MotionDetectService forground services this action will be trigger from MainActivity
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"//For Stopping MotionDetectService forground services permanently this action will be trigger from MainActivity
    const val STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION"// For Stopping MotionDetectService forground services permanently this action will be trigger from Notification
    const val STOP_SERVICE_ACTION_CALL = "STOP_SERVICE_ACTION_CALL"//stop call service from notification
    const val CANCEL_ACTION = "CANCEL_ACTION"//cancel al the service from notification of pressing stop services
    const val START_PHONE_SERVICES = "START_PHONE_SERVICES"//starting the calling service from motion services class
}