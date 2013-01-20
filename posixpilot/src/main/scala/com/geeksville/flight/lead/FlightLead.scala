package com.geeksville.flight.lead

// Standard akka imports
import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.wingman.Wingman
import com.geeksville.util.Counted
import com.geeksville.util.SystemTools
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.shell.ScalaShell
import com.geeksville.shell.ScalaConsole
import gnu.io.NoSuchPortException
import com.geeksville.logback.Logging
import com.geeksville.flight.FlightLead
import com.geeksville.akka.MockAkka

object Main extends Logging {

  val arduPilotId = Wingman.targetSystemId
  val groundControlId = 255
  val wingmanId = Wingman.systemId

  /**
   * We use a systemId 2, because the ardupilot is normally on 1.
   */
  val systemId: Int = 2

  def createSerial() {
    try {
      val baudRate = 57600 // Use 115200 for a local connection, 57600 for 3dr telemetry
      val port = "/dev/ttyUSB0" // Use ttyACM0 for local serial

      // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
      val mavSerial = MockAkka.actorOf(MavlinkPosix.openFtdi(port, baudRate)(), "serrx")

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavSerial, groundControlId)
      // Anything from the wingman, send it to the serial port
      MavlinkEventBus.subscribe(mavSerial, wingmanId)

      // Watch for failures
      MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), arduPilotId)
    } catch {
      case ex: NoSuchPortException =>
        logger.error("No serial port found, disabling...")
    }
  }

  def main(args: Array[String]) {
    logger.info("FlightLead running...")
    logger.debug("CWD is " + System.getProperty("user.dir"))

    // Needed for rxtx native code
    //val libprop = "java.library.path"
    System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyUSB0")
    SystemTools.addDir("libsrc") // FIXME - skanky hack to find rxtx dll

    // FIXME create this somewhere else
    val mavUDP = MockAkka.actorOf(new MavlinkUDP, "mavudp")

    val startSerial = true
    val startFlightLead = false
    val startWingman = false
    val dumpSerialRx = true

    if (startSerial)
      createSerial()

    if (startFlightLead) {
      // Create flightlead actors
      // If you want logging uncomment the following line
      // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
      MockAkka.actorOf(new FlightLead, "lead")
      MockAkka.actorOf(new IGCPublisher(getClass.getResourceAsStream("pretty-good-res-dumps-1hr.igc")), "igcpub")

      // Watch for failures
      MavlinkEventBus.subscribe(MockAkka.actorOf(new HeartbeatMonitor), systemId)
    }

    //
    // Wire up our subscribers
    //

    // Anything from the ardupilot, forward it to the controller app
    MavlinkEventBus.subscribe(mavUDP, arduPilotId)

    // Also send our wingman and flightlead planes to the ground control app
    MavlinkEventBus.subscribe(mavUDP, wingmanId)
    MavlinkEventBus.subscribe(mavUDP, systemId)

    // Anything from our sim lead, send it to the controller app (so it will hopefully show him)
    // Doesn't work yet - mission planner freaks out
    // MavlinkEventBus.subscribe(mavUDP, VehicleSimulator.systemId)

    if (startWingman)
      // Create wingman actors
      MockAkka.actorOf(new Wingman, "wing")

    // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
    MockAkka.actorOf(new LogIncomingMavlink(arduPilotId,
      if (dumpSerialRx)
        LogIncomingMavlink.allowDefault
      else
        LogIncomingMavlink.allowNothing), "ardlog")

    // to see GroundControl packets
    MockAkka.actorOf(new LogIncomingMavlink(groundControlId), "gclog")

    val shell = new ScalaShell() {
      override def name = "flight"
      override def initCmds = Seq("import com.geeksville.flight.lead.ShellCommands._")

      override def onExit() {
        logger.info("Shutting down actors...")
        MockAkka.shutdown()

        super.onExit()
      }
    }
    shell.run()
  }
}
