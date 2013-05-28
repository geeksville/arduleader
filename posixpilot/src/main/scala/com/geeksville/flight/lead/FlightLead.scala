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
import java.io.File
import com.geeksville.mavserve.MavServe

object Main extends Logging {

  val arduPilotId = Wingman.targetSystemId
  val groundControlId = 255
  val wingmanId = Wingman.systemId

  /**
   * We use a systemId 2, because the ardupilot is normally on 1.
   */
  val systemId: Int = 2

  def createMavlinkClient(stream: MavlinkStream) {
    try {
      // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
      val mavSerial = MockAkka.actorOf(stream, "serrx")

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavSerial, groundControlId)

      // Also send anything from our active agent to the serial port
      MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

      // Anything from the wingman, send it to the serial port
      MavlinkEventBus.subscribe(mavSerial, wingmanId)
    } catch {
      case ex: NoSuchPortException =>
        logger.error("No serial port found, disabling...")
    }
  }

  def createSerial() {
    try {
      val telemPort = "/dev/ttyUSB0"
      val serDriver = if ((new File(telemPort)).exists)
        MavlinkPosix.openFtdi(telemPort, 57600)
      else
        MavlinkPosix.openSerial("/dev/ttyACM0", 115200)

      createMavlinkClient(serDriver)
    } catch {
      case ex: NoSuchPortException =>
        logger.error("No serial port found, disabling...")
    }
  }

  def createSITLClient() = createMavlinkClient(MavlinkTCP.connect("localhost", 5760))

  def main(args: Array[String]) {
    logger.info("FlightLead running...")
    logger.debug("CWD is " + System.getProperty("user.dir"))

    // Needed for rxtx native code
    //val libprop = "java.library.path"
    System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyUSB0")

    val arch = System.getProperty("os.arch")
    logger.info("arch: " + arch)
    SystemTools.addDir("libsrc/" + arch) // FIXME - skanky hack to find rxtx dll

    // We don't want anyone else's native libraries
    System.setProperty("jna.nosys", "true")

    // FIXME - select these options based on cmd line flags
    val startOutgoingUDP = true
    val startIncomingUDP = false
    val startSerial = true
    val startSITL = false
    val startFlightLead = false
    val startWingman = false
    val startMonitor = false
    val startMavServe = true
    val dumpSerialRx = false
    val logToConsole = false
    val logToFile = true

    if (startSerial)
      createSerial()

    if (startSITL)
      createSITLClient()

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

    if (startOutgoingUDP || startIncomingUDP) {
      // FIXME create this somewhere else
      logger.info("Opening UDP link")
      val udp = if (startOutgoingUDP)
        new MavlinkUDP(destHostName = Some("192.168.0.160"), destPortNumber = Some(MavlinkUDP.portNumber))
      else
        new MavlinkUDP(localPortNumber = Some(MavlinkUDP.portNumber))

      val mavUDP = MockAkka.actorOf(udp, "mavudp")

      // Anything from the ardupilot, forward it to the controller app
      MavlinkEventBus.subscribe(mavUDP, arduPilotId)

      // Also send our wingman and flightlead planes to the ground control app
      MavlinkEventBus.subscribe(mavUDP, wingmanId)
      MavlinkEventBus.subscribe(mavUDP, systemId)
    }

    if (startMonitor) {
      // Keep a complete model of the arduplane state
      val model = MockAkka.actorOf(new VehicleModel)

      // That model wants to hear messages from id 1
      MavlinkEventBus.subscribe(model, arduPilotId)

      if (startMavServe) {
        new MavServe(model)
      }
    }

    // Anything from our sim lead, send it to the controller app (so it will hopefully show him)
    // Doesn't work yet - mission planner freaks out
    // MavlinkEventBus.subscribe(mavUDP, VehicleSimulator.systemId)

    if (startWingman)
      // Create wingman actors
      MockAkka.actorOf(new Wingman, "wing")

    if (logToConsole) {
      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      MockAkka.actorOf(new LogIncomingMavlink(arduPilotId,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing), "ardlog")

      // to see GroundControl packets
      MockAkka.actorOf(new LogIncomingMavlink(groundControlId), "gclog")
    }

    if (logToFile) {
      // Generate log files mission control would understand
      val logger = MockAkka.actorOf(LogBinaryMavlink.create(true), "gclog")
      MavlinkEventBus.subscribe(logger, arduPilotId)
      MavlinkEventBus.subscribe(logger, groundControlId)
      MavlinkEventBus.subscribe(logger, VehicleSimulator.andropilotId)
    }

    def shell = new ScalaShell() {
      override def name = "flight"
      override def initCmds = Seq("import com.geeksville.flight.lead.ShellCommands._")
    }
    shell.run()

    logger.info("Shutting down actors...")
    MockAkka.shutdown()
  }
}
