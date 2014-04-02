package com.geeksville.apiproxy

import java.io.File
import com.geeksville.logback.Logging
import com.geeksville.util.ThreadTools
import com.geeksville.util.Using._
import java.util.UUID
import com.geeksville.dapi._
import com.google.protobuf.ByteString
import java.io.BufferedInputStream
import java.io.FileInputStream
import com.geeksville.mavlink.BinaryMavlinkReader
import com.geeksville.mavlink.TimestampedMessage

/**
 * This utility class will upload a tlog to the new DroneHub
 */
class TLogUploader(srcFile: File, val loginName: String, val password: String, val vehicleId: UUID = UUID.randomUUID) extends Logging {

  val interfaceNum = 0

  private var startTime = 0L

  ThreadTools.start("uploader")(worker)

  private def worker() {
    using(new BufferedInputStream(new FileInputStream(srcFile))) { in =>
      val messages = new BinaryMavlinkReader(in)
      startTime = messages.head.time

      using(new GCSHooksImpl(startTime = startTime)) { webapi: GCSHooks =>

        val email = None // FIXME

        // Create user if necessary/possible
        if (webapi.isUsernameAvailable(loginName))
          webapi.createUser(loginName, password, email)
        else
          webapi.loginUser(loginName, password)

        webapi.flush()

        val sysId = 1;
        webapi.setVehicleId(vehicleId.toString, interfaceNum, sysId, false);

        logger.info("Starting mission")
        webapi.send(Envelope(startMission = Some(StartMissionMsg(keep = false, viewPrivacy = Some(AccessCode.PRIVATE)))))

        messages.foreach(sendMavlink)

        webapi.stopMission(keep = true)

        logger.info("Upload successful")
      }
    }
  }

  private def sendMavlink(m: TimestampedMessage) {
    val deltat = m.time - startTime
    Envelope(mavlink = Some(MavlinkMsg(interfaceNum, Vector(ByteString.copyFrom(m.msg.encode)), Some(deltat))))
  }

  // Methods that can be overridden for notification

  protected def handleUploadFailed(ex: Option[Exception]) {
    logger.error(s"TLog upload failed: $ex")
  }

  protected def handleUploadCompleted(viewURL: String) {
    logger.info(s"TLog upload complete $viewURL")
  }

  protected def handleProgress(percent: Int) {
    logger.debug(s"TLog upload progress $percent")
  }
}